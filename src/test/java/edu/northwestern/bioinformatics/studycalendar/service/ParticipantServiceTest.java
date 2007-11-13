package edu.northwestern.bioinformatics.studycalendar.service;

import edu.nwu.bioinformatics.commons.DateUtils;
import edu.nwu.bioinformatics.commons.testing.CoreTestCase;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.ParticipantDao;
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
public class ParticipantServiceTest extends StudyCalendarTestCase {
    private ParticipantDao participantDao;
    private ParticipantService service;

    private User user;

    private Arm arm;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        participantDao = registerMockFor(ParticipantDao.class);

        service = new ParticipantService();
        service.setParticipantDao(participantDao);

        Epoch epoch = Epoch.create("Epoch", "A", "B", "C");
        arm = epoch.getArms().get(0);
        Period p1 = createPeriod("P1", 1, 7, 3);
        Period p2 = createPeriod("P2", 3, 1, 1);
        Period p3 = createPeriod("P3", 8, 28, 2);
        arm.addPeriod(p1);
        arm.addPeriod(p2);
        arm.addPeriod(p3);

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
        userRole.setRole(Role.PARTICIPANT_COORDINATOR);
        userRoles.add(userRole);
        user.setUserRoles(userRoles);
    }

    public void testAssignParticipant() throws Exception {
        Study study = createNamedInstance("Glancing", Study.class);
        Amendment expectedAmendment = new Amendment("Foom");
        study.setAmendment(expectedAmendment);
        Site site = createNamedInstance("Lake", Site.class);
        StudySite studySite = createStudySite(study, site);
        Participant participantIn = createParticipant("Alice", "Childress");
        Date startDate = DateUtils.createDate(2006, Calendar.OCTOBER, 31);
        Arm expectedArm = Epoch.create("Treatment", "A", "B", "C").getArms().get(1);
        expectedArm.addPeriod(createPeriod("DC", 1, 7, 1));
        expectedArm.getPeriods().iterator().next().addPlannedActivity(createPlannedActivity("Any", 4));

        Participant participantExpectedSave = createParticipant("Alice", "Childress");

        StudyParticipantAssignment expectedAssignment = new StudyParticipantAssignment();
        expectedAssignment.setStartDateEpoch(startDate);
        expectedAssignment.setParticipant(participantExpectedSave);
        expectedAssignment.setStudySite(studySite);
        expectedAssignment.setCurrentAmendment(expectedAmendment);

        participantExpectedSave.addAssignment(expectedAssignment);

        participantDao.save(participantExpectedSave);
        expectLastCall().times(2);
        replayMocks();

        StudyParticipantAssignment actualAssignment = service.assignParticipant(participantIn, studySite, expectedArm, startDate, user);
        verifyMocks();

        assertNotNull("Assignment not returned", actualAssignment);
        assertEquals("Assignment not added to participant", 1, participantIn.getAssignments().size());
        assertEquals("Assignment not added to participant", actualAssignment, participantIn.getAssignments().get(0));

        assertNotNull(actualAssignment.getScheduledCalendar());
        assertEquals(1, actualAssignment.getScheduledCalendar().getScheduledArms().size());
        ScheduledArm scheduledArm = actualAssignment.getScheduledCalendar().getScheduledArms().get(0);
        assertEquals(expectedArm, scheduledArm.getArm());
        assertPositive("No scheduled events", scheduledArm.getEvents().size());
    }

    public void testScheduleFirstArm() throws Exception {
        StudyParticipantAssignment assignment = new StudyParticipantAssignment();
        assignment.setParticipant(createParticipant("Alice", "Childress"));
        participantDao.save(assignment.getParticipant());

        StudySite studySite = new StudySite();
        studySite.setSite(new Site());
        assignment.setStudySite(studySite);

        Amendment expectedAmendment = new Amendment();
        assignment.setCurrentAmendment(expectedAmendment);

        replayMocks();

        ScheduledArm returnedArm = service.scheduleArm(
                assignment, arm, DateUtils.createDate(2006, Calendar.APRIL, 1),
                NextArmMode.PER_PROTOCOL);
        verifyMocks();

        ScheduledCalendar scheduledCalendar = assignment.getScheduledCalendar();
        assertNotNull("Scheduled calendar not created", scheduledCalendar);
        assertEquals("Arm not added to scheduled arms", 1, scheduledCalendar.getScheduledArms().size());
        assertSame("Arm not added to scheduled arms", returnedArm, scheduledCalendar.getScheduledArms().get(0));
        assertSame("Wrong arm scheduled", arm, scheduledCalendar.getScheduledArms().get(0).getArm());
        assertEquals("Wrong start day for scheduled arm", 1, (int) returnedArm.getStartDay());
        assertDayOfDate("Wrong start date for scheduled arm", 2006, Calendar.APRIL, 1, returnedArm.getStartDate());
        List<ScheduledActivity> events = scheduledCalendar.getScheduledArms().get(0).getEvents();
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
    
    public void testScheduleFirstArmWithNegativeDays() throws Exception {
        arm.getPeriods().first().setStartDay(-7);
        // this will shift the days for events in the first period:
        // event 1: -7, 0, 7
        // event 2: -5, 2, 9

        StudyParticipantAssignment assignment = new StudyParticipantAssignment();
        assignment.setParticipant(createParticipant("Alice", "Childress"));
        participantDao.save(assignment.getParticipant());

        StudySite studySite = new StudySite();
        studySite.setSite(new Site());
        assignment.setStudySite(studySite);        

        replayMocks();


        ScheduledArm returnedArm = service.scheduleArm(
            assignment, arm, DateUtils.createDate(2006, Calendar.MARCH, 24), NextArmMode.PER_PROTOCOL);
        verifyMocks();

        ScheduledCalendar scheduledCalendar = assignment.getScheduledCalendar();
        assertNotNull("Scheduled calendar not created", scheduledCalendar);
        assertEquals("Arm not added to scheduled arms", 1, scheduledCalendar.getScheduledArms().size());
        assertSame("Arm not added to scheduled arms", returnedArm, scheduledCalendar.getScheduledArms().get(0));
        assertSame("Wrong arm scheduled", arm, scheduledCalendar.getScheduledArms().get(0).getArm());
        List<ScheduledActivity> events = scheduledCalendar.getScheduledArms().get(0).getEvents();
        assertEquals("Wrong number of events added", 11, events.size());
        assertEquals("Wrong start day for arm", -7, (int) returnedArm.getStartDay());
        assertDayOfDate("Wrong start date for arm", 2006, Calendar.MARCH, 24, returnedArm.getStartDate());

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

    public void testScheduleImmediateNextArm() throws Exception {
        StudyParticipantAssignment assignment = new StudyParticipantAssignment();
        ScheduledCalendar calendar = new ScheduledCalendar();
        assignment.setScheduledCalendar(calendar);
        assignment.setParticipant(createParticipant("Alice", "Childress"));

        ScheduledArm existingArm = new ScheduledArm();
        existingArm.addEvent(createScheduledActivity("CBC", 2005, Calendar.AUGUST, 1));
        existingArm.addEvent(createScheduledActivity("CBC", 2005, Calendar.AUGUST, 2,
            new Occurred(null, DateUtils.createDate(2005, Calendar.AUGUST, 4))));
        existingArm.addEvent(createScheduledActivity("CBC", 2005, Calendar.AUGUST, 3,
            new Canceled(null)));

        calendar.addArm(existingArm);

        participantDao.save(assignment.getParticipant());

        StudySite studySite = new StudySite();
        studySite.setSite(new Site());
        assignment.setStudySite(studySite);        

        replayMocks();
        ScheduledArm returnedArm = service.scheduleArm(
            assignment, arm, DateUtils.createDate(2005, Calendar.SEPTEMBER, 1), NextArmMode.IMMEDIATE);
        verifyMocks();

        ScheduledCalendar scheduledCalendar = assignment.getScheduledCalendar();
        assertEquals("Arm not added to scheduled arms", 2, scheduledCalendar.getScheduledArms().size());
        assertSame("Arm not added to scheduled arms", returnedArm, scheduledCalendar.getScheduledArms().get(1));
        assertSame("Wrong arm scheduled", arm, scheduledCalendar.getScheduledArms().get(1).getArm());

        List<ScheduledActivity> events = scheduledCalendar.getScheduledArms().get(1).getEvents();
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

        ScheduledActivity wasScheduledActivity = existingArm.getEvents().get(0);
        assertEquals("No new state in scheduled", 2, wasScheduledActivity.getAllStates().size());
        assertEquals("Scheduled event not canceled", Canceled.class, wasScheduledActivity.getCurrentState().getClass());
        assertEquals("Wrong reason for cancelation", "Immediate transition to Epoch: A", wasScheduledActivity.getCurrentState().getReason());

        ScheduledActivity wasOccurredEvent = existingArm.getEvents().get(1);
        assertEquals("Occurred event changed", 2, wasOccurredEvent.getAllStates().size());
        assertEquals("Occurred event changed", Occurred.class, wasOccurredEvent.getCurrentState().getClass());

        ScheduledActivity wasCanceledEvent = existingArm.getEvents().get(2);
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
        StudyParticipantAssignment assignment = new StudyParticipantAssignment();
        ScheduledCalendar scheduledCalendar = new ScheduledCalendar();
        assignment.setScheduledCalendar(scheduledCalendar);
        ScheduledArm existingArm = new ScheduledArm();
        existingArm.addEvent(createScheduledActivity("CBC", 2005, Calendar.AUGUST, 1));
        scheduledCalendar.addArm(existingArm);

        StudySite studySite = new StudySite();
        studySite.setSite(new Site());
        assignment.setStudySite(studySite);
        List<ScheduledActivity> events = scheduledCalendar.getScheduledArms().get(0).getEvents();
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
                ParticipantService.RESCHEDULED + description);
    }

    public void testAvoidWeekendsAndHolidays() throws Exception {
        StudyParticipantAssignment assignment = new StudyParticipantAssignment();
        ScheduledCalendar scheduledCalendar = new ScheduledCalendar();
        assignment.setScheduledCalendar(scheduledCalendar);

        Epoch epoch = Epoch.create("Epoch", "A", "B", "C");
        Arm scheduledArm = epoch.getArms().get(0);
        Period p1 = createPeriod("P1", 1, 7, 3);
        scheduledArm.addPeriod(p1);
        p1.addPlannedActivity(setId(1, createPlannedActivity("CBC", 1)));
        
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

        assignment.setParticipant(createParticipant("Alice", "Childress"));
        participantDao.save(assignment.getParticipant());


        replayMocks();

        ScheduledArm returnedArm = service.scheduleArm(
                assignment, scheduledArm, DateUtils.createDate(2005, Calendar.AUGUST , 1),
                NextArmMode.PER_PROTOCOL);
        verifyMocks();

        List<ScheduledActivity> events = returnedArm.getEvents();

        assertNotNull("Scheduled calendar not created", scheduledCalendar);
        assertEquals("Arm not added to scheduled arms", 1, scheduledCalendar.getScheduledArms().size());
        assertSame("Arm not added to scheduled arms", returnedArm, scheduledCalendar.getScheduledArms().get(0));
        assertEquals("Wrong number of events added", 3, events.size());

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(events.get(0).getActualDate());
        assertEquals("Date is not reset ", calendar.get(Calendar.DAY_OF_MONTH), 4);
        assertEquals("Month is not reset ", calendar.get(Calendar.MONTH), Calendar.AUGUST);
        assertEquals("Date is not reset ", calendar.get(Calendar.YEAR), 2005);

        assertNewlyScheduledActivity(2005, Calendar.AUGUST, 8, 1, events.get(1));
        assertNewlyScheduledActivity(2005, Calendar.AUGUST, 15, 1, events.get(2));


    }

    public void testTakeParticipantOffStudy() throws Exception {
        Date startDate = DateUtils.createDate(2007, Calendar.AUGUST, 31);
        Date expectedEndDate = DateUtils.createDate(2007, Calendar.SEPTEMBER, 4);

        StudyParticipantAssignment expectedAssignment = setId(1, new StudyParticipantAssignment());
        expectedAssignment.setStartDateEpoch(startDate);

        ScheduledArm arm0 = new ScheduledArm();
        arm0.addEvent(createScheduledActivity("ABC", 2007, Calendar.SEPTEMBER, 2, new Occurred()));
        arm0.addEvent(createScheduledActivity("DEF", 2007, Calendar.SEPTEMBER, 4, new Canceled()));
        arm0.addEvent(createScheduledActivity("GHI", 2007, Calendar.SEPTEMBER, 6, new Occurred()));
        arm0.addEvent(createScheduledActivity("JKL", 2007, Calendar.SEPTEMBER, 8, new Scheduled()));

        ScheduledArm arm1 = new ScheduledArm();
        arm1.addEvent(createScheduledActivity("MNO", 2007, Calendar.OCTOBER, 2, new Occurred()));
        arm1.addEvent(createScheduledActivity("PQR", 2007, Calendar.OCTOBER, 4, new Scheduled()));
        arm1.addEvent(createScheduledActivity("STU", 2007, Calendar.OCTOBER, 6, new Scheduled()));
        arm1.addEvent(createScheduledActivity("VWX", 2007, Calendar.OCTOBER, 8, new Scheduled()));
        arm1.addEvent(createConditionalEvent("YZA", 2007, Calendar.OCTOBER, 10));

        ScheduledCalendar calendar = new ScheduledCalendar();
        calendar.setAssignment(expectedAssignment);
        calendar.addArm(arm0);
        calendar.addArm(arm1);
        expectedAssignment.setScheduledCalendar(calendar);

        participantDao.save(expectedAssignment.getParticipant());
        replayMocks();

        StudyParticipantAssignment actualAssignment = service.takeParticipantOffStudy(expectedAssignment, expectedEndDate);
        verifyMocks();

        CoreTestCase.assertDayOfDate("Wrong off study day", 2007, Calendar.SEPTEMBER, 4, actualAssignment.getEndDateEpoch());

        assertEquals("Wrong Event Mode", ScheduledActivityMode.OCCURRED, arm0.getEvents().get(2).getCurrentState().getMode());
        assertEquals("Wrong Event Mode", ScheduledActivityMode.CANCELED, arm0.getEvents().get(3).getCurrentState().getMode());
        assertEquals("Wrong Event Mode", ScheduledActivityMode.OCCURRED, arm1.getEvents().get(0).getCurrentState().getMode());
        assertEquals("Wrong Event Mode", ScheduledActivityMode.CANCELED, arm1.getEvents().get(1).getCurrentState().getMode());
        assertEquals("Wrong Event Mode", ScheduledActivityMode.CANCELED, arm1.getEvents().get(2).getCurrentState().getMode());
        assertEquals("Wrong Event Mode", ScheduledActivityMode.CANCELED, arm1.getEvents().get(3).getCurrentState().getMode());
        assertEquals("Wrong Event Mode", ScheduledActivityMode.NOT_APPLICABLE, arm1.getEvents().get(4).getCurrentState().getMode());
    }

    public void testScheduleArmWithOffStudyParticipant() {
        StudyParticipantAssignment assignment = new StudyParticipantAssignment();
        assignment.setParticipant(createParticipant("Alice", "Childress"));
        assignment.setEndDateEpoch(DateUtils.createDate(2006, Calendar.APRIL, 1));

        StudySite studySite = new StudySite();
        studySite.setSite(new Site());
        assignment.setStudySite(studySite);

        replayMocks();

        ScheduledArm returnedArm = service.scheduleArm(
                assignment, arm, DateUtils.createDate(2006, Calendar.APRIL, 1),
                NextArmMode.PER_PROTOCOL);
        verifyMocks();

        ScheduledCalendar scheduledCalendar = assignment.getScheduledCalendar();
        assertNull("Scheduled calendar not created", scheduledCalendar);
        assertSame("Arm not added to scheduled arms", null, returnedArm);
    }
}
