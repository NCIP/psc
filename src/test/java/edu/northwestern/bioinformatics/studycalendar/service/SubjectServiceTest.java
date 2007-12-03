package edu.northwestern.bioinformatics.studycalendar.service;

import edu.nwu.bioinformatics.commons.DateUtils;
import edu.nwu.bioinformatics.commons.testing.CoreTestCase;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.SubjectDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Scheduled;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Occurred;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Canceled;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;

import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import static org.easymock.classextension.EasyMock.*;

/**
 * @author Rhett Sutphin
 */
public class SubjectServiceTest extends StudyCalendarTestCase {
    private SubjectService service;

    private SubjectDao subjectDao;
    private AmendmentService amendmentService;

    private User user;

    private StudySegment studySegment;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        subjectDao = registerDaoMockFor(SubjectDao.class);
        amendmentService = registerMockFor(AmendmentService.class);

        service = new SubjectService();
        service.setSubjectDao(subjectDao);
        service.setAmendmentService(amendmentService);

        Epoch epoch = Epoch.create("Epoch", "A", "B", "C");
        studySegment = epoch.getStudySegments().get(0);
        Period p1 = createPeriod("P1", 1, 7, 3);
        Period p2 = createPeriod("P2", 3, 1, 1);
        Period p3 = createPeriod("P3", 8, 28, 2);
        studySegment.addPeriod(p1);
        studySegment.addPeriod(p2);
        studySegment.addPeriod(p3);

        Activity a1 = new Activity();
        a1.setId(1);
        a1.setName("CBC");
                                                                                                              // days
        p1.addPlannedActivity(setId(1, createPlannedActivity("CBC", 1, "CBC Details")));                      // 1, 8, 15
        p1.addPlannedActivity(setId(2, createPlannedActivity("Vitals", 3, "Vitals Details")));                // 3, 10, 17
        p2.addPlannedActivity(setId(3, createPlannedActivity("Questionnaire", 1, "Questionnaire Details")));  // 3
        p3.addPlannedActivity(setId(4, createPlannedActivity("Infusion", 1, "Infusion Details")));            // 8, 36
        p3.addPlannedActivity(setId(5, createPlannedActivity("Infusion", 18, "Infusion Details")));           // 25, 53

        user = new User();
        Set<UserRole> userRoles = new HashSet<UserRole>();
        UserRole userRole = new UserRole();
        userRole.setRole(Role.SUBJECT_COORDINATOR);
        userRoles.add(userRole);
        user.setUserRoles(userRoles);
    }

    public void testAssignSubject() throws Exception {
        Study study = createNamedInstance("Glancing", Study.class);
        Amendment expectedAmendment = new Amendment("Foom");
        study.setAmendment(expectedAmendment);
        Site site = createNamedInstance("Lake", Site.class);
        StudySite studySite = createStudySite(study, site);
        Subject subjectIn = createSubject("Alice", "Childress");
        Date startDate = DateUtils.createDate(2006, Calendar.OCTOBER, 31);
        StudySegment expectedStudySegment = Epoch.create("Treatment", "A", "B", "C").getStudySegments().get(1);
        expectedStudySegment.addPeriod(createPeriod("DC", 1, 7, 1));
        expectedStudySegment.getPeriods().iterator().next().addPlannedActivity(createPlannedActivity("Any", 4));
        expect(amendmentService.getAmendedNode(expectedStudySegment, expectedAmendment)).andReturn(expectedStudySegment);

        Subject subjectExpectedSave = createSubject("Alice", "Childress");

        StudySubjectAssignment expectedAssignment = new StudySubjectAssignment();
        expectedAssignment.setStartDateEpoch(startDate);
        expectedAssignment.setSubject(subjectExpectedSave);
        expectedAssignment.setStudySite(studySite);
        expectedAssignment.setCurrentAmendment(expectedAmendment);

        subjectExpectedSave.addAssignment(expectedAssignment);

        subjectDao.save(subjectExpectedSave);
        expectLastCall().times(2);
        replayMocks();

        StudySubjectAssignment actualAssignment = service.assignSubject(subjectIn, studySite, expectedStudySegment, startDate, user);
        verifyMocks();

        assertNotNull("Assignment not returned", actualAssignment);
        assertEquals("Assignment not added to subject", 1, subjectIn.getAssignments().size());
        assertEquals("Assignment not added to subject", actualAssignment, subjectIn.getAssignments().get(0));

        assertNotNull(actualAssignment.getScheduledCalendar());
        assertEquals(1, actualAssignment.getScheduledCalendar().getScheduledStudySegments().size());
        ScheduledStudySegment scheduledStudySegment = actualAssignment.getScheduledCalendar().getScheduledStudySegments().get(0);
        assertEquals(expectedStudySegment, scheduledStudySegment.getStudySegment());
        assertPositive("No scheduled events", scheduledStudySegment.getEvents().size());
    }

    public void testScheduleFirstStudySegment() throws Exception {
        StudySubjectAssignment assignment = new StudySubjectAssignment();
        assignment.setSubject(createSubject("Alice", "Childress"));
        subjectDao.save(assignment.getSubject());

        StudySite studySite = new StudySite();
        studySite.setSite(new Site());
        assignment.setStudySite(studySite);

        Amendment expectedAmendment = new Amendment();
        assignment.setCurrentAmendment(expectedAmendment);
        expect(amendmentService.getAmendedNode(studySegment, expectedAmendment)).andReturn(studySegment);

        replayMocks();

        ScheduledStudySegment returnedStudySegment = service.scheduleStudySegment(
                assignment, studySegment, DateUtils.createDate(2006, Calendar.APRIL, 1),
                NextStudySegmentMode.PER_PROTOCOL);
        verifyMocks();

        ScheduledCalendar scheduledCalendar = assignment.getScheduledCalendar();
        assertNotNull("Scheduled calendar not created", scheduledCalendar);
        assertEquals("Study segment not added to scheduled study segments", 1, scheduledCalendar.getScheduledStudySegments().size());
        assertSame("Study segment not added to scheduled arms", returnedStudySegment, scheduledCalendar.getScheduledStudySegments().get(0));
        assertSame("Wrong study segment scheduled", studySegment, scheduledCalendar.getScheduledStudySegments().get(0).getStudySegment());
        assertEquals("Wrong start day for scheduled study segment", 1, (int) returnedStudySegment.getStartDay());
        assertDayOfDate("Wrong start date for scheduled study segment", 2006, Calendar.APRIL, 1, returnedStudySegment.getStartDate());
        List<ScheduledActivity> events = scheduledCalendar.getScheduledStudySegments().get(0).getEvents();
        assertEquals("Wrong number of events added", 11, events.size());

        Activity a1 = createNamedInstance("CBC", Activity.class);
        Activity a2 = createNamedInstance("Vitals", Activity.class);
        Activity a3 = createNamedInstance("Questionnaire", Activity.class);
        Activity a4 = createNamedInstance("Infusion", Activity.class);

        assertNewlyScheduledActivity(2006, Calendar.APRIL,  1, 1, "CBC Details",           a1, 0, events.get(0));
        assertNewlyScheduledActivity(2006, Calendar.APRIL,  3, 2, "Vitals Details",        a2, 0, events.get(1));
        assertNewlyScheduledActivity(2006, Calendar.APRIL,  3, 3, "Questionnaire Details", a3, 0, events.get(2));
        assertNewlyScheduledActivity(2006, Calendar.APRIL,  8, 1, "CBC Details",           a1, 1, events.get(3));
        assertNewlyScheduledActivity(2006, Calendar.APRIL,  8, 4, "Infusion Details",      a4, 0, events.get(4));
        assertNewlyScheduledActivity(2006, Calendar.APRIL, 10, 2, "Vitals Details",        a2, 1, events.get(5));
        assertNewlyScheduledActivity(2006, Calendar.APRIL, 15, 1, "CBC Details",           a1, 2, events.get(6));
        assertNewlyScheduledActivity(2006, Calendar.APRIL, 17, 2, "Vitals Details",        a2, 2, events.get(7));
        assertNewlyScheduledActivity(2006, Calendar.APRIL, 25, 5, "Infusion Details",      a4, 0, events.get(8));
        assertNewlyScheduledActivity(2006, Calendar.MAY,    6, 4, "Infusion Details",      a4, 1, events.get(9));
        assertNewlyScheduledActivity(2006, Calendar.MAY,   23, 5, "Infusion Details",      a4, 1, events.get(10));

        assertSame("Source amendment not set on SEs", expectedAmendment, events.get(7).getSourceAmendment());
    }
    
    public void testScheduleFirstStudySegmentWithNegativeDays() throws Exception {
        studySegment.getPeriods().first().setStartDay(-7);
        // this will shift the days for events in the first period:
        // event 1: -7, 0, 7
        // event 2: -5, 2, 9

        StudySubjectAssignment assignment = new StudySubjectAssignment();
        assignment.setSubject(createSubject("Alice", "Childress"));
        subjectDao.save(assignment.getSubject());

        StudySite studySite = new StudySite();
        studySite.setSite(new Site());
        assignment.setStudySite(studySite);        

        Amendment expectedAmendment = new Amendment();
        assignment.setCurrentAmendment(expectedAmendment);
        expect(amendmentService.getAmendedNode(studySegment, expectedAmendment)).andReturn(studySegment);

        replayMocks();


        ScheduledStudySegment returnedStudySegment = service.scheduleStudySegment(
            assignment, studySegment, DateUtils.createDate(2006, Calendar.MARCH, 24), NextStudySegmentMode.PER_PROTOCOL);
        verifyMocks();

        ScheduledCalendar scheduledCalendar = assignment.getScheduledCalendar();
        assertNotNull("Scheduled calendar not created", scheduledCalendar);
        assertEquals("Study segment not added to scheduled study segments", 1, scheduledCalendar.getScheduledStudySegments().size());
        assertSame("Study segment not added to scheduled study segments", returnedStudySegment, scheduledCalendar.getScheduledStudySegments().get(0));
        assertSame("Wrong study segment scheduled", studySegment, scheduledCalendar.getScheduledStudySegments().get(0).getStudySegment());
        List<ScheduledActivity> events = scheduledCalendar.getScheduledStudySegments().get(0).getEvents();
        assertEquals("Wrong number of events added", 11, events.size());
        assertEquals("Wrong start day for study segment", -7, (int) returnedStudySegment.getStartDay());
        assertDayOfDate("Wrong start date for study segment", 2006, Calendar.MARCH, 24, returnedStudySegment.getStartDate());

        assertNewlyScheduledActivity(2006, Calendar.MARCH, 24, 1, events.get(0));
        assertNewlyScheduledActivity(2006, Calendar.MARCH, 26, 2, events.get(1));
        assertNewlyScheduledActivity(2006, Calendar.MARCH, 31, 1, events.get(2));
        assertNewlyScheduledActivity(2006, Calendar.APRIL,  2, 2, events.get(3));
        assertNewlyScheduledActivity(2006, Calendar.APRIL,  3, 3, events.get(4));
        assertNewlyScheduledActivity(2006, Calendar.APRIL,  7, 1, events.get(5));
        assertNewlyScheduledActivity(2006, Calendar.APRIL,  8, 4, events.get(6));
        assertNewlyScheduledActivity(2006, Calendar.APRIL,  9, 2, events.get(7));
        assertNewlyScheduledActivity(2006, Calendar.APRIL, 25, 5, events.get(8));
        assertNewlyScheduledActivity(2006, Calendar.MAY,    6, 4, events.get(9));
        assertNewlyScheduledActivity(2006, Calendar.MAY,   23, 5, events.get(10));
    }

    public void testScheduleImmediateNextStudySegment() throws Exception {
        StudySubjectAssignment assignment = new StudySubjectAssignment();
        ScheduledCalendar calendar = new ScheduledCalendar();
        assignment.setScheduledCalendar(calendar);
        assignment.setSubject(createSubject("Alice", "Childress"));
        Amendment expectedAmendment = new Amendment();
        assignment.setCurrentAmendment(expectedAmendment);
        expect(amendmentService.getAmendedNode(studySegment, expectedAmendment)).andReturn(studySegment);

        ScheduledStudySegment existingStudySegment = new ScheduledStudySegment();
        existingStudySegment.addEvent(createScheduledActivity("CBC", 2005, Calendar.AUGUST, 1));
        existingStudySegment.addEvent(createScheduledActivity("CBC", 2005, Calendar.AUGUST, 2,
            new Occurred(null, DateUtils.createDate(2005, Calendar.AUGUST, 4))));
        existingStudySegment.addEvent(createScheduledActivity("CBC", 2005, Calendar.AUGUST, 3,
            new Canceled(null)));

        calendar.addStudySegment(existingStudySegment);

        subjectDao.save(assignment.getSubject());

        StudySite studySite = new StudySite();
        studySite.setSite(new Site());
        assignment.setStudySite(studySite);        

        replayMocks();
        ScheduledStudySegment returnedStudySegment = service.scheduleStudySegment(
            assignment, studySegment, DateUtils.createDate(2005, Calendar.SEPTEMBER, 1), NextStudySegmentMode.IMMEDIATE);
        verifyMocks();

        ScheduledCalendar scheduledCalendar = assignment.getScheduledCalendar();
        assertEquals("Study segment not added to scheduled arms", 2, scheduledCalendar.getScheduledStudySegments().size());
        assertSame("Study segment not added to scheduled arms", returnedStudySegment, scheduledCalendar.getScheduledStudySegments().get(1));
        assertSame("Wrong study segment scheduled", studySegment, scheduledCalendar.getScheduledStudySegments().get(1).getStudySegment());

        List<ScheduledActivity> events = scheduledCalendar.getScheduledStudySegments().get(1).getEvents();
        assertEquals("Wrong number of events added", 11, events.size());
        assertNewlyScheduledActivity(2005, Calendar.SEPTEMBER,  1, 1, events.get(0));
        assertNewlyScheduledActivity(2005, Calendar.SEPTEMBER,  3, 2, events.get(1));
        assertNewlyScheduledActivity(2005, Calendar.SEPTEMBER,  3, 3, events.get(2));
        assertNewlyScheduledActivity(2005, Calendar.SEPTEMBER,  8, 1, events.get(3));
        assertNewlyScheduledActivity(2005, Calendar.SEPTEMBER,  8, 4, events.get(4));
        assertNewlyScheduledActivity(2005, Calendar.SEPTEMBER, 10, 2, events.get(5));
        assertNewlyScheduledActivity(2005, Calendar.SEPTEMBER, 15, 1, events.get(6));
        assertNewlyScheduledActivity(2005, Calendar.SEPTEMBER, 17, 2, events.get(7));
        assertNewlyScheduledActivity(2005, Calendar.SEPTEMBER, 25, 5, events.get(8));
        assertNewlyScheduledActivity(2005, Calendar.OCTOBER,    6, 4, events.get(9));
        assertNewlyScheduledActivity(2005, Calendar.OCTOBER,   23, 5, events.get(10));

        ScheduledActivity wasScheduledActivity = existingStudySegment.getEvents().get(0);
        assertEquals("No new state in scheduled", 2, wasScheduledActivity.getAllStates().size());
        assertEquals("Scheduled event not canceled", Canceled.class, wasScheduledActivity.getCurrentState().getClass());
        assertEquals("Wrong reason for cancelation", "Immediate transition to Epoch: A", wasScheduledActivity.getCurrentState().getReason());

        ScheduledActivity wasOccurredEvent = existingStudySegment.getEvents().get(1);
        assertEquals("Occurred event changed", 2, wasOccurredEvent.getAllStates().size());
        assertEquals("Occurred event changed", Occurred.class, wasOccurredEvent.getCurrentState().getClass());

        ScheduledActivity wasCanceledEvent = existingStudySegment.getEvents().get(2);
        assertEquals("Canceled event changed", 2, wasCanceledEvent.getAllStates().size());
        assertEquals("Canceled event changed", Canceled.class, wasCanceledEvent.getCurrentState().getClass());
    }

    private void assertNewlyScheduledActivity(
        int expectedYear, int expectedMonth, int expectedDayOfMonth,
        int expectedPlannedActivityId, ScheduledActivity actualEvent
    ) {
        assertEquals("Wrong associated planned event", expectedPlannedActivityId, (int) actualEvent.getPlannedActivity().getId());
        assertDayOfDate("Wrong ideal date", expectedYear, expectedMonth, expectedDayOfMonth, actualEvent.getIdealDate());
        assertTrue("Wrong current state mode", actualEvent.getCurrentState() instanceof Scheduled);
        Scheduled currentState = (Scheduled) actualEvent.getCurrentState();
        assertEquals("Current and ideal date not same", actualEvent.getIdealDate(), currentState.getDate());
        assertEquals("Wrong reason", "Initialized from template", currentState.getReason());
    }

    private void assertNewlyScheduledActivity(
        int expectedYear, int expectedMonth, int expectedDayOfMonth,
        int expectedPlannedActivityId, String expectedDetails,
        Activity expectedActivity, int expectedRepetitionNumber,
        ScheduledActivity actualEvent
    ){
        assertNewlyScheduledActivity(expectedYear, expectedMonth, expectedDayOfMonth, expectedPlannedActivityId, actualEvent);
        assertEquals("Wrong details", expectedDetails, actualEvent.getDetails());
        assertEquals("Wrong repetition number", expectedRepetitionNumber, (int) actualEvent.getRepetitionNumber());
        assertNotNull("No activity", actualEvent.getActivity());
        assertEquals("Wrong activity", expectedActivity.getName(), actualEvent.getActivity().getName());
    }

    public void testFindRecurringHoliday() throws Exception {
        Calendar cal = Calendar.getInstance();
        List<Date> listOfDays = new ArrayList<Date>();
        assertTrue(listOfDays.size()==0);
        int dayOfTheWeek = Calendar.DAY_OF_WEEK;
        listOfDays = service.findRecurringHoliday(cal, dayOfTheWeek);
        assertTrue(listOfDays.size()>3 && listOfDays.size()<6);
    }

    public void testShiftDayByOne() throws Exception {
        Calendar cal = Calendar.getInstance();
        Date date = cal.getTime();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date newDate = service.shiftDayByOne(date);
        assertNotEquals("dates are equals", df.format(newDate), df.format(date));

        java.sql.Timestamp timestampTo = new java.sql.Timestamp(newDate.getTime());
        long oneDay = 24 * 60 * 60 * 1000;
        timestampTo.setTime(timestampTo.getTime() - oneDay);
        assertEquals("dates are not equals", df.format(timestampTo), df.format(date));
    }

    public void testResetTheEvent() throws Exception {
        StudySubjectAssignment assignment = new StudySubjectAssignment();
        ScheduledCalendar scheduledCalendar = new ScheduledCalendar();
        assignment.setScheduledCalendar(scheduledCalendar);
        ScheduledStudySegment existingStudySegment = new ScheduledStudySegment();
        existingStudySegment.addEvent(createScheduledActivity("CBC", 2005, Calendar.AUGUST, 1));
        scheduledCalendar.addStudySegment(existingStudySegment);

        StudySite studySite = new StudySite();
        studySite.setSite(new Site());
        assignment.setStudySite(studySite);
        List<ScheduledActivity> events = scheduledCalendar.getScheduledStudySegments().get(0).getEvents();
        ScheduledActivity event = events.get(0);
        Calendar holiday = Calendar.getInstance();
        holiday.set(2005, Calendar.AUGUST, 1);
        String description = "Closed";
        Date dateBeforeReset = event.getActualDate();
        assertNotEquals("descriptions are equals", event.getCurrentState().getReason(), description);
        service.shiftToAvoidBlackoutDate(holiday.getTime(), event,
                scheduledCalendar.getAssignment().getStudySite().getSite(),
                description);
        Date dateAfterReset = event.getActualDate();

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        assertNotEquals("dates are equals", df.format(dateAfterReset), df.format(dateBeforeReset));

        java.sql.Timestamp timestampTo = new java.sql.Timestamp(dateAfterReset.getTime());
        long oneDay = 1 * 24 * 60 * 60 * 1000;
        timestampTo.setTime(timestampTo.getTime() - oneDay);
        assertEquals("dates are not equals", df.format(timestampTo), df.format(dateBeforeReset));        
        assertEquals("descriptions are not equals", event.getCurrentState().getReason(),
                SubjectService.RESCHEDULED + description);
    }

    public void testAvoidWeekendsAndHolidays() throws Exception {
        StudySubjectAssignment assignment = new StudySubjectAssignment();
        ScheduledCalendar scheduledCalendar = new ScheduledCalendar();
        assignment.setScheduledCalendar(scheduledCalendar);

        Epoch epoch = Epoch.create("Epoch", "A", "B", "C");
        StudySegment scheduledStudySegment = epoch.getStudySegments().get(0);
        Period p1 = createPeriod("P1", 1, 7, 3);
        scheduledStudySegment.addPeriod(p1);
        p1.addPlannedActivity(setId(1, createPlannedActivity("CBC", 1)));

        Amendment expectedAmendment = new Amendment();
        assignment.setCurrentAmendment(expectedAmendment);
        expect(amendmentService.getAmendedNode(scheduledStudySegment, expectedAmendment))
            .andReturn(scheduledStudySegment);

        StudySite studySite = new StudySite();
        Site site = new Site();

        MonthDayHoliday holidayOne = new MonthDayHoliday();
        holidayOne.setDay(1);
        holidayOne.setMonth(Calendar.AUGUST);
        holidayOne.setYear(2005);
        MonthDayHoliday holidayTwo = new MonthDayHoliday();
        holidayTwo.setDay(2);
        holidayTwo.setMonth(Calendar.AUGUST);
        holidayTwo.setYear(2005);
        MonthDayHoliday holidayThree = new MonthDayHoliday();
        holidayThree.setDay(3);
        holidayThree.setMonth(Calendar.AUGUST);
        holidayThree.setYear(2005);
        List<Holiday> listOfHolidays = new ArrayList<Holiday>();
        listOfHolidays.add(holidayOne);
        listOfHolidays.add(holidayTwo);
        listOfHolidays.add(holidayThree);
        site.setHolidaysAndWeekends(listOfHolidays);

       studySite.setSite(site);
        assignment.setStudySite(studySite);

        assignment.setSubject(createSubject("Alice", "Childress"));
        subjectDao.save(assignment.getSubject());


        replayMocks();

        ScheduledStudySegment returnedStudySegment = service.scheduleStudySegment(
                assignment, scheduledStudySegment, DateUtils.createDate(2005, Calendar.AUGUST , 1),
                NextStudySegmentMode.PER_PROTOCOL);
        verifyMocks();

        List<ScheduledActivity> events = returnedStudySegment.getEvents();

        assertNotNull("Scheduled calendar not created", scheduledCalendar);
        assertEquals("Study segment not added to scheduled study segments", 1, scheduledCalendar.getScheduledStudySegments().size());
        assertSame("Study segment not added to scheduled study segments", returnedStudySegment, scheduledCalendar.getScheduledStudySegments().get(0));
        assertEquals("Wrong number of events added", 3, events.size());

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(events.get(0).getActualDate());
        assertEquals("Date is not reset ", calendar.get(Calendar.DAY_OF_MONTH), 4);
        assertEquals("Month is not reset ", calendar.get(Calendar.MONTH), Calendar.AUGUST);
        assertEquals("Date is not reset ", calendar.get(Calendar.YEAR), 2005);

        assertNewlyScheduledActivity(2005, Calendar.AUGUST, 8, 1, events.get(1));
        assertNewlyScheduledActivity(2005, Calendar.AUGUST, 15, 1, events.get(2));


    }

    public void testTakeSubjectOffStudy() throws Exception {
        Date startDate = DateUtils.createDate(2007, Calendar.AUGUST, 31);
        Date expectedEndDate = DateUtils.createDate(2007, Calendar.SEPTEMBER, 4);

        StudySubjectAssignment expectedAssignment = setId(1, new StudySubjectAssignment());
        expectedAssignment.setStartDateEpoch(startDate);

        ScheduledStudySegment studySegment0 = new ScheduledStudySegment();
        studySegment0.addEvent(createScheduledActivity("ABC", 2007, Calendar.SEPTEMBER, 2, new Occurred()));
        studySegment0.addEvent(createScheduledActivity("DEF", 2007, Calendar.SEPTEMBER, 4, new Canceled()));
        studySegment0.addEvent(createScheduledActivity("GHI", 2007, Calendar.SEPTEMBER, 6, new Occurred()));
        studySegment0.addEvent(createScheduledActivity("JKL", 2007, Calendar.SEPTEMBER, 8, new Scheduled()));

        ScheduledStudySegment studySegment1 = new ScheduledStudySegment();
        studySegment1.addEvent(createScheduledActivity("MNO", 2007, Calendar.OCTOBER, 2, new Occurred()));
        studySegment1.addEvent(createScheduledActivity("PQR", 2007, Calendar.OCTOBER, 4, new Scheduled()));
        studySegment1.addEvent(createScheduledActivity("STU", 2007, Calendar.OCTOBER, 6, new Scheduled()));
        studySegment1.addEvent(createScheduledActivity("VWX", 2007, Calendar.OCTOBER, 8, new Scheduled()));
        studySegment1.addEvent(createConditionalEvent("YZA", 2007, Calendar.OCTOBER, 10));

        ScheduledCalendar calendar = new ScheduledCalendar();
        calendar.setAssignment(expectedAssignment);
        calendar.addStudySegment(studySegment0);
        calendar.addStudySegment(studySegment1);
        expectedAssignment.setScheduledCalendar(calendar);

        subjectDao.save(expectedAssignment.getSubject());
        replayMocks();

        StudySubjectAssignment actualAssignment = service.takeSubjectOffStudy(expectedAssignment, expectedEndDate);
        verifyMocks();

        CoreTestCase.assertDayOfDate("Wrong off study day", 2007, Calendar.SEPTEMBER, 4, actualAssignment.getEndDateEpoch());

        assertEquals("Wrong Event Mode", ScheduledActivityMode.OCCURRED, studySegment0.getEvents().get(2).getCurrentState().getMode());
        assertEquals("Wrong Event Mode", ScheduledActivityMode.CANCELED, studySegment0.getEvents().get(3).getCurrentState().getMode());
        assertEquals("Wrong Event Mode", ScheduledActivityMode.OCCURRED, studySegment1.getEvents().get(0).getCurrentState().getMode());
        assertEquals("Wrong Event Mode", ScheduledActivityMode.CANCELED, studySegment1.getEvents().get(1).getCurrentState().getMode());
        assertEquals("Wrong Event Mode", ScheduledActivityMode.CANCELED, studySegment1.getEvents().get(2).getCurrentState().getMode());
        assertEquals("Wrong Event Mode", ScheduledActivityMode.CANCELED, studySegment1.getEvents().get(3).getCurrentState().getMode());
        assertEquals("Wrong Event Mode", ScheduledActivityMode.NOT_APPLICABLE, studySegment1.getEvents().get(4).getCurrentState().getMode());
    }

    public void testScheduleStudySegmentWithOffStudySubject() {
        StudySubjectAssignment assignment = new StudySubjectAssignment();
        assignment.setSubject(createSubject("Alice", "Childress"));
        assignment.setEndDateEpoch(DateUtils.createDate(2006, Calendar.APRIL, 1));

        StudySite studySite = new StudySite();
        studySite.setSite(new Site());
        assignment.setStudySite(studySite);

        replayMocks();

        ScheduledStudySegment returnedStudySegment = service.scheduleStudySegment(
                assignment, studySegment, DateUtils.createDate(2006, Calendar.APRIL, 1),
                NextStudySegmentMode.PER_PROTOCOL);
        verifyMocks();

        ScheduledCalendar scheduledCalendar = assignment.getScheduledCalendar();
        assertNull("Scheduled calendar not created", scheduledCalendar);
        assertSame("Study segment not added to scheduled study segments", null, returnedStudySegment);
    }
}
