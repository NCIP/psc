package edu.northwestern.bioinformatics.studycalendar.service;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Canceled;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Conditional;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.NotApplicable;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Occurred;
import edu.northwestern.bioinformatics.studycalendar.core.*;
import edu.northwestern.bioinformatics.studycalendar.xml.domain.NextScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySegmentDao;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import gov.nih.nci.cabig.ctms.lang.DateTools;

import java.util.Calendar;

import static org.easymock.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
public class ScheduleServiceTest extends StudyCalendarTestCase {
    private static final String REVISION_DISPLAY_NAME = "10/01/1926 (Leopard)";

    private ScheduleService service;
    private SubjectService subjectService;

    private ScheduledStudySegment scheduledStudySegment;
    private Site site;
    private Amendment amendment;
    private StudySegmentDao studySegmentDao;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Just so there's a non-null chain from event to Site
        site = createNamedInstance("The Sun", Site.class);
        Study study = createBasicTemplate();
        StudySubjectAssignment assignment
            = createAssignment(study, site, createSubject("Alice", "Wonder"));
        scheduledStudySegment = new ScheduledStudySegment();
        assignment.getScheduledCalendar().addStudySegment(scheduledStudySegment);

        amendment = createAmendments("Leopard");
        amendment.setDate(DateTools.createDate(1926, Calendar.OCTOBER, 1));

        service = new ScheduleService();
        // this is a real instance instead of mock because eventually
        // some or all of the methods invoked by SS on PS are going to
        // be moved into SS.
        subjectService = new SubjectService();
        service.setSubjectService(subjectService);

        studySegmentDao = registerDaoMockFor(StudySegmentDao.class);
        service.setStudySegmentDao(studySegmentDao);
    }

    public void testReviseDateForScheduledScheduledActivity() throws Exception {
        ScheduledActivity event = createScheduledActivity("DC", 2004, Calendar.APRIL, 1);
        scheduledStudySegment.addEvent(event);

        service.reviseDate(event, 7, amendment);
        assertEquals(2, event.getAllStates().size());
        assertEquals("Shifted forward 7 days in revision " + REVISION_DISPLAY_NAME, event.getCurrentState().getReason());
        assertEquals(ScheduledActivityMode.SCHEDULED, event.getCurrentState().getMode());
        assertDayOfDate(2004, Calendar.APRIL, 8, event.getActualDate());
    }

    public void testReviseDateForConditionalScheduledActivity() throws Exception {
        ScheduledActivity event = createScheduledActivity("DC", 2004, Calendar.APRIL, 24,
            new Conditional("DC", DateTools.createDate(2004, Calendar.APRIL, 30)));
        scheduledStudySegment.addEvent(event);

        service.reviseDate(event, -7, amendment);
        assertEquals(3, event.getAllStates().size());
        assertEquals("Shifted back 7 days in revision " + REVISION_DISPLAY_NAME, event.getCurrentState().getReason());
        assertEquals(ScheduledActivityMode.CONDITIONAL, event.getCurrentState().getMode());
        assertDayOfDate(2004, Calendar.APRIL, 23, event.getActualDate());
    }

    public void testReviseConditionForConditionalScheduledActivity() throws Exception {
        ScheduledActivity event = createScheduledActivity("DC 1", 2004, Calendar.APRIL, 30,
            new Conditional("DC 2", DateTools.createDate(2004, Calendar.APRIL, 30)));
        scheduledStudySegment.addEvent(event);

        service.reviseDate(event, 0, amendment);
        assertEquals(3, event.getAllStates().size());
        assertEquals("State change in revision " + REVISION_DISPLAY_NAME, event.getCurrentState().getReason());
        assertEquals(ScheduledActivityMode.CONDITIONAL, event.getCurrentState().getMode());
        assertEquals("Wrong state of the event", ScheduledActivityMode.CONDITIONAL, event.getCurrentState().getMode());
    }

    public void testReviseDateForOccurredScheduledActivity() throws Exception {
        ScheduledActivity event = createScheduledActivity("DC", 2004, Calendar.APRIL, 24,
            new Occurred("DC", DateTools.createDate(2004, Calendar.APRIL, 30)));
        scheduledStudySegment.addEvent(event);

        service.reviseDate(event, -7, amendment);
        assertEquals(2, event.getAllStates().size());
        assertEquals(ScheduledActivityMode.OCCURRED, event.getCurrentState().getMode());
        assertDayOfDate(2004, Calendar.APRIL, 30, event.getActualDate());
    }

    public void testReviseDateForCanceledScheduledActivity() throws Exception {
        ScheduledActivity event = createScheduledActivity("DC", 2004, Calendar.APRIL, 24,
            new Canceled("DC",DateTools.createDate(2004, Calendar.APRIL, 24)));
        scheduledStudySegment.addEvent(event);

        service.reviseDate(event, -7, amendment);
        assertEquals(2, event.getAllStates().size());
        assertEquals(ScheduledActivityMode.CANCELED, event.getCurrentState().getMode());
        assertDayOfDate(2004, Calendar.APRIL, 24, event.getActualDate());
    }

    public void testReviseDateForNotApplicableScheduledActivity() throws Exception {
        ScheduledActivity event = createScheduledActivity("DC", 2004, Calendar.APRIL, 24,
            new NotApplicable("DC",DateTools.createDate(2004, Calendar.APRIL, 24)));
        scheduledStudySegment.addEvent(event);

        service.reviseDate(event, -7, amendment);
        assertEquals(2, event.getAllStates().size());
        assertEquals(ScheduledActivityMode.NOT_APPLICABLE, event.getCurrentState().getMode());
        assertDayOfDate(2004, Calendar.APRIL, 24, event.getActualDate());
    }

    public void testReviseDateForScheduledScheduledActivityAvoidsBlackouts() throws Exception {
        WeekdayBlackout noThursdays = new WeekdayBlackout();
        noThursdays.setDayOfTheWeek("Thursday");
        site.getBlackoutDates().add(noThursdays);

        ScheduledActivity event = createScheduledActivity("DC", 2007, Calendar.OCTOBER, 2);
        scheduledStudySegment.addEvent(event);

        service.reviseDate(event, 2, amendment);

        assertEquals(ScheduledActivityMode.SCHEDULED, event.getCurrentState().getMode());
        assertDayOfDate(2007, Calendar.OCTOBER, 5, event.getActualDate());
        assertEquals(3, event.getAllStates().size());
    }

    public void testResolveNextScheduledStudySegmentWhenStudySegmentFound() throws Exception {
        NextScheduledStudySegment scheduled = createNextScheduledStudySegment();
        StudySegment existingSegment = setGridId("segment-grid0", new StudySegment());
        existingSegment.setId(1);

        assertNull("StudySegment is newly created",scheduled.getStudySegment().getId());
        expect(studySegmentDao.getByGridId("segment-grid0")).andReturn(existingSegment);

        replayMocks();
        NextScheduledStudySegment actual = service.resolveNextScheduledStudySegment(scheduled);
        verifyMocks();

        assertNotNull("Existing StudySegment is not set", actual.getStudySegment().getId());
        assertSame("StudySegment is not same", existingSegment, actual.getStudySegment());
    }

    public void testResolveNextScheduledStudySegmentWhenNoStudySegmentFound() throws Exception {
        NextScheduledStudySegment scheduled = createNextScheduledStudySegment();
        expect(studySegmentDao.getByGridId("segment-grid0")).andReturn(null);

        replayMocks();
        try {
            service.resolveNextScheduledStudySegment(scheduled);
            fail("Exception not thrown");
        } catch (StudyCalendarValidationException scve) {
            assertEquals("Segment with grid Identifier segment-grid0 not found.", scve.getMessage());
        }
    }

    //Helper Method
    private NextScheduledStudySegment createNextScheduledStudySegment() {
        NextScheduledStudySegment scheduledSegment = new NextScheduledStudySegment();
        scheduledSegment.setStartDay(2);
        scheduledSegment.setStartDate(DateTools.createDate(2010, Calendar.APRIL, 24));
        scheduledSegment.setStudySegment(setGridId("segment-grid0", new StudySegment()));
        scheduledSegment.setMode(NextStudySegmentMode.PER_PROTOCOL);
        return scheduledSegment;
    }
}
