/**
 *
 */
package edu.northwestern.bioinformatics.studycalendar.grid;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateSkeletonCreatorImpl;
import gov.nih.nci.ccts.grid.*;
import gov.nih.nci.ccts.grid.common.StudyConsumerI;
import gov.nih.nci.ccts.grid.studyconsumer.stubs.types.InvalidStudyException;
import gov.nih.nci.ccts.grid.studyconsumer.stubs.types.StudyCreationException;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 * Assumptions: 1. Site should already be existing in DB..
 *
 * @author <a href="mailto:saurabh.agrawal@semanticbits.com>Saurabh Agrawal</a>
 */
public class PSCStudyConsumer implements StudyConsumerI {

    private static final Log logger = LogFactory.getLog(PSCStudyConsumer.class);

    public static final String SERVICE_BEAN_NAME = "scheduledCalendarService";

    private static final String COORDINATING_CENTER_IDENTIFIER_TYPE = "Coordinating Center Identifier";

    private ApplicationContext ctx;

    private SiteDao siteDao;

    private StudyService studyService;

    private StudyDao studyDao;

    public PSCStudyConsumer() {
        ctx = new ClassPathXmlApplicationContext(new String[]{
                // "classpath:applicationContext.xml",
                "classpath:applicationContext-api.xml", "classpath:applicationContext-command.xml",
                "classpath:applicationContext-dao.xml", "classpath:applicationContext-db.xml",
                "classpath:applicationContext-security.xml", "classpath:applicationContext-service.xml",
                "classpath:applicationContext-spring.xml"});
        siteDao = (SiteDao) ctx.getBean("siteDao");
        studyService = (StudyService) ctx.getBean("studyService");
        studyDao = (StudyDao) ctx.getBean("studyDao");
    }

    public void createStudy(final gov.nih.nci.ccts.grid.Study studyDto) throws RemoteException, InvalidStudyException,
            StudyCreationException {
        if (studyDto == null) {
            String message = "No Study message was found";
            throw getInvalidStudyException(message);
        }

        String ccIdentifier = findCoordinatingCenterIdentifier(studyDto);

        if (studyDao.getStudyIdByAssignedIdentifier(ccIdentifier) != null) {
            logger.debug("Already a study with the same Coordinating Center Identifier (" + ccIdentifier
                    + ") exists.Returning without processing the request.");
            return;
        }

        Study study = TemplateSkeletonCreatorImpl.createBase(studyDto.getShortTitleText());
        study.setAssignedIdentifier(ccIdentifier);
        study.setGridId(studyDto.getGridId());
        study.setLongTitle(studyDto.getLongTitleText());

        StudyOrganizationType[] studyOrganizationTypes = studyDto.getStudyOrganization();
        populateStudySite(study, studyOrganizationTypes);

        // now add epochs and arms to the planned calendar of study
        populateEpochsAndArms(studyDto, study);

        studyService.save(study);
        logger.info("Created the study :" + study.getId());

    }

    /**
     * does nothing as we are already  commiting Study message by default.
     *
     * @param studyDto
     * @throws RemoteException
     * @throws InvalidStudyException
     */
    public void commit(final gov.nih.nci.ccts.grid.Study studyDto) throws RemoteException, InvalidStudyException {
//        if (studyDto == null) {
//            throw new InvalidStudyException();
//        }
//        logger.info("commit called for study:long titlte-" + studyDto.getLongTitleText());
//        String ccIdentifier = findCoordinatingCenterIdentifier(studyDto);
//
//        try {
//            studyDao.commitInProgressStudy(ccIdentifier);
//        }
//
//        catch (Exception exp) {
//            logger.error("Exception while trying to commit the study", exp);
//            InvalidStudyException invalidStudyException = new InvalidStudyException();
//            invalidStudyException.setFaultReason("Exception while comitting study," + exp.getMessage());
//            invalidStudyException.setFaultString("Exception while comitting study," + exp.getMessage());
//            throw invalidStudyException;
//        }
    }

    public void rollback(final gov.nih.nci.ccts.grid.Study studyDto) throws RemoteException, InvalidStudyException {
        if (studyDto == null) {
            String message = "No Study message was found";
            throw getInvalidStudyException(message);
        }
        logger.info("rollback called for study:long titlte-" + studyDto.getLongTitleText());
        String ccIdentifier = findCoordinatingCenterIdentifier(studyDto);

        try {
            Study study = studyService.getStudyByAssignedIdentifier(ccIdentifier);
            studyDao.delete(study);
        }
        catch (Exception exp) {
            String message = "Exception while rollback study," + exp.getMessage();
            throw getInvalidStudyException(message);
        }
    }

    private void populateEpochsAndArms(final gov.nih.nci.ccts.grid.Study studyDto, final Study study) {
        EpochType[] epochTypes = studyDto.getEpoch();

        for (int i = 0; i < epochTypes.length; i++) {
            EpochType epochType = epochTypes[i];
            if (epochType instanceof NonTreatmentEpochType) {
                TemplateSkeletonCreatorImpl.addEpoch(study, i, Epoch.create(epochType.getName()));
            } else if (epochType instanceof TreatmentEpochType) {
                TemplateSkeletonCreatorImpl.addEpoch(study, i,
                        createEpochForTreatmentEpochType((TreatmentEpochType) epochType));

            }

        }

    }

    private Epoch createEpochForTreatmentEpochType(final TreatmentEpochType treatmentEpochType) {
        Epoch epoch = null;

        ArmType[] armTypes = treatmentEpochType.getArm();
        List<String> armNames = new ArrayList<String>();
        for (ArmType armType : armTypes) {
            armNames.add(armType.getName());
        }
        epoch = Epoch.create(treatmentEpochType.getName(), armNames.toArray(new String[0]));
        return epoch;
    }

    /**
     * Populates study siste and returns it.
     *
     * @param study
     * @param studyOrganizationTypes
     * @throws InvalidStudyException
     */
    private void populateStudySite(final Study study, final StudyOrganizationType[] studyOrganizationTypes)
            throws StudyCreationException, InvalidStudyException {

        List<StudySite> studySites = new ArrayList<StudySite>();
        for (StudyOrganizationType studyOrganizationType : studyOrganizationTypes) {
            StudySite studySite = null;
            if (studyOrganizationType instanceof StudySiteType) {
                studySite = new StudySite();
                studySite.setSite(fetchSite(studyOrganizationType.getHealthcareSite(0).getNciInstituteCode()));
                studySite.setStudy(study);
                studySite.setGridId(studyOrganizationType.getGridId());
            }
            studySites.add(studySite);
        }
        if (studySites.size() == 0 || ArrayUtils.isEmpty(studyOrganizationTypes)) {
            String message = "No sites is associated to this study" + study.getLongTitle();
            throw getStudyCreationException(message);

        }
        study.setStudySites(studySites);
    }

    /**
     * Fetches the site from the DB
     *
     * @param assignedIdentifier
     * @return
     */
    private Site fetchSite(final String assignedIdentifier) throws StudyCreationException {

        Site site = siteDao.getByAssignedIdentifier(assignedIdentifier);
        if (site == null) {

            String message = "No site exists  assignedIdentifier :" + assignedIdentifier;
            throw getStudyCreationException(message);
        }

        return site;
    }

    /**
     * This method will return the identifier specified by Coordinating center to this study.
     *
     * @param studyDto
     * @return
     * @throws InvalidStudyException
     */
    String findCoordinatingCenterIdentifier(final gov.nih.nci.ccts.grid.Study studyDto) throws InvalidStudyException {
        String ccIdentifier = null;
        for (IdentifierType identifierType : studyDto.getIdentifier()) {
            if (identifierType instanceof OrganizationAssignedIdentifierType
                    && StringUtils.equals(identifierType.getType(), COORDINATING_CENTER_IDENTIFIER_TYPE)) {
                ccIdentifier = identifierType.getValue();
                break;
            }
        }

        if (ccIdentifier == null) {
            String message = "no cc identifier for study:long titlte-" + studyDto.getLongTitleText();
            throw getInvalidStudyException(message);
        }
        return ccIdentifier;

    }


    private StudyCreationException getStudyCreationException(final String message) {
        StudyCreationException studyCreationException = new StudyCreationException();
        studyCreationException.setFaultString(message);
        studyCreationException.setFaultReason(message);
        logger.error(message);
        return studyCreationException;
    }

    private InvalidStudyException getInvalidStudyException(final String message) throws InvalidStudyException {
        logger.error(message);
        InvalidStudyException invalidStudyException = new InvalidStudyException();
        invalidStudyException.setFaultReason(message);
        invalidStudyException.setFaultString(message);
        throw invalidStudyException;
    }


}
