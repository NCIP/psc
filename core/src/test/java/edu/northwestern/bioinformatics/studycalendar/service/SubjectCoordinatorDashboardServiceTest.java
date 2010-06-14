package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.core.*;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createSubject;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Occurred;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Scheduled;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Conditional;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityDao;
import edu.northwestern.bioinformatics.studycalendar.configuration.Configuration;
import edu.northwestern.bioinformatics.studycalendar.configuration.MockConfiguration;
import edu.northwestern.bioinformatics.studycalendar.tools.FormatTools;
import edu.nwu.bioinformatics.commons.DateUtils;

import java.util.*;
import static java.util.Calendar.*;
import java.text.SimpleDateFormat;
import java.text.DateFormat;

import static org.easymock.EasyMock.expect;

/**
 * @author Nataliya Shurupova
 */
public class SubjectCoordinatorDashboardServiceTest extends StudyCalendarTestCase {

    private SubjectCoordinatorDashboardService service;
    private ScheduledActivityDao scheduledActivityDao;
    private StudySubjectAssignment subjectAssignment;
    private ScheduledCalendar calendar;

    protected void setUp() throws Exception {
        super.setUp();

        scheduledActivityDao = registerMockFor(ScheduledActivityDao.class);

        service = new SubjectCoordinatorDashboardService();
        service.setScheduledActivityDao(scheduledActivityDao);

        calendar = new ScheduledCalendar();

        subjectAssignment = new StudySubjectAssignment();

        subjectAssignment.setSubject(createSubject("John", "Doe"));
        subjectAssignment.setScheduledCalendar(calendar);
    }

    public void testGetMapOfOverdueEvents() throws Exception {
        ScheduledActivity sa1 = Fixtures.createScheduledActivity("AAA", 2009, 7, 1);
        ScheduledActivity sa2 = Fixtures.createScheduledActivity("BBB", 2009, 5, 3);
        ScheduledActivity sa3 = Fixtures.createScheduledActivity("CCC", 2009, 8, 6);
        List<ScheduledActivity> activities = new ArrayList<ScheduledActivity>();
        activities.add(sa1);
        activities.add(sa2);
        activities.add(sa3);

        Date currentDate = new Date();
        Date endDate = service.shiftStartDayByNumberOfDays(currentDate, -1);
        List<StudySubjectAssignment> assignments = Collections.singletonList(subjectAssignment);

        expect(scheduledActivityDao.getEventsByDate(calendar, null, endDate)).andReturn(activities);

        replayMocks();
        Map<Object, Object> mapOfOverdueEvents = service.getMapOfOverdueEvents(assignments);
        verifyMocks();

        assertNotNull("Map of overdue events is null", mapOfOverdueEvents);
        assertEquals("It's more than one key in the map", 1, mapOfOverdueEvents.keySet().size());

        Set<Object> keys = mapOfOverdueEvents.keySet();
        Object key =  keys.iterator().next();
        assertTrue("Key is not a hash map", key instanceof HashMap);
        HashMap<Subject, Integer> subjectSizeMap = (HashMap<Subject, Integer>)key;
        Subject subject = subjectSizeMap.keySet().iterator().next();

        assertEquals("Subject is not the right one", subjectAssignment.getSubject(), subject);
        assertEquals("Number of events is not the right one", new Integer(3), (Integer)subjectSizeMap.values().iterator().next());

        Collection<Object> keysOfValue = mapOfOverdueEvents.values();
        Object keyOfValue = keysOfValue.iterator().next();
        HashMap<Object, Object> eventsMap = (HashMap<Object, Object>)keyOfValue;
        StudySubjectAssignment ssa = (StudySubjectAssignment)eventsMap.keySet().iterator().next();
        assertEquals("Assignment is not equals", subjectAssignment, ssa );
        Collection<Object> value = eventsMap.values();
        ScheduledActivity sa = (ScheduledActivity)value.iterator().next();
        assertEquals("The earliest event is incorrect", sa2, sa);
    }

    public void testGetMapOfCurrentEvents() throws Exception {
        ScheduledActivity sa1 = createScheduledActivity("AAA", 2009, 9, 1, new Scheduled(null, DateUtils.createDate(2009, AUGUST, 29)));
        ScheduledActivity sa2 = createScheduledActivity("BBB", 2009, 10, 3, new Conditional(null, DateUtils.createDate(2009, SEPTEMBER, 10)));
        ScheduledActivity sa3 = createScheduledActivity("CCC", 2009, 11, 5, new Scheduled(null, DateUtils.createDate(2009, OCTOBER, 15)));
        ScheduledActivity sa4 = createScheduledActivity("DDD", 2009, 10, 15, new Occurred(null, DateUtils.createDate(2009, SEPTEMBER, 15)));
        List<ScheduledActivity> activities = new ArrayList<ScheduledActivity>();
        activities.add(sa1);
        activities.add(sa2);
        activities.add(sa3);
        activities.add(sa4);

        Date currentDate = new Date();
        currentDate = service.shiftStartDayByNumberOfDays(currentDate, 0);
        List<StudySubjectAssignment> assignments = new ArrayList<StudySubjectAssignment>();
        assignments.add(subjectAssignment);

        expect(scheduledActivityDao.getEventsByDate(calendar, currentDate, currentDate)).andReturn(activities);

        replayMocks();
        Map<String, Object> mapOfCurrentEvents = service.getMapOfCurrentEvents(assignments, 1);
        verifyMocks();

        assertNotNull("Map of overdue events is null", mapOfCurrentEvents);
        assertEquals("It's more than one key in the map", 1, mapOfCurrentEvents.keySet().size());

        Set<String> keys = mapOfCurrentEvents.keySet();
        String key =  keys.iterator().next();
        assertTrue("Key is not a hash map", key instanceof String);

        String expectedDate = service.formatDateToString(currentDate);
        expectedDate = expectedDate + " - " + service.convertDateKeyToDayOfTheWeekString(currentDate);
        assertEquals("Key date is not the right one", expectedDate , key);

        Collection<Object> subjectAndEventsCollection = mapOfCurrentEvents.values();
        Object keyOfValue = subjectAndEventsCollection.iterator().next();
        HashMap<Object, Object> subjectAndEvents = (HashMap<Object, Object>)keyOfValue;
        Set<Object> subjectSet = subjectAndEvents.keySet();
        assertTrue("It's more then one subject in the map ", subjectSet.size()==1);
        Subject subject = (Subject)subjectSet.iterator().next();
        assertEquals("Subject is not the expected one", subjectAssignment.getSubject(), subject );

        Collection<Object> valueOfValue = subjectAndEvents.values();
        List<ScheduledActivity> events = (ArrayList<ScheduledActivity>)valueOfValue.iterator().next();

        assertTrue("It's not the expected number of events", events.size()==3);
        assertTrue("Events don't contain scheduled activity AAA", events.contains(sa1));
        assertTrue("Events don't contain scheduled activity BBB", events.contains(sa2));
        assertTrue("Events don't contain scheduled activity CCC", events.contains(sa3));
        assertFalse("Events contain occured activity DDD", events.contains(sa4));
     }

    public void testGetMapOfCurrentEventsForSpecificActivity() throws Exception {
        ScheduledActivity sa1 = createScheduledActivity("AAA", 2009, 9, 1, new Scheduled(null, DateUtils.createDate(2009, AUGUST, 29)));
        ScheduledActivity sa2 = createScheduledActivity("BBB", 2009, 10, 3, new Conditional(null, DateUtils.createDate(2009, SEPTEMBER, 10)));
        ScheduledActivity sa3 = createScheduledActivity("CCC", 2009, 11, 5, new Scheduled(null, DateUtils.createDate(2009, OCTOBER, 15)));
        ScheduledActivity sa4 = createScheduledActivity("DDD", 2009, 10, 15, new Occurred(null, DateUtils.createDate(2009, SEPTEMBER, 15)));
        List<ScheduledActivity> activities = new ArrayList<ScheduledActivity>();
        activities.add(sa1);
        activities.add(sa2);
        activities.add(sa3);
        activities.add(sa4);

        List<ActivityType> activitiesMap = new ArrayList<ActivityType>();
        activitiesMap.add(sa1.getActivity().getType());
        activitiesMap.add(sa2.getActivity().getType());
        activitiesMap.add(sa4.getActivity().getType());

        Date currentDate = new Date();
        currentDate = service.shiftStartDayByNumberOfDays(currentDate, 0);
        expect(scheduledActivityDao.getEventsByDate(calendar, currentDate, currentDate)).andReturn(activities);

        replayMocks();
        Map<String, Object> mapOfCurrentEvents = service.getMapOfCurrentEventsForSpecificActivity(Collections.singletonList(subjectAssignment), 1, activitiesMap);
        verifyMocks();

        assertNotNull("Result is null", mapOfCurrentEvents);

        Collection<Object> subjectAndEventsCollection = mapOfCurrentEvents.values();
        Object keyOfValue = subjectAndEventsCollection.iterator().next();
        HashMap<Object, Object> subjectAndEvents = (HashMap<Object, Object>)keyOfValue;
        Collection<Object> valueOfValue = subjectAndEvents.values();
        List<ScheduledActivity> events = (ArrayList<ScheduledActivity>)valueOfValue.iterator().next();

        assertTrue("It's not the expected number of events", events.size()==3);
        assertTrue("Events don't contain scheduled activity AAA", events.contains(sa1));
        assertTrue("Events don't contain scheduled activity BBB", events.contains(sa2));
        assertTrue("Events don't contain scheduled activity CCC", events.contains(sa3));
        assertFalse("Events contain occured activity DDD", events.contains(sa4));
    }

    public void testConvertDateKeyToDayOfTheWeekSting() throws Exception{
        Date today = DateUtils.createDate(2009, SEPTEMBER, 11);
        String result = service.convertDateKeyToDayOfTheWeekString(today);

        assertNotNull("Result string is null", result);
        assertEquals("Wrong string as a result", "Friday", result);
    }

    public void testConvertNullDateKeyToDayOfTheWeekSting() throws Exception{
        Date today = null;
        String result = service.convertDateKeyToDayOfTheWeekString(today);
        assertNull("Result string is null", result);
    }

    public void testFormatDateToString() throws Exception {
         Date today = DateUtils.createDate(2009, SEPTEMBER, 11);
         String result = service.formatDateToString(today);

         assertNotNull("Result string is null", result);
         assertEquals("Wrong string as a result", "09/11", result);
    }

    public void testShiftStartDayByPositiveNumberOfDays() throws Exception {
        Date today = DateUtils.createDate(2009, SEPTEMBER, 11);
        Date expected = service.shiftStartDayByNumberOfDays(DateUtils.createDate(2009, SEPTEMBER, 14), 0);
        Date result = service.shiftStartDayByNumberOfDays(today, 3);
        assertNotNull("Result date is null", result);
        assertEquals("Result is not the same as expected", expected, result);
    }

    public void testShiftStartDayByNegativeNumberOfDays() throws Exception {
        Date today = DateUtils.createDate(2009, SEPTEMBER, 11);
        Date expected = service.shiftStartDayByNumberOfDays(DateUtils.createDate(2009, SEPTEMBER, 7),0);

        Date result = service.shiftStartDayByNumberOfDays(today, -4);
        assertNotNull("Result date is null", result);
        assertEquals("Result is not the same as expected", expected, result);
    }

    public void testShiftStartDayByZeroNumberOfDays() throws Exception {
        Date today = service.shiftStartDayByNumberOfDays(DateUtils.createDate(2009, SEPTEMBER, 11), 0);
        Date result = service.shiftStartDayByNumberOfDays(today, 0);
        assertNotNull("Result date is null", result);
        assertEquals("Result is not the same as expected", today, result);
    }

    public void testGetEarliestEvent() throws Exception {
        ScheduledActivity sa1 = Fixtures.createScheduledActivity("AAA", 2009, 9, 1);
        ScheduledActivity sa2 = Fixtures.createScheduledActivity("BBB", 2009, 9, 3);
        ScheduledActivity sa3 = Fixtures.createScheduledActivity("CCC", 2009, 9, 2);
        ScheduledActivity sa4 = Fixtures.createScheduledActivity("DDD", 2009, 8, 31);
        ScheduledActivity sa5 = Fixtures.createScheduledActivity("EEE", 2009, 7, 15);
        List<ScheduledActivity> activities = new ArrayList<ScheduledActivity>();
        activities.add(sa1);
        activities.add(sa2);
        activities.add(sa3);
        activities.add(sa4);
        activities.add(sa5);

        ScheduledActivity result = service.getEarliestEvent(activities);
        assertNotNull("Scheduled activity is null", result);
        assertEquals("Scheduled activities are not equals", sa5, result);
    }
}
