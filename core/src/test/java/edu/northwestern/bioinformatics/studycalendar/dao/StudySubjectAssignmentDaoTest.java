package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.AdverseEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.Notification;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.nwu.bioinformatics.commons.DateUtils;
import edu.nwu.bioinformatics.commons.testing.CoreTestCase;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class StudySubjectAssignmentDaoTest extends ContextDaoTestCase<StudySubjectAssignmentDao> {
    private StudyDao studyDao;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        studyDao = (StudyDao) getApplicationContext().getBean("studyDao");
    }

    public void testGetAllAssignmentsWhichHaveNoActivityBeyondADate() throws Exception {
        Date date = DateUtils.createDate(2008, Calendar.MAY, 8);
        List<StudySubjectAssignment> assignments = getDao().getAllAssignmenetsWhichHaveNoActivityBeyondADate(date);
        assertEquals("there must  be 2 assignments which have no activities after 8th May", 2, assignments.size());

        assertTrue("Wrong assignment id", Integer.valueOf(-11).equals(assignments.get(0).getId()) || Integer.valueOf(-12).equals(assignments.get(0).getId()));
        assertTrue("Wrong assignment id", Integer.valueOf(-11).equals(assignments.get(1).getId()) || Integer.valueOf(-12).equals(assignments.get(1).getId()));
    }

    public void testGetById() throws Exception {
        StudySubjectAssignment assignment = getDao().getById(-10);

        assertEquals("Wrong id", -10, (int) assignment.getId());
        CoreTestCase.assertDayOfDate("Wrong start date", 2003, Calendar.FEBRUARY, 1,
                assignment.getStartDate());
        CoreTestCase.assertDayOfDate("Wrong end date", 2003, Calendar.SEPTEMBER, 1,
                assignment.getEndDate());
        assertEquals("Wrong subject", -20, (int) assignment.getSubject().getId());
        assertEquals("Wrong study site", -15, (int) assignment.getStudySite().getId());
        assertEquals("Wrong study id", "A", assignment.getStudySubjectId());
        assertEquals("Wrong current amendment", -18, (int) assignment.getCurrentAmendment().getId());
        assertEquals("Wrong number of populations", 1, assignment.getPopulations().size());
        assertEquals("Wrong population", -21, (int) assignment.getPopulations().iterator().next().getId());
        assertEquals("Wrong manager ID", -67, (int) assignment.getManagerCsmUserId());
    }

    public void testGetByGridId() throws Exception {
        StudySubjectAssignment assignment = getDao().getByGridId("NOT-SMALL1");
        assertNotNull(assignment);
        assertEquals("Wrong obj returned", -10, (int) assignment.getId());
    }

    public void testAesSaved() throws Exception {
        {
            StudySubjectAssignment assignment = getDao().getById(-10);
            assertEquals("Should already be one", 1, assignment.getNotifications().size());
            AdverseEvent event = new AdverseEvent();
            event.setDescription("Big bad");
            event.setDetectionDate(DateUtils.createDate(2006, Calendar.APRIL, 5));
            Notification notification = new Notification(event);

            assignment.addAeNotification(notification);
        }

        interruptSession();

        StudySubjectAssignment reloaded = getDao().getById(-10);
        assertEquals("Wrong number of notifications", 2, reloaded.getNotifications().size());
        Notification notification = reloaded.getNotifications().get(1);
        assertNotNull(notification.getId());
        assertFalse(notification.isDismissed());
        assertEquals("Big bad", notification.getMessage());
        //CoreTestCase.assertDayOfDate(2006, Calendar.APRIL, 5, notification.getAdverseEvent().getDetectionDate());
    }

    public void testGetByStudySubjectIdentifier() throws Exception {
        StudySubjectAssignment ssa = getDao().getByStudySubjectIdentifier(studyDao.getById(-101), "B");
        assertNotNull(ssa);
        assertEquals("Wrong SSA found", -13, (int) ssa.getId());
    }

    public void testGetAssignmentsInIntersection() throws Exception {
        List<StudySubjectAssignment> actual = getDao().
            getAssignmentsInIntersection(Arrays.asList(-101), Arrays.asList(-200));

        assertEquals("Wrong number of assignments found", 1, actual.size());
        assertEquals("Wrong assignment found", -13, (int) actual.get(0).getId());
    }

    public void testGetAssignmentsInIntersectionWithNullStudies() throws Exception {
        List<StudySubjectAssignment> actual = getDao().
            getAssignmentsInIntersection(null, Arrays.asList(-200));

        assertEquals("Wrong number of assignments found", 4, actual.size());
    }

    public void testGetAssignmentsInIntersectionWithNullSites() throws Exception {
        List<StudySubjectAssignment> actual = getDao().
            getAssignmentsInIntersection(Arrays.asList(-100), null);

        assertEquals("Wrong number of assignments found", 3, actual.size());
    }

    public void testGetAssignmentsInIntersectionForUnknownSite() throws Exception {
        List<StudySubjectAssignment> actual = getDao().
            getAssignmentsInIntersection(Arrays.asList(-101), Arrays.asList(-20000));

        assertEquals("Wrong number of assignments found", 0, actual.size());
    }

    public void testGetAssignmentsInIntersectionForUnknownStudy() throws Exception {
        List<StudySubjectAssignment> actual = getDao().
            getAssignmentsInIntersection(Arrays.asList(-10100), Arrays.asList(-200));

        assertEquals("Wrong number of assignments found", 0, actual.size());
    }

    public void testGetAssignmentsByManager() throws Exception {
        List<StudySubjectAssignment> actual =
            getDao().getAssignmentsByManagerCsmUserId(-8901);

        assertEquals("Wrong number of assignments found", 1, actual.size());
        assertEquals("Wrong assignment found", -12, (int) actual.get(0).getId());
    }
}
