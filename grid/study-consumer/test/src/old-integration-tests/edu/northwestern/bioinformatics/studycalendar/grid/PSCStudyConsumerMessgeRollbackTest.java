/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.grid;

import edu.northwestern.bioinformatics.studycalendar.dao.*;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.service.*;
import gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.test.AbstractTransactionalSpringContextTests;

import java.util.Date;

/**
 * Added for testing CCTS roll-back script requirement.
 * <p/>
 * Don't use StudyCalendarDbTestCase class because there  are risks of  deleting the database by mistake.
 *
 * @author Saurabh Agrawal
 */
public class PSCStudyConsumerMessgeRollbackTest extends AbstractTransactionalSpringContextTests {


    private TemplateDevelopmentService templateDevelopmentService;
    private StudySiteDao studySiteDao;
    private UserDao userDao;
    private StudyService studyService;
    private DeltaService deltaService;
    private TemplateService templateService;
    private StudyDao studyDao;
    private AmendmentDao amendmentDao;
    private PopulationService populationService;


    private UserRoleDao userRoleDao;


    public static final Log logger = LogFactory.getLog(PSCStudyConsumerMessgeRollbackTest.class);

    private String assignedIdentifier = "SMOKE_TEST";

    private String nciCode = "NCI";
    private SiteDao siteDao;
    private String shortTitle = "SMOTE_TEST";
    private String longTitle = "Test long title";

    public void testRollBackCCTSStudy() throws Exception {
        Study study = studyDao.getByAssignedIdentifier(assignedIdentifier);
        if (study != null) {
            this.deleteReleasedAmendmentForCCTSSmokeTesting();
            commitAndStartNewTransaction();
        } else {
            logger.error(String.format("no study found for given identifier %s", assignedIdentifier));
            createStudy(); //create study and re-run the test case..
        }


    }

    /**
     * Deletes the SMOKE_TEST study created by CCTS grid services . It will only delete 'SMOKE_TEST' study  which was created by 'ccts@mail.nih.gov' user
     * at 'NCI' site.
     */
    private void deleteReleasedAmendmentForCCTSSmokeTesting() {
        String assignedIdentifier = "SMOKE_TEST";
        String nciCode = "NCI";
        String userName = "psc";
        //String userName = "ccts@mail.nih.gov";

        Study study = studyDao.getByAssignedIdentifier(assignedIdentifier);
        if (study != null) {
            logger.debug(String.format("Deleting study  %s", assignedIdentifier));
        } else {
            logger.error(String.format("no study found for given identifier %s", assignedIdentifier));
            return;
        }

        Site site = siteDao.getByAssignedIdentifier(nciCode);
        if (site == null) {
            String message = "No site exists for given assigned identifier" + nciCode;
            logger.error(message);

            return;
        }
        User user = userDao.getByName(userName);
        if (user == null) {
            String message = userName + " user does not exists";
            logger.error(message);

            return;

        }

        templateDevelopmentService.deleteDevelopmentAmendmentOnly(study);
        logger.debug(String.format("deleted development amendment for study ", assignedIdentifier));

        StudySite studySite = study.getStudySite(site);

        UserRole userRole = user.getUserRole(Role.SITE_COORDINATOR);
        if (userRole != null) {
            userRole.removeStudySite(studySite);
            userRoleDao.save(userRole);

        }
        userRole = user.getUserRole(Role.SUBJECT_COORDINATOR);
        if (userRole != null) {
            userRole.removeStudySite(studySite);
            userRoleDao.save(userRole);

        }


        studySite.getStudy().getStudySites().remove(studySite);
        studySite.getSite().getStudySites().remove(studySite);

        studySiteDao.delete(studySite);

        populationService.delete(study.getPopulations());
        if (study.getAmendment() != null) {
            for (Delta<?> delta : study.getAmendment().getDeltas()) {
                deltaService.delete(delta);
            }


            amendmentDao.delete(study.getAmendment());

            logger.debug(String.format("deleted released amendment for study ", assignedIdentifier));

        }
        templateService.delete(study.getPlannedCalendar());
        studyDao.delete(study);
        logger.debug(String.format("deleted study ", assignedIdentifier));


    }


    /**
     * this method is added only for testing purpose and for validating the testRollBackCCTSStudy() logic.
     *
     * @throws Exception
     */
    public void createStudy() throws Exception {
        if (studyDao.getByAssignedIdentifier(assignedIdentifier) == null) {
            logger.debug("creating study for given identifer:" + assignedIdentifier);
            Study study = TemplateSkeletonCreatorImpl.createBase(shortTitle);
            study.setAssignedIdentifier(assignedIdentifier);
            study.setLongTitle(longTitle);

            Site site = siteDao.getByAssignedIdentifier(nciCode);
            if (site == null) {
                String message = "No site exists for given assigned identifier" + nciCode;
                logger.error(message);

                return;
            }
            StudySite studySite = new StudySite();
            studySite.setSite(site);
            studySite.setStudy(study);
            study.addStudySite(studySite);

            TemplateSkeletonCreatorImpl.addEpoch(study, 0, Epoch.create("Treatment"));


            studyService.save(study);
            logger.info("Created the study :" + study.getId());
            commitAndStartNewTransaction();

        } else {
            logger.debug("study already exists for given identifier : " + assignedIdentifier);
        }
    }


    protected String[] getConfigLocations() {

        String[] configs = {"classpath:applicationContext-studyConsumer-grid.xml"};


        return configs;
    }

    protected void onSetUpInTransaction() throws Exception {

        DataAuditInfo.setLocal(new DataAuditInfo("test", "localhost", new Date(), "/wsrf/services/cagrid/StudyConsumer"));


    }

    protected void onTearDownAfterTransaction() throws Exception {

        DataAuditInfo.setLocal(null);

    }


    private void commitAndStartNewTransaction() {
        setComplete();
        endTransaction();
        startNewTransaction();

    }

    @Required
    public void setTemplateDevelopmentService(
			TemplateDevelopmentService templateDevelopmentService) {
		this.templateDevelopmentService = templateDevelopmentService;
	}

    @Required
    public void setStudyService(StudyService studyService) {
        this.studyService = studyService;
    }

    @Required
    public void setSiteDao(SiteDao siteDao) {
        this.siteDao = siteDao;
    }


    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    @Required

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    @Required

    public void setDeltaService(DeltaService deltaService) {
        this.deltaService = deltaService;
    }

    @Required

    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

    @Required

    public void setAmendmentDao(AmendmentDao amendmentDao) {
        this.amendmentDao = amendmentDao;
    }

    @Required

    public void setPopulationService(PopulationService populationService) {
        this.populationService = populationService;
    }

    @Required

    public void setUserRoleDao(UserRoleDao userRoleDao) {
        this.userRoleDao = userRoleDao;
    }
}