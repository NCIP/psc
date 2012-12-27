/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityState;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.nwu.bioinformatics.commons.DateUtils;
import gov.nih.nci.cabig.ctms.domain.DomainObjectTools;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase.*;
import static edu.nwu.bioinformatics.commons.testing.CoreTestCase.assertSameDay;

/**
 * @author Rhett Sutphin
 */
public class ScheduledCalendarDaoTest extends ContextDaoTestCase<ScheduledCalendarDao> {
    private SubjectDao subjectDao
            = (SubjectDao) getApplicationContext().getBean("subjectDao");
    private StudySegmentDao studySegmentDao
            = (StudySegmentDao) getApplicationContext().getBean("studySegmentDao");
    private AmendmentDao amendmentDao
        = (AmendmentDao) getApplicationContext().getBean("amendmentDao");
    private StudySubjectAssignmentDao assignmentDao
        = (StudySubjectAssignmentDao) getApplicationContext().getBean("studySubjectAssignmentDao");
    private StudyDao studyDao
        = (StudyDao) getApplicationContext().getBean("studyDao");

    public void testGetById() throws Exception {
        ScheduledCalendar cal = getDao().getById(-20);
        assertScheduledCalendar20(cal);
    }

    private void assertScheduledCalendar20(ScheduledCalendar cal) {
        assertEquals("Wrong assignment", -1, (int) cal.getAssignment().getId());

        assertEquals("Wrong number of study segments", 2, cal.getScheduledStudySegments().size());
        assertEquals("Wrong studySegment 0", -22, (int) cal.getScheduledStudySegments().get(0).getId());
        assertEquals("Wrong studySegment 1", -21, (int) cal.getScheduledStudySegments().get(1).getId());

        assertEquals("Wrong number of events in studySegment 0", 5, cal.getScheduledStudySegments().get(0).getActivities().size());
        assertEquals("Wrong number of events in studySegment 1", 0, cal.getScheduledStudySegments().get(1).getActivities().size());

        ScheduledActivity event = cal.getScheduledStudySegments().get(0).getActivities().get(0);
        assertEquals("Wrong event", -10, (int) event.getId());
        assertEquals("Wrong base event for event", -6, (int) event.getPlannedActivity().getId());
        assertDayOfDate("Wrong ideal date", 2006, Calendar.OCTOBER, 31, event.getIdealDate());
        assertEquals("Wrong notes", "Boo!", event.getNotes());
        assertEquals("Wrong amendment", -17, (int) event.getSourceAmendment().getId());
        assertEquals("Wrong rep number", 3, (int) event.getRepetitionNumber());

        ScheduledActivityState currentState = event.getCurrentState();
        assertEquals(ScheduledActivityMode.OCCURRED, currentState.getMode());
        assertDayOfDate("Wrong current state date", 2006, Calendar.OCTOBER, 25, currentState.getDate());
        assertEquals("Wrong current state mode", ScheduledActivityMode.OCCURRED, currentState.getMode());
        assertEquals("Wrong current state reason", "Success", currentState.getReason());

        List<ScheduledActivityState> states = cal.getScheduledStudySegments().get(0).getActivities().get(0).getAllStates();
        assertEquals("Wrong number of states", 4, states.size());
        assertEventState(-11, ScheduledActivityMode.SCHEDULED, "Initial input", DateUtils.createDate(2006, Calendar.OCTOBER, 22), states.get(0));
        assertEventState(-12, ScheduledActivityMode.CANCELED,  "Called to cancel", null, states.get(1));
        assertEventState(-13, ScheduledActivityMode.SCHEDULED, "Called to reschedule", DateUtils.createDate(2006, Calendar.OCTOBER, 25), states.get(2));
        assertEventState(null, ScheduledActivityMode.OCCURRED, "Success", DateUtils.createDate(2006, Calendar.OCTOBER, 25), states.get(3));
    }

    public void testChangeStateAndSave() throws Exception {
        {
            ScheduledCalendar cal = getDao().getById(-20);
            cal.getScheduledStudySegments().get(0).getActivities().get(0).changeState(ScheduledActivityMode.CANCELED.createStateInstance(DateUtils.createDate(2006, Calendar.OCTOBER, 25), "For great victory"));
            getDao().save(cal);
        }

        interruptSession();

        {
            ScheduledCalendar loaded = getDao().getById(-20);
            List<ScheduledActivityState> states = loaded.getScheduledStudySegments().get(0).getActivities().get(0).getAllStates();
            assertEquals("Wrong number of states", 5, states.size());
            // second to last should now have an ID
            assertNotNull(states.get(3).getId());
            assertEventState(states.get(3).getId(), ScheduledActivityMode.OCCURRED, "Success", DateUtils.createDate(2006, Calendar.OCTOBER, 25), states.get(3));

            assertEventState(null, ScheduledActivityMode.CANCELED, "For great victory", null, states.get(4));
        }
    }

    private void assertEventState(
        Integer expectedId, ScheduledActivityMode expectedMode, String expectedReason,
        Date expectedDate, ScheduledActivityState actualState
    ) {
        assertEquals("Wrong ID", expectedId, actualState.getId());
        assertEquals("Wrong mode", expectedMode, actualState.getMode());
        assertEquals("Wrong reason", expectedReason, actualState.getReason());
        if (expectedDate != null) assertSameDay("Wrong date", expectedDate, actualState.getDate());
    }

    public void testSave() throws Exception {
        int savedId;

        Date expectedIdealDate = DateUtils.createDate(2006, Calendar.SEPTEMBER, 20);
        Date expectedActualDate = DateUtils.createDate(2006, Calendar.SEPTEMBER, 22);
        ScheduledActivityMode expectedMode = ScheduledActivityMode.OCCURRED;
        String expectedReason = "All done";
        Activity expectedActivity = Fixtures.setId(-100, Fixtures.createNamedInstance("Infusion", Activity.class));
        expectedActivity.setVersion(0);
        Amendment expectedAmendment = amendmentDao.getById(-17);

        {
            Subject subject = subjectDao.getById(-1);

            ScheduledCalendar calendar = new ScheduledCalendar();
            calendar.setAssignment(assignmentDao.getById(-2));
            StudySegment studySegment4 = studySegmentDao.getById(-4);
            StudySegment studySegment3 = studySegmentDao.getById(-3);
            calendar.addStudySegment(Fixtures.createScheduledStudySegment(studySegment4));
            calendar.addStudySegment(Fixtures.createScheduledStudySegment(studySegment3));
            ScheduledStudySegment lastScheduledStudySegment = Fixtures.createScheduledStudySegment(studySegment4);
            calendar.addStudySegment(lastScheduledStudySegment);

            ScheduledActivity event = new ScheduledActivity();
            event.setIdealDate(expectedIdealDate);
            event.setPlannedActivity(studySegment3.getPeriods().iterator().next().getPlannedActivities().get(0));
            event.changeState(ScheduledActivityMode.OCCURRED.createStateInstance(expectedActualDate, expectedReason));
            event.setActivity(expectedActivity);
            event.setSourceAmendment(expectedAmendment);
            lastScheduledStudySegment.addEvent(event);

            assertNull(calendar.getId());
            getDao().save(calendar);
            assertNotNull("Saved calendar not assigned an ID", calendar.getId());
            savedId = calendar.getId();
        }

        interruptSession();

        ScheduledCalendar reloaded = getDao().getById(savedId);
        assertEquals("Wrong assignment", -2, (int) reloaded.getAssignment().getId());
        assertEquals("Wrong number of study segments: " + reloaded.getScheduledStudySegments(), 3, reloaded.getScheduledStudySegments().size());
        assertEquals("Wrong study segment 0", -4, (int) reloaded.getScheduledStudySegments().get(0).getStudySegment().getId());
        assertEquals("Wrong study segment 1", -3, (int) reloaded.getScheduledStudySegments().get(1).getStudySegment().getId());
        assertEquals("Wrong study segment 2", -4, (int) reloaded.getScheduledStudySegments().get(2).getStudySegment().getId());

        assertEquals("Wrong number of events for last studySegment", 1, reloaded.getScheduledStudySegments().get(2).getActivities().size());
        ScheduledActivity loadedEvent = reloaded.getScheduledStudySegments().get(2).getActivities().get(0);
        assertSameDay("Wrong ideal date", expectedIdealDate, loadedEvent.getIdealDate());
        assertEquals("Wrong planned event", -7, (int) loadedEvent.getPlannedActivity().getId());

        ScheduledActivityState currentState = loadedEvent.getCurrentState();
        assertEquals(ScheduledActivityMode.OCCURRED, currentState.getMode());
        assertSameDay("Wrong current state date", expectedActualDate, currentState.getDate());
        assertEquals("Wrong current state mode", expectedMode, currentState.getMode());
        assertEquals("Wrong current state reason", expectedReason, currentState.getReason());

        Activity currentActivity = reloaded.getScheduledStudySegments().get(2).getActivities().get(0).getActivity();
        assertNotNull("Activity null", currentActivity);
        assertEquals("Wrong Activity", expectedActivity.getName(), currentActivity.getName());
    }

    public void testGetAllForStudy() throws Exception {
        Collection<ScheduledCalendar> matches = getDao().getAllFor(studyDao.getById(-2));
        assertEquals("Wrong number of matches", 2, matches.size());
        Collection<Integer> matchIds = DomainObjectTools.collectIds(matches);
        assertContains(matchIds, -201);
        assertContains(matchIds, -203);
    }

    public void testInitialize() throws Exception {
        ScheduledCalendar cal = getDao().getById(-20);
        getDao().initialize(cal);
        interruptSession();

        assertScheduledCalendar20(cal);
    }
}
