package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Canceled;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Occurred;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setId;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createParticipant;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createStudySite;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createScheduledEvent;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.addEvents;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledEventDao;

import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import static org.easymock.EasyMock.expect;


public class ScheduleCommandTest extends StudyCalendarTestCase {
    private User user;
    private String userName;
    private UserDao userDao;
    private ScheduledEventDao scheduledEventDao;

    private ScheduleCommand command = new ScheduleCommand();


    @Override
    protected void setUp() throws Exception {
        super.setUp();

        user = new User();
        userName = "USER NAME";
        user.setName(userName);
        userDao = registerDaoMockFor(UserDao.class);
        userDao = registerMockFor(UserDao.class);
        scheduledEventDao = registerMockFor(ScheduledEventDao.class);

        command.setScheduledEventDao(scheduledEventDao);
        command.setUserDao(userDao);
        command.setUser(user);
        command.setToDate(3);

    }

    public void testShiftStartDayByNumberOfDays() throws Exception {
        Date startDate = new Date();
        Integer numberOfDaysToShift = 3;
        Calendar rightNow = Calendar.getInstance();
        rightNow.add(Calendar.DATE, numberOfDaysToShift);
        Date expectedDate = rightNow.getTime();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = df.format(expectedDate);
        expectedDate = df.parse(dateString);
        replayMocks();
        Date actualDate = command.shiftStartDayByNumberOfDays(startDate, numberOfDaysToShift);
        verifyMocks();
        assertEquals("Expected and Actual Dates are different ", expectedDate, actualDate);
    }

    public void testFormatDateToString() throws Exception {
        Calendar rightNow = Calendar.getInstance(); 
        DateFormat df = new SimpleDateFormat("MM/dd");
        String expectedDateString = df.format(new Date());
        replayMocks();
        String actualDateString = command.formatDateToString(rightNow.getTime());
        verifyMocks();
        assertEquals("Expected and Actual formats are not equals ", expectedDateString, actualDateString);
    }

    public void testGetMapOfCurrentEvents() throws Exception {
        Participant participant = setId(11, createParticipant("Fred", "Jones"));
        StudySite studySite = setId(14, createStudySite(null, null));
        StudyParticipantAssignment assignment = new StudyParticipantAssignment();
        ScheduledCalendar calendar;

        ScheduledEvent e1, e2, e3;

        calendar = setId(6, new ScheduledCalendar());
        calendar.addArm(new ScheduledArm());
        calendar.addArm(new ScheduledArm());

        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH);
        int year = c.get(Calendar.YEAR);

        e1 =  createScheduledEvent("C", year, month, day);
        e2 =  createScheduledEvent("O", year, month, day+1);
        e3 =  createScheduledEvent("S", year, month, day+2);

        addEvents(calendar.getScheduledArms().get(0), e1, e2, e3);

        assignment.setParticipant(participant);
        assignment.setStudySite(studySite);
        assignment.setStartDateEpoch(new Date());
        assignment.setScheduledCalendar(calendar);
        List<StudyParticipantAssignment> studyParticipantAssignment = new ArrayList<StudyParticipantAssignment>();
        studyParticipantAssignment.add(assignment);

        Collection<ScheduledEvent> events = new ArrayList<ScheduledEvent>();
        events.add(e1);
        Date startDate = new Date();
        Date tempStartDateOne = command.shiftStartDayByNumberOfDays(startDate, 0);
        expect(scheduledEventDao.getEventsByDate(calendar, tempStartDateOne, tempStartDateOne)).andReturn(events);

        Collection<ScheduledEvent> eventsTwo = new ArrayList<ScheduledEvent>();
        eventsTwo.add(e2);
        Date tempStartDateTwo = command.shiftStartDayByNumberOfDays(startDate, 1);
        expect(scheduledEventDao.getEventsByDate(calendar, tempStartDateTwo, tempStartDateTwo)).andReturn(eventsTwo);

        Collection<ScheduledEvent> eventsThree = new ArrayList<ScheduledEvent>();
        eventsThree.add(e3);
        Date tempStartDateThree = command.shiftStartDayByNumberOfDays(startDate, 2);
        expect(scheduledEventDao.getEventsByDate(calendar, tempStartDateThree, tempStartDateThree)).andReturn(eventsThree);

        replayMocks();
        Map<String, Object> map = command.getMapOfCurrentEvents(studyParticipantAssignment);
        verifyMocks();

        assertNotNull("Map is null", map);
        assertEquals("Map size is incorrect", 3, map.size());

        Set<String> keys = map.keySet();
        Date today = new Date();
        Date todayPlusOne = command.shiftStartDayByNumberOfDays(today, 1);
        Date todayPlusTwo = command.shiftStartDayByNumberOfDays(today, 2);
        assertTrue("Keys don't contain today's date", keys.contains(command.formatDateToString(today)));
        assertTrue("Keys don't contain next day ", keys.contains(command.formatDateToString(todayPlusOne)));
        assertTrue("Keys don't contain day after next ", keys.contains(command.formatDateToString(todayPlusTwo)));

        Map <String, Object> valueOne = (Map<String, Object>) map.get(command.formatDateToString(today));
        Map <String, Object> valueTwo = (Map<String, Object>) map.get(command.formatDateToString(todayPlusOne));
        Map <String, Object> valueThree = (Map<String, Object>) map.get(command.formatDateToString(todayPlusTwo));
        assertTrue("Value doesn't contain the right event", valueOne.values().contains(e1));
        assertTrue("ValueTwo doesn't contain the right event", valueTwo.values().contains(e2));
        assertTrue("ValueThree doesn't contain the right event", valueThree.values().contains(e3));
    }


    public void testExecute() throws Exception {
        Participant participantOne = setId(1, createParticipant("Kate", "Kateson"));
        Participant participantTwo = setId(2, createParticipant("Bill", "Billman"));

        StudySite studySite = setId(14, createStudySite(null, null));
        StudyParticipantAssignment assignment = new StudyParticipantAssignment();
        StudyParticipantAssignment assignmentTwo = new StudyParticipantAssignment();
        ScheduledCalendar calendar;
        ScheduledCalendar calendarTwo;

        ScheduledEvent e1, e2, e3, e5, e6, e7;

        calendar = setId(6, new ScheduledCalendar());
        calendar.addArm(new ScheduledArm());

        calendarTwo = setId(7, new ScheduledCalendar());
        calendarTwo.addArm(new ScheduledArm());


        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH);
        int year = c.get(Calendar.YEAR);

        e1 =  createScheduledEvent("C", year, month, day);
        e2 =  createScheduledEvent("O", year, month, day+1);
        e3 =  createScheduledEvent("S", year, month, day+2);

        addEvents(calendar.getScheduledArms().get(0), e1, e2, e3);


        e5 = createScheduledEvent("C", year, month, day +1, new Canceled());
        e6 = createScheduledEvent("O", year, month, day +2, new Occurred());
        e7 = createScheduledEvent("S", year, month, day +3);

        addEvents(calendarTwo.getScheduledArms().get(0), e5, e6, e7);

        assignment.setParticipant(participantOne);
        assignment.setStudySite(studySite);
        assignment.setStartDateEpoch(new Date());
        assignment.setScheduledCalendar(calendar);

        assignmentTwo.setParticipant(participantTwo);
        assignmentTwo.setStudySite(studySite);
        assignmentTwo.setStartDateEpoch(command.shiftStartDayByNumberOfDays(new Date(), 2));
        assignmentTwo.setScheduledCalendar(calendarTwo);

        List<StudyParticipantAssignment> studyParticipantAssignments = new ArrayList<StudyParticipantAssignment>();
        studyParticipantAssignments.add(assignment);
        studyParticipantAssignments.add(assignmentTwo);

        expect(userDao.getAssignments(user)).andReturn(studyParticipantAssignments);


        Collection<ScheduledEvent> eventsForKate = new ArrayList<ScheduledEvent>();
        eventsForKate.add(e1);
        Collection<ScheduledEvent> eventsForBill = new ArrayList<ScheduledEvent>();
        Date startDate = new Date();
        Date tempStartDateOne = command.shiftStartDayByNumberOfDays(startDate, 0);
        expect(scheduledEventDao.getEventsByDate(calendar, tempStartDateOne, tempStartDateOne)).andReturn(eventsForKate);
        expect(scheduledEventDao.getEventsByDate(calendarTwo, tempStartDateOne, tempStartDateOne)).andReturn(eventsForBill);

        Collection<ScheduledEvent> eventsTwoForKate = new ArrayList<ScheduledEvent>();
        eventsTwoForKate.add(e2);
        Collection<ScheduledEvent> eventsTwoForBill = new ArrayList<ScheduledEvent>();
        eventsTwoForBill.add(e5);
        Date tempStartDateTwo = command.shiftStartDayByNumberOfDays(startDate, 1);
        expect(scheduledEventDao.getEventsByDate(calendar, tempStartDateTwo, tempStartDateTwo)).andReturn(eventsTwoForKate);
        expect(scheduledEventDao.getEventsByDate(calendarTwo, tempStartDateTwo, tempStartDateTwo)).andReturn(eventsTwoForBill);

        Collection<ScheduledEvent> eventsThreeForKate = new ArrayList<ScheduledEvent>();
        eventsThreeForKate.add(e3);
        Collection<ScheduledEvent> eventsThreeForBill = new ArrayList<ScheduledEvent>();
        eventsThreeForBill.add(e6);

        Date tempStartDateThree = command.shiftStartDayByNumberOfDays(startDate, 2);
        expect(scheduledEventDao.getEventsByDate(calendar, tempStartDateThree, tempStartDateThree)).andReturn(eventsThreeForKate);
        expect(scheduledEventDao.getEventsByDate(calendarTwo, tempStartDateThree, tempStartDateThree)).andReturn(eventsThreeForBill);

        replayMocks();
        Map<String, Object> map = command.execute();
        verifyMocks();
        assertNotNull("Map is null", map);
        assertEquals("Map size is incorrect", 1, map.size());
        Set<String> keys = map.keySet();
        assertEquals("Keys of the outter map are not the same", "mapOfUserAndCalendar", keys.toArray()[0]);

        Map<String, Object> values = (Map<String, Object>) map.get(keys.toArray()[0]);
        Set<String> dates = values.keySet();
        Date today = new Date();
        Date todayPlusOne = command.shiftStartDayByNumberOfDays(today, 1);
        Date todayPlusTwo = command.shiftStartDayByNumberOfDays(today, 2);
        assertTrue("Keys don't contain today's date", dates.contains(command.formatDateToString(today)));
        assertTrue("Keys don't contain next day ", dates.contains(command.formatDateToString(todayPlusOne)));
        assertTrue("Keys don't contain day after next ", dates.contains(command.formatDateToString(todayPlusTwo)));

        Map <String, Object> valueOne = (Map<String, Object>) values.get(command.formatDateToString(today));
        Map <String, Object> valueTwo = (Map<String, Object>) values.get(command.formatDateToString(todayPlusOne));
        Map <String, Object> valueThree = (Map<String, Object>) values.get(command.formatDateToString(todayPlusTwo));

        assertTrue("Value doesn't contain the right event", valueOne.values().contains(e1));
        assertTrue("ValueTwo doesn't contain the right event", valueTwo.values().contains(e2));
        assertTrue("ValueTwo doesn't contain the right event", valueTwo.values().contains(e5));
        assertTrue("ValueThree doesn't contain the right event", valueThree.values().contains(e3));
        assertTrue("ValueThree doesn't contain the right event", valueThree.values().contains(e6));

        Set<String> valueOneKey = valueOne.keySet();
        assertEquals("Wrong number of participants ", 1, valueOneKey.size());
        String participantKey = (String) valueOneKey.toArray()[0];
        String expectedParticipantKeyOne = participantOne.getFullName() + " - " + e1.getActivity().getName();
        assertEquals("Participants are not the same", expectedParticipantKeyOne, participantKey);
        assertEquals("Date " + today + "has more than one event", 1, valueOne.values().size());

        Set<String> valueTwoKey = valueTwo.keySet();
        assertEquals("Wrong number of participants ", 2, valueTwoKey.size());
        assertEquals("Date " + todayPlusOne + "has more than one event", 2, valueTwo.values().size());

        Set<String> valueThreeKey = valueThree.keySet();
        assertEquals("Wrong number of participants ", 2, valueThreeKey.size());
        assertEquals("Date " + todayPlusTwo + "has more than one event", 2, valueThree.values().size());

    }
}

