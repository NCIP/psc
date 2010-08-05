package edu.northwestern.bioinformatics.studycalendar.grid;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SubjectDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.test.AbstractTransactionalSpringContextTests;

import java.util.Date;

/**
 *Test class added to validate the clean scripts that were added for CCTS roll-back script requirement
 *
 * @author Saurabh Agrawal
 */
public class PSCRegistrationConsumerMessgeRollbackTest extends AbstractTransactionalSpringContextTests {

    private StudySubjectAssignmentDao studySubjectAssignmentDao;

    private SubjectService subjectService;

    private StudyDao studyDao;

    private SubjectDao subjectDao;

    public static final Log logger = LogFactory.getLog(PSCRegistrationConsumerMessgeRollbackTest.class);

    private String assignmentGridId = "6115c43c-851e-425c-8312-fd78367aaef3"; //John Smith
    private String assignedIdentifier = "SMOKE_TEST";

    private String nciCode = "NCI";
    private String subjectGridId = "91dd4580-801b-4874-adeb-a174361bacea";


    public void testRollBackRegistrationMessage() {
//        StudySubjectAssignment studySubjectAssignment = studySubjectAssignmentDao.getByGridId(assignmentGridId);
//        if (studySubjectAssignment != null) {
//            logger.debug(String.format("Deleting assignment for the subject %s", studySubjectAssignment.getSubject().getFullName()));
//            Subject subject = studySubjectAssignment.getSubject();
//            subject.getAssignments().clear();
//            subjectDao.save(subject);
//
//            commitAndStartNewTransaction();
////            studySubjectAssignment = studySubjectAssignmentDao.getByGridId(assignmentGridId);
////            assertNull("assignment must be null for given grid id:" + assignmentGridId, studySubjectAssignment);
////            logger.info(String.format("Sucessfully deleted assignment for given grid id %s", assignmentGridId));
//
//
//        } else {
////            logger.error(String.format("no assignment found for given grid id %s", assignmentGridId));
//            createSubjectAssigment();
//        }


    }

    public void createSubjectAssigment() {
        if (studySubjectAssignmentDao.getByGridId(assignmentGridId) == null) {
            logger.debug("in createSubjectAssigment method");


            Study study = studyDao.getByAssignedIdentifier(assignedIdentifier);

            if (study == null) {
                String message = "Study identified by assigned Identifier '" + assignedIdentifier + "' doesn't exist";
                logger.error(message);
                return;
            }

            StudySite studySite = findStudySite(study, nciCode);
            if (studySite == null) {

                String message = "The study '" + study.getLongTitle() + "', identified by assigned Identifier '" + assignedIdentifier
                        + "' is not associated to a site identified by NCI code :'" + nciCode + "'";
                logger.error(message);

                return;

            }

            Subject subject = subjectDao.getByGridId(subjectGridId);

            if (subject == null) {
                subject = new Subject();
                subject.setGridId(subjectGridId);
                subject.setGender(Gender.MALE);

                subject.setDateOfBirth(new Date());
                subject.setFirstName("first name");
                subject.setLastName(" last name");
                subject.setPersonId("1234");

                subjectDao.save(subject);
                logger.debug("created subject: " + subject.getId());

                commitAndStartNewTransaction();

            } else {
                logger.debug(String.format("subject %s found for given grid id %s", subject.getFullName(), subjectGridId));

            }
            StudySegment loadedStudySegment = null;
            try {
                loadedStudySegment = study.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(0);
            } catch (Exception e) {
                String message = "The study '" + study.getLongTitle() + "', identified by Coordinating Center Identifier '" + assignedIdentifier
                        + "' does not have any arm'";
                logger.error(message);
                return;

            }
            StudySubjectAssignment newAssignment = null;
            try {
                logger.debug("creating subject assignment");

                newAssignment = subjectService.assignSubject(subject, studySite, loadedStudySegment,
                        new Date(), assignmentGridId, null);
                commitAndStartNewTransaction();

                ScheduledCalendar scheduledCalendar = newAssignment.getScheduledCalendar();
                logger.debug("Created assignment " + scheduledCalendar.getId());

            } catch (StudyCalendarSystemException exp) {
                logger.error("Error crating assignment. " + exp.getMessage());

            }

        } else {
            logger.debug("assignment already exists for given grid id");
        }
    }

    private StudySite findStudySite(final Study study, final String siteNCICode) {
        for (StudySite studySite : study.getStudySites()) {
            if (StringUtils.equals(studySite.getSite().getAssignedIdentifier(), siteNCICode)) {
                return studySite;
            }
        }
        return null;


    }

    protected String[] getConfigLocations() {

        String[] configs = {"classpath:applicationContext-grid.xml"};


        return configs;
    }

    protected void onSetUpInTransaction() throws Exception {

        DataAuditInfo.setLocal(new DataAuditInfo("test", "localhost", new Date(), "/wsrf/services/cagrid/RegistrationConsumer"));


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
    public void setStudySubjectAssignmentDao(StudySubjectAssignmentDao studySubjectAssignmentDao) {
        this.studySubjectAssignmentDao = studySubjectAssignmentDao;
    }

    @Required

    public void setSubjectService(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    @Required
    public void setSubjectDao(SubjectDao subjectDao) {
        this.subjectDao = subjectDao;
    }
}