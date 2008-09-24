package edu.northwestern.bioinformatics.studycalendar.grid;

import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SubjectDao;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.api.ScheduledCalendarService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import org.springframework.test.AbstractTransactionalSpringContextTests;
import org.springframework.beans.factory.annotation.Required;
import gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo;
import gov.nih.nci.cabig.ctms.grid.ae.stubs.types.RegistrationFailed;

import java.util.Date;

/**
 * Test class added to validate the clean scripts that were added for CCTS roll-back script requirement
 *
 * @author Saurabh Agrawal
 */
public class PSCAdverseEventMessgeRollbackTest extends AbstractTransactionalSpringContextTests {

    private StudySubjectAssignmentDao studySubjectAssignmentDao;


    private final Log logger = LogFactory.getLog(getClass());

    private String assignmentGridId = "6115c43c-851e-425c-8312-fd78367aaef3";  //John Doe

    private ScheduledCalendarService scheduledCalendarService;


    public void testRollBackAeNotifications() {
        StudySubjectAssignment studySubjectAssignment = studySubjectAssignmentDao.getByGridId(assignmentGridId);
        if (studySubjectAssignment != null) {
            logger.debug(String.format("Deleting subject's %s ae notifications", studySubjectAssignment.getSubject().getFullName()));
            studySubjectAssignment.getNotifications().clear();
            studySubjectAssignmentDao.save(studySubjectAssignment);
           commitAndStartNewTransaction();
            studySubjectAssignment = studySubjectAssignmentDao.getByGridId(assignmentGridId);
            assertTrue("must not have any notificaitons.", studySubjectAssignment.getNotifications().isEmpty());
            logger.debug(String.format("Sucessfully deleted subject's %s ae notifications", studySubjectAssignment.getSubject().getFullName()));

          createAeNotification();

        } else {
            logger.error(String.format("no assignment found for given grid id %s", assignmentGridId));

        }


    }

    /**
     * this method added to test the logig of testRollBackAeNotifications. Do not call this method unless you are editing the testRollBackAeNotifications method
     */
    public void createAeNotification() {
        logger.debug(String.format("adding ae notifications for assignment %s", assignmentGridId));

        StudySubjectAssignment assignment = studySubjectAssignmentDao.getByGridId(assignmentGridId);

        if (assignment != null) {
            logger.debug(String.format("creating ae notification for subject %s ", assignment.getSubject().getFullName()));

            AdverseEvent event = new AdverseEvent();
            event.setDescription("test desc");
            event.setDetectionDate(new Date());

            try {
                scheduledCalendarService.registerSevereAdverseEvent(assignment, event);
                commitAndStartNewTransaction();
            }
            catch (Exception ex) {
                logger.error("Error registering adverse event: " + ex.getMessage(), ex);
            }
        } else {
            logger.error(String.format("Error adding adverse events because no assignment found for given grid id %s", assignmentGridId));

        }


    }

    protected String[] getConfigLocations() {

        String[] configs = {"classpath:applicationContext-grid-ae.xml"};


        return configs;
    }

    protected void onSetUpInTransaction() throws Exception {

        DataAuditInfo.setLocal(new gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo("test", "localhost", new Date(), "/wsrf/services/cagrid/AdverseEventConsumer"));


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
    public void setScheduledCalendarService(ScheduledCalendarService scheduledCalendarService) {
        this.scheduledCalendarService = scheduledCalendarService;
    }
}
