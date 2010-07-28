package edu.northwestern.bioinformatics.studycalendar.web.dashboard.subjectcoordinator;

import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Canceled;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectCoordinatorDashboardService;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import gov.nih.nci.cabig.ctms.lang.DateTools;
import static org.easymock.EasyMock.expect;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class ScheduleCommandTest extends StudyCalendarTestCase {
    private User user;
    private String userName;
    private UserDao userDao;
    private ScheduledActivityDao scheduledActivityDao;

    private ScheduleCommand command = new ScheduleCommand();
    private SubjectCoordinatorDashboardService paService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        user = new User();
        userName = "USER NAME";
        user.setName(userName);
        userDao = registerDaoMockFor(UserDao.class);
        userDao = registerMockFor(UserDao.class);
        scheduledActivityDao = registerMockFor(ScheduledActivityDao.class);

        command.setScheduledActivityDao(scheduledActivityDao);
        command.setUserDao(userDao);
        command.setUser(user);
        command.setToDate(3);

        paService = new SubjectCoordinatorDashboardService();
        paService.setScheduledActivityDao(scheduledActivityDao);
    }

    public void testShiftStartDayByNumberOfDays() throws Exception {
        Date startDate = DateTools.createDate(2005, Calendar.AUGUST, 3);
        replayMocks();
        Date actualDate = paService.shiftStartDayByNumberOfDays(startDate, 3);
        verifyMocks();
        assertDayOfDate("Incorrect shift", 2005, Calendar.AUGUST, 6, actualDate);
    }

    public void testFormatDateToString() throws Exception {
        Calendar rightNow = Calendar.getInstance(); 
        DateFormat df = new SimpleDateFormat("MM/dd");
        String expectedDateString = df.format(new Date());
        replayMocks();
        String actualDateString = paService.formatDateToString(rightNow.getTime());
        verifyMocks();
        assertEquals("Expected and Actual formats are not equals ", expectedDateString, actualDateString);
    }

    public void testGetMapOfCurrentEvents() throws Exception {
        Subject subject = setId(11, createSubject("Fred", "Jones"));
        StudySite studySite = setId(14, createStudySite(null, null));
        StudySubjectAssignment assignment = new StudySubjectAssignment();
        ScheduledCalendar calendar;

        ScheduledActivity e1, e2, e3;

        calendar = setId(6, new ScheduledCalendar());
        calendar.addStudySegment(new ScheduledStudySegment());
        calendar.addStudySegment(new ScheduledStudySegment());

        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH);
        int year = c.get(Calendar.YEAR);

        e1 =  createScheduledActivity("C", year, month, day);
        e2 =  createScheduledActivity("O", year, month, day+1);
        e3 =  createScheduledActivity("S", year, month, day+2);

        addEvents(calendar.getScheduledStudySegments().get(0), e1, e2, e3);

        assignment.setSubject(subject);
        assignment.setStudySite(studySite);
        assignment.setStartDate(new Date());
        assignment.setScheduledCalendar(calendar);
        List<StudySubjectAssignment> studySubjectAssignment = new ArrayList<StudySubjectAssignment>();
        studySubjectAssignment.add(assignment);

        Collection<ScheduledActivity> events = new ArrayList<ScheduledActivity>();
        events.add(e1);
        Date startDate = new Date();
        Date tempStartDateOne = paService.shiftStartDayByNumberOfDays(startDate, 0);
        expect(scheduledActivityDao.getEventsByDate(calendar, tempStartDateOne, tempStartDateOne)).andReturn(events);

        Collection<ScheduledActivity> eventsTwo = new ArrayList<ScheduledActivity>();
        eventsTwo.add(e2);
        Date tempStartDateTwo = paService.shiftStartDayByNumberOfDays(startDate, 1);
        expect(scheduledActivityDao.getEventsByDate(calendar, tempStartDateTwo, tempStartDateTwo)).andReturn(eventsTwo);

        Collection<ScheduledActivity> eventsThree = new ArrayList<ScheduledActivity>();
        eventsThree.add(e3);
        Date tempStartDateThree = paService.shiftStartDayByNumberOfDays(startDate, 2);
        expect(scheduledActivityDao.getEventsByDate(calendar, tempStartDateThree, tempStartDateThree)).andReturn(eventsThree);

        replayMocks();
        Map<String, Object> map = paService.getMapOfCurrentEvents(studySubjectAssignment, 3);
        verifyMocks();

        assertNotNull("Map is null", map);
        assertEquals("Map size is incorrect", 3, map.size());

        Set<String> keys = map.keySet();
        Date today = new Date();
        Date todayPlusOne = paService.shiftStartDayByNumberOfDays(today, 1);
        Date todayPlusTwo = paService.shiftStartDayByNumberOfDays(today, 2);

        String todayKey = paService.formatDateToString(today) + " - " + paService.convertDateKeyToDayOfTheWeekString(today);
        String todayPlusOneKey = paService.formatDateToString(todayPlusOne) + " - " + paService.convertDateKeyToDayOfTheWeekString(todayPlusOne);
        String todayPlusTwoKey = paService.formatDateToString(todayPlusTwo) + " - " + paService.convertDateKeyToDayOfTheWeekString(todayPlusTwo);

        assertTrue("Keys don't contain today's date", keys.contains(todayKey));
        assertTrue("Keys don't contain next day ", keys.contains(todayPlusOneKey));
        assertTrue("Keys don't contain day after next ", keys.contains(todayPlusTwoKey));

        Map <String, Object> valueOne = (Map<String, Object>) map.get(todayKey);
        Map <String, Object> valueTwo = (Map<String, Object>) map.get(todayPlusOneKey);
        Map <String, Object> valueThree = (Map<String, Object>) map.get(todayPlusTwoKey);
        assertTrue("Value doesn't contain the right event", ((ArrayList)valueOne.values().iterator().next()).contains(e1));
        assertTrue("ValueTwo doesn't contain the right event", ((ArrayList)valueTwo.values().iterator().next()).contains(e2));
        assertTrue("ValueThree doesn't contain the right event", ((ArrayList)valueThree.values().iterator().next()).contains(e3));
    }


    public void testGetMapOfCurrentEventsForSpecificActivity() throws Exception {
        Subject subject = setId(11, createSubject("Fred", "Jones"));
        StudySite studySite = setId(14, createStudySite(null, null));
        StudySubjectAssignment assignment = new StudySubjectAssignment();
        ScheduledCalendar calendar;

        ScheduledActivity e1, e2, e3;

        calendar = setId(6, new ScheduledCalendar());
        calendar.addStudySegment(new ScheduledStudySegment());
        calendar.addStudySegment(new ScheduledStudySegment());

        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH);
        int year = c.get(Calendar.YEAR);

        e1 =  createScheduledActivity("C", year, month, day);
        e1.setActivity(Fixtures.createActivity("Activity1"));
        e2 =  createScheduledActivity("O", year, month, day+1);
        e2.setActivity(Fixtures.createActivity("Activity2"));
        e3 =  createScheduledActivity("S", year, month, day+2);
        e3.getActivity().setType(Fixtures.createActivityType("INTERVENTION"));

        addEvents(calendar.getScheduledStudySegments().get(0), e1, e2, e3);

        assignment.setSubject(subject);
        assignment.setStudySite(studySite);
        assignment.setStartDate(new Date());
        assignment.setScheduledCalendar(calendar);
        List<StudySubjectAssignment> studySubjectAssignment = new ArrayList<StudySubjectAssignment>();
        studySubjectAssignment.add(assignment);

        List<ActivityType> activities = new ArrayList<ActivityType>();
        activities.add(Fixtures.createActivityType("DISEASE_MEASURE"));
        activities.add(Fixtures.createActivityType("INTERVENTION"));


        Collection<ScheduledActivity> events = new ArrayList<ScheduledActivity>();
        events.add(e1);
        Date startDate = new Date();
        Date tempStartDateOne = paService.shiftStartDayByNumberOfDays(startDate, 0);
        expect(scheduledActivityDao.getEventsByDate(calendar, tempStartDateOne, tempStartDateOne)).andReturn(events);

        Collection<ScheduledActivity> eventsTwo = new ArrayList<ScheduledActivity>();
        eventsTwo.add(e2);
        Date tempStartDateTwo = paService.shiftStartDayByNumberOfDays(startDate, 1);
        expect(scheduledActivityDao.getEventsByDate(calendar, tempStartDateTwo, tempStartDateTwo)).andReturn(eventsTwo);

        Collection<ScheduledActivity> eventsThree = new ArrayList<ScheduledActivity>();
        eventsThree.add(e3);
        Date tempStartDateThree = paService.shiftStartDayByNumberOfDays(startDate, 2);
        expect(scheduledActivityDao.getEventsByDate(calendar, tempStartDateThree, tempStartDateThree)).andReturn(eventsThree);

        replayMocks();
        Map<String, Object> map = paService.getMapOfCurrentEventsForSpecificActivity(studySubjectAssignment, 3, activities);
        verifyMocks();

        assertNotNull("Map is null", map);
        assertEquals("Map size is incorrect: " + map, 1, map.size());

        Set<String> keys = map.keySet();
        Date today = new Date();
        Date todayPlusOne = paService.shiftStartDayByNumberOfDays(today, 1);
        Date todayPlusTwo = paService.shiftStartDayByNumberOfDays(today, 2);

        String todayKey = paService.formatDateToString(today) + " - " + paService.convertDateKeyToDayOfTheWeekString(today);
        String todayPlusOneKey = paService.formatDateToString(todayPlusOne) + " - " + paService.convertDateKeyToDayOfTheWeekString(todayPlusOne);
        String todayPlusTwoKey = paService.formatDateToString(todayPlusTwo) + " - " + paService.convertDateKeyToDayOfTheWeekString(todayPlusTwo);

//        assertTrue("Keys don't contain today's date", keys.contains(todayKey));
//        assertTrue("Keys don't contain next day ", keys.contains(todayPlusOneKey));
        assertTrue("Keys don't contain day after next ", keys.contains(todayPlusTwoKey));

        Map <String, Object> valueOne = (Map<String, Object>) map.get(todayKey);
        Map <String, Object> valueTwo = (Map<String, Object>) map.get(todayPlusOneKey);
        Map <String, Object> valueThree = (Map<String, Object>) map.get(todayPlusTwoKey);
//        assertEquals("Value is not empty", 0, valueOne.values().size());
//        assertEquals("ValueTwo is not empty", 0, valueTwo.values().size());
        assertEquals("ValueThree is empty", 1, valueThree.values().size());
        assertTrue("ValueThree doesn't contain the right event", ((ArrayList)valueThree.values().iterator().next()).contains(e3));
    }


    public void testExecute() throws Exception {
        Subject subjectOne = setId(1, createSubject("Kate", "Kateson"));
        Subject subjectTwo = setId(2, createSubject("Bill", "Billman"));

        StudySite studySite = setId(14, createStudySite(null, null));
        StudySubjectAssignment assignment = new StudySubjectAssignment();
        StudySubjectAssignment assignmentTwo = new StudySubjectAssignment();
        ScheduledCalendar calendar;
        ScheduledCalendar calendarTwo;

        ScheduledActivity e1, e2, e3, e5, e6, e7;

        List<ActivityType> activities = new ArrayList<ActivityType>();
        activities.add(Fixtures.createActivityType("LAB_TEST"));

        calendar = setId(6, new ScheduledCalendar());
        calendar.addStudySegment(new ScheduledStudySegment());

        calendarTwo = setId(7, new ScheduledCalendar());
        calendarTwo.addStudySegment(new ScheduledStudySegment());


        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH);
        int year = c.get(Calendar.YEAR);

        e1 =  createScheduledActivity("C", year, month, day);
        e2 =  createScheduledActivity("O", year, month, day+1);
        e3 =  createScheduledActivity("S", year, month, day+2);

        addEvents(calendar.getScheduledStudySegments().get(0), e1, e2, e3);


        e5 = createScheduledActivity("C", year, month, day +1, new Canceled());
        e6 = createScheduledActivity("O", year, month, day +2);
        e7 = createScheduledActivity("S", year, month, day +3);

        addEvents(calendarTwo.getScheduledStudySegments().get(0), e5, e6, e7);

        assignment.setSubject(subjectOne);
        assignment.setStudySite(studySite);
        assignment.setStartDate(new Date());
        assignment.setScheduledCalendar(calendar);

        assignmentTwo.setSubject(subjectTwo);
        assignmentTwo.setStudySite(studySite);
        assignmentTwo.setStartDate(paService.shiftStartDayByNumberOfDays(new Date(), 2));
        assignmentTwo.setScheduledCalendar(calendarTwo);

        List<StudySubjectAssignment> studySubjectAssignments = new ArrayList<StudySubjectAssignment>();
        studySubjectAssignments.add(assignment);
        studySubjectAssignments.add(assignmentTwo);

        expect(userDao.getAssignments(user)).andReturn(studySubjectAssignments);


        Collection<ScheduledActivity> eventsForKate = new ArrayList<ScheduledActivity>();
        eventsForKate.add(e1);
        Collection<ScheduledActivity> eventsForBill = new ArrayList<ScheduledActivity>();
        Date startDate = new Date();
        Date tempStartDateOne = paService.shiftStartDayByNumberOfDays(startDate, 0);
        expect(scheduledActivityDao.getEventsByDate(calendar, tempStartDateOne, tempStartDateOne)).andReturn(eventsForKate);
        expect(scheduledActivityDao.getEventsByDate(calendarTwo, tempStartDateOne, tempStartDateOne)).andReturn(eventsForBill);

        Collection<ScheduledActivity> eventsTwoForKate = new ArrayList<ScheduledActivity>();
        eventsTwoForKate.add(e2);
        Collection<ScheduledActivity> eventsTwoForBill = new ArrayList<ScheduledActivity>();
        eventsTwoForBill.add(e5);
        Date tempStartDateTwo = paService.shiftStartDayByNumberOfDays(startDate, 1);
        expect(scheduledActivityDao.getEventsByDate(calendar, tempStartDateTwo, tempStartDateTwo)).andReturn(eventsTwoForKate);
        expect(scheduledActivityDao.getEventsByDate(calendarTwo, tempStartDateTwo, tempStartDateTwo)).andReturn(eventsTwoForBill);

        Collection<ScheduledActivity> eventsThreeForKate = new ArrayList<ScheduledActivity>();
        eventsThreeForKate.add(e3);
        Collection<ScheduledActivity> eventsThreeForBill = new ArrayList<ScheduledActivity>();
        eventsThreeForBill.add(e6);

        Date tempStartDateThree = paService.shiftStartDayByNumberOfDays(startDate, 2);
        expect(scheduledActivityDao.getEventsByDate(calendar, tempStartDateThree, tempStartDateThree)).andReturn(eventsThreeForKate);
        expect(scheduledActivityDao.getEventsByDate(calendarTwo, tempStartDateThree, tempStartDateThree)).andReturn(eventsThreeForBill);

        command.setActivityTypes(activities);

        replayMocks();
        Map<String, Object> map = command.execute(paService);
        verifyMocks();
        assertNotNull("Map is null", map);
        assertEquals("Map size is incorrect", 2, map.size());
        Set<String> keys = map.keySet();
        assertTrue("Keys of the outter map are not the same", keys.contains("mapOfUserAndCalendar"));
        assertTrue("Keys of the outter map are not the same", keys.contains("numberOfDays"));


        Map<String, Object> values = (Map<String, Object>) map.get("mapOfUserAndCalendar");
        Set<String> dates = values.keySet();
        Date today = new Date();
        Date todayPlusOne = paService.shiftStartDayByNumberOfDays(today, 1);
        Date todayPlusTwo = paService.shiftStartDayByNumberOfDays(today, 2);

        String todayKey = paService.formatDateToString(today) + " - " + paService.convertDateKeyToDayOfTheWeekString(today);
        String todayPlusOneKey = paService.formatDateToString(todayPlusOne) + " - " + paService.convertDateKeyToDayOfTheWeekString(todayPlusOne);
        String todayPlusTwoKey = paService.formatDateToString(todayPlusTwo) + " - " + paService.convertDateKeyToDayOfTheWeekString(todayPlusTwo);

        assertTrue("Keys don't contain today's date", dates.contains(todayKey));
        assertTrue("Keys don't contain next day ", dates.contains(todayPlusOneKey));
        assertTrue("Keys don't contain day after next ", dates.contains(todayPlusTwoKey));

        Map <String, Object> valueOne = (Map<String, Object>) values.get(todayKey);
        Map <String, Object> valueTwo = (Map<String, Object>) values.get(todayPlusOneKey);
        Map <String, Object> valueThree = (Map<String, Object>) values.get(todayPlusTwoKey);

        assertEquals("ValueOne doesn't contain the right number of events", 1, valueOne.values().size());
        assertEquals("ValueTwo doesn't contain the right number of events", 1, valueTwo.values().size());
        assertEquals("ValueThree doesn't contain the right number of events", 2, valueThree.values().size());


        Set<String> valueOneKey = valueOne.keySet();
        assertEquals("Wrong number of subjects ", 1, valueOneKey.size());
        Subject subjectKey = (Subject) valueOneKey.toArray()[0];
        String expectedSubjectKeyOne = subjectOne.getFullName();
        assertEquals("Subjects are not the same", expectedSubjectKeyOne, subjectKey.getFullName());
        assertEquals("Date " + today + "has more than one event", 1, valueOne.values().size());

        Set<String> valueTwoKey = valueTwo.keySet();
        assertEquals("Wrong number of subjects ", 1, valueTwoKey.size());
        assertEquals("Date " + todayPlusOne + "has more than one event", 1, valueTwo.values().size());

        Set<String> valueThreeKey = valueThree.keySet();
        assertEquals("Wrong number of subjects ", 2, valueThreeKey.size());
        assertEquals("Date " + todayPlusTwo + "has more than one event", 2, valueThree.values().size());

    }


    public void testExecuteWithDifferentActivityTypes() throws Exception {
        Subject subjectOne = setId(1, createSubject("Kate", "Kateson"));
        Subject subjectTwo = setId(2, createSubject("Bill", "Billman"));

        StudySite studySite = setId(14, createStudySite(null, null));
        StudySubjectAssignment assignment = new StudySubjectAssignment();
        StudySubjectAssignment assignmentTwo = new StudySubjectAssignment();
        ScheduledCalendar calendar;
        ScheduledCalendar calendarTwo;

        ScheduledActivity e1, e2, e3, e5, e6, e7;

        List<ActivityType> activities = new ArrayList<ActivityType>();
        activities.add(Fixtures.createActivityType("DISEASE_MEASURE"));
        activities.add(Fixtures.createActivityType("INTERVENTION"));
        activities.add(Fixtures.createActivityType("PROCEDURE"));

        calendar = setId(6, new ScheduledCalendar());
        calendar.addStudySegment(new ScheduledStudySegment());

        calendarTwo = setId(7, new ScheduledCalendar());
        calendarTwo.addStudySegment(new ScheduledStudySegment());


        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH);
        int year = c.get(Calendar.YEAR);

        e1 =  createScheduledActivity("C", year, month, day);
        e1.getActivity().setType(Fixtures.createActivityType("PROCEDURE"));
        e2 =  createScheduledActivity("O", year, month, day+1);
        e2.getActivity().setType(Fixtures.createActivityType("INTERVENTION"));
        e3 =  createScheduledActivity("S", year, month, day+2);

        addEvents(calendar.getScheduledStudySegments().get(0), e1, e2, e3);


        e5 = createScheduledActivity("C", year, month, day +1);
        e6 = createScheduledActivity("O", year, month, day +2);
        e6.getActivity().setType(Fixtures.createActivityType("INTERVENTION"));
        e7 = createScheduledActivity("S", year, month, day +3);


        addEvents(calendarTwo.getScheduledStudySegments().get(0), e5, e6, e7);

        assignment.setSubject(subjectOne);
        assignment.setStudySite(studySite);
        assignment.setStartDate(new Date());
        assignment.setScheduledCalendar(calendar);

        assignmentTwo.setSubject(subjectTwo);
        assignmentTwo.setStudySite(studySite);
        assignmentTwo.setStartDate(paService.shiftStartDayByNumberOfDays(new Date(), 2));
        assignmentTwo.setScheduledCalendar(calendarTwo);

        List<StudySubjectAssignment> studySubjectAssignments = new ArrayList<StudySubjectAssignment>();
        studySubjectAssignments.add(assignment);
        studySubjectAssignments.add(assignmentTwo);

        expect(userDao.getAssignments(user)).andReturn(studySubjectAssignments);


        Collection<ScheduledActivity> eventsForKate = new ArrayList<ScheduledActivity>();
        eventsForKate.add(e1);
        Collection<ScheduledActivity> eventsForBill = new ArrayList<ScheduledActivity>();
        Date startDate = new Date();
        Date tempStartDateOne = paService.shiftStartDayByNumberOfDays(startDate, 0);
        expect(scheduledActivityDao.getEventsByDate(calendar, tempStartDateOne, tempStartDateOne)).andReturn(eventsForKate);
        expect(scheduledActivityDao.getEventsByDate(calendarTwo, tempStartDateOne, tempStartDateOne)).andReturn(eventsForBill);

        Collection<ScheduledActivity> eventsTwoForKate = new ArrayList<ScheduledActivity>();
        eventsTwoForKate.add(e2);
        Collection<ScheduledActivity> eventsTwoForBill = new ArrayList<ScheduledActivity>();
        eventsTwoForBill.add(e5);
        Date tempStartDateTwo = paService.shiftStartDayByNumberOfDays(startDate, 1);
        expect(scheduledActivityDao.getEventsByDate(calendar, tempStartDateTwo, tempStartDateTwo)).andReturn(eventsTwoForKate);
        expect(scheduledActivityDao.getEventsByDate(calendarTwo, tempStartDateTwo, tempStartDateTwo)).andReturn(eventsTwoForBill);

        Collection<ScheduledActivity> eventsThreeForKate = new ArrayList<ScheduledActivity>();
        eventsThreeForKate.add(e3);
        Collection<ScheduledActivity> eventsThreeForBill = new ArrayList<ScheduledActivity>();
        eventsThreeForBill.add(e6);

        Date tempStartDateThree = paService.shiftStartDayByNumberOfDays(startDate, 2);
        expect(scheduledActivityDao.getEventsByDate(calendar, tempStartDateThree, tempStartDateThree)).andReturn(eventsThreeForKate);
        expect(scheduledActivityDao.getEventsByDate(calendarTwo, tempStartDateThree, tempStartDateThree)).andReturn(eventsThreeForBill);

        command.setActivityTypes(activities);

        replayMocks();
        Map<String, Object> map = command.execute(paService);
        verifyMocks();
        assertNotNull("Map is null", map);
        assertEquals("Map size is incorrect", 2, map.size());
        Set<String> keys = map.keySet();
        assertTrue("Keys of the outter map are not the same", keys.contains("mapOfUserAndCalendar"));
        assertTrue("Keys of the outter map are not the same", keys.contains("numberOfDays"));

        Map<String, Object> values = (Map<String, Object>) map.get("mapOfUserAndCalendar");
        Set<String> dates = values.keySet();
        Date today = new Date();
        Date todayPlusOne = paService.shiftStartDayByNumberOfDays(today, 1);
        Date todayPlusTwo = paService.shiftStartDayByNumberOfDays(today, 2);

        String todayKey = paService.formatDateToString(today) + " - " + paService.convertDateKeyToDayOfTheWeekString(today);
        String todayPlusOneKey = paService.formatDateToString(todayPlusOne) + " - " + paService.convertDateKeyToDayOfTheWeekString(todayPlusOne);
        String todayPlusTwoKey = paService.formatDateToString(todayPlusTwo) + " - " + paService.convertDateKeyToDayOfTheWeekString(todayPlusTwo);

        assertTrue("Keys don't contain today's date", dates.contains(todayKey));
        assertTrue("Keys don't contain next day ", dates.contains(todayPlusOneKey));
        assertTrue("Keys don't contain day after next ", dates.contains(todayPlusTwoKey));

        Map <String, Object> valueOne = (Map<String, Object>) values.get(todayKey);
        Map <String, Object> valueTwo = (Map<String, Object>) values.get(todayPlusOneKey);
        Map <String, Object> valueThree = (Map<String, Object>) values.get(todayPlusTwoKey);

        assertEquals("ValueOne doesn't contain the right number of events", 1, valueOne.values().size());
        assertEquals("ValueTwo doesn't contain the right number of events", 1, valueTwo.values().size());
        assertEquals("ValueThree doesn't contain the right number of events", 1, valueThree.values().size());

        Set<String> valueOneKey = valueOne.keySet();
        assertEquals("Wrong number of subjects ", 1, valueOneKey.size());
        Subject subjectKey = (Subject)valueOneKey.toArray()[0];
        String expectedSubjectKeyOne = subjectOne.getFullName() ;
        assertEquals("Subjects are not the same", expectedSubjectKeyOne, subjectKey.getFullName());
        assertEquals("Date " + today + "has more than one event", 1, valueOne.values().size());

        Set<String> valueTwoKey = valueTwo.keySet();
        assertEquals("Wrong number of subjects ", 1, valueTwoKey.size());
        assertEquals("Date " + todayPlusOne + "has more than one event", 1, valueTwo.values().size());

        Set<String> valueThreeKey = valueThree.keySet();
        assertEquals("Wrong number of subjects ", 1, valueThreeKey.size());
        assertEquals("Date " + todayPlusTwo + "has more than one event", 1, valueThree.values().size());

    }


}

