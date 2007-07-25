package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedEventDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedEvent;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

import java.util.List;
import java.util.ArrayList;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import org.easymock.IArgumentMatcher;
import org.easymock.classextension.EasyMock;

/**
 * @author Rhett Sutphin
 * @author Jaron Sampson
 */
public class ManagePeriodEventsCommandTest extends StudyCalendarTestCase {
    private ManagePeriodEventsCommand command;
    private ActivityDao activityDao;
    private Period period;
    private PlannedEventDao plannedEventDao;

    private List<Activity> activities;

    protected void setUp() throws Exception {
        super.setUp();
        period = createPeriod("src", 13, 7, 2);
        activityDao = registerMockFor(ActivityDao.class);
        plannedEventDao = registerMockFor(PlannedEventDao.class);
        initCommand();
        activities = new ArrayList<Activity>();
        activities.add(setId(3, createNamedInstance("Three", Activity.class)));
        activities.add(setId(4, createNamedInstance("Four", Activity.class)));
        activities.add(setId(5, createNamedInstance("Five", Activity.class)));
        activities.add(setId(6, createNamedInstance("Six", Activity.class)));
        activities.add(setId(7, createNamedInstance("Seven", Activity.class)));
    }

    public void testInitializeGrid() throws Exception {
        period.addPlannedEvent(createPlannedEvent( 0, 2, "Det A", 02));
        period.addPlannedEvent(createPlannedEvent( 0, 5, "Det B", 05));
        period.addPlannedEvent(createPlannedEvent( 2, 7, 27));
        period.addPlannedEvent(createPlannedEvent( 3, 5, 35));
        period.addPlannedEvent(createPlannedEvent( 4, 3, "Det C", 43));
        period.addPlannedEvent(createPlannedEvent( 4, 3, "Det C", 143));
        period.addPlannedEvent(createPlannedEvent( 4, 4, "Det C", 44));
        assertEquals(7, period.getPlannedEvents().size());

        initCommand();
        assertEquals("Wrong number of rows in grid", 6, command.getGrid().size());

        assertGridRow(command.getGrid().get(0), activities.get(0), "Det A", null, 02, null, null, null, null, null);
        assertGridRow(command.getGrid().get(1), activities.get(0), "Det B", null, null, null, null, 05, null, null);
        assertGridRow(command.getGrid().get(2), activities.get(2), null,    null, null, null, null, null, null, 27);
        assertGridRow(command.getGrid().get(3), activities.get(3), null,    null, null, null, null, 35, null, null);
        assertGridRow(command.getGrid().get(5), activities.get(4), "Det C",    null, null, 143, null, null, null, null);
        assertGridRow(command.getGrid().get(4), activities.get(4), "Det C", null, null, 43, 44, null, null, null);
    }

    private void assertGridRow(
        ManagePeriodEventsCommand.GridRow actual, Activity expectedActivity, String expectedDetails, Integer... eventIds
    ) {
        assertEquals("Wrong activity", expectedActivity, actual.getActivity());
        assertEquals("Wrong details", expectedDetails, actual.getDetails());
        assertEqualArrays("Wrong counts", eventIds, actual.getEventIds().toArray(new Integer[0]));
    }

    public void testApplyToBlank() throws Exception {
        ManagePeriodEventsCommand.GridRow row1 =
                createGridRow(activities.get(0), "Det A", null, 02, null, null, null, null, null);
        row1.setUpdated(true);
        row1.setAddition(true);

        initCommand();

        command.getGrid().add(row1);
        PlannedEvent event1 = new PlannedEvent();
        event1.setDetails(row1.getDetails());
        event1.setActivity(row1.getActivity());
        plannedEventDao.save(eqPlannedEvent(event1));

        replayMocks();
        command.apply();
        verifyMocks();

        assertEquals("Event was not added" , 1, period.getPlannedEvents().size());
        assertEquals("Details not updated", "Det A", period.getPlannedEvents().get(0).getDetails());
    }

//    public void testApplyDoesNotAddOverExisting() throws Exception {
//        PlannedEvent expectedEvent = createPlannedEvent(3, 4, 25);
//
//        period.addPlannedEvent(expectedEvent);
//        initCommand();
//
//        replayMocks();
//        command.apply();
//        verifyMocks();
//
//        assertEquals(1, period.getPlannedEvents().size());
//        assertSame(expectedEvent, period.getPlannedEvents().get(0));
//    }

    public void testApplyAddSecond() throws Exception {
        PlannedEvent expectedEvent = createPlannedEvent(3, 4, 25);

        period.addPlannedEvent(expectedEvent);
        initCommand();

        ManagePeriodEventsCommand.GridRow gr = command.getGrid().get(0);
        gr.setUpdated(true);
        gr.setAddition(true);
        plannedEventDao.save(eqPlannedEvent(expectedEvent));

        replayMocks();
        command.apply();
        verifyMocks();

        assertEquals(2, period.getPlannedEvents().size());
        assertSame(period.getPlannedEvents().get(0), expectedEvent);
        assertNotSame(period.getPlannedEvents().get(1), expectedEvent);
    }

    public void testApplyRemove() throws Exception {
        PlannedEvent existingEvent = createPlannedEvent(4, 2, 23);
        period.addPlannedEvent(existingEvent);
        initCommand();

        ManagePeriodEventsCommand.GridRow rowToRemove = command.getGrid().get(0);
        rowToRemove.setUpdated(true);
        rowToRemove.setColumnNumber(existingEvent.getDay()-1);
        rowToRemove.setAddition(false);
        plannedEventDao.delete(eqPlannedEvent(existingEvent));
        replayMocks();
        command.apply();
        verifyMocks();
        assertEquals(0, period.getPlannedEvents().size());
    }

    public void testApplyChangeDetailsDirectly() throws Exception {
        PlannedEvent existingEvent = createPlannedEvent(4, 2, "Det A", 23);
        period.addPlannedEvent(existingEvent);
        initCommand();

        ManagePeriodEventsCommand.GridRow rowToUpdate = command.getGrid().get(0);
        rowToUpdate.setUpdated(true);
        rowToUpdate.setColumnNumber(-1);
        rowToUpdate.setDetails("Det B");
        expect(plannedEventDao.getById(existingEvent.getId())).andReturn(existingEvent);
        plannedEventDao.save(eqPlannedEvent(existingEvent));
        replayMocks();
        command.apply();
        verifyMocks();

        assertEquals(1, period.getPlannedEvents().size());
        PlannedEvent actual = period.getPlannedEvents().get(0);
        assertEquals("Details not updated", "Det B", actual.getDetails());
    }

    public void testApplyAnotherWithDifferentDetails() throws Exception {
        PlannedEvent existingEvent = createPlannedEvent(4, 2, "Det A", 02);
        period.addPlannedEvent(existingEvent);
        initCommand();

        ManagePeriodEventsCommand.GridRow row =
                createGridRow(activities.get(4), "Det B", null, null, 03, null, null, null, null);
        row.setUpdated(true);
        row.setAddition(true);
        command.getGrid().add(row);
        PlannedEvent addedEvent = createPlannedEvent(4, 2, row.getDetails(), 03);
        plannedEventDao.save(eqPlannedEvent(addedEvent));
        replayMocks();
        command.apply();
        verifyMocks();

        assertEquals(2, period.getPlannedEvents().size());
        assertEquals("Details wrong on old event", "Det A", period.getPlannedEvents().get(0).getDetails());
        assertEquals("Details wrong on new event", "Det B", period.getPlannedEvents().get(1).getDetails());
    }


    public static PlannedEvent eqPlannedEvent(PlannedEvent event) {
        EasyMock.reportMatcher(new PlannedEventMatcher(event));
        return null;
    }

    private static class PlannedEventMatcher implements IArgumentMatcher {
        private PlannedEvent expectedPlannedEvent;

        public PlannedEventMatcher(PlannedEvent expectedPlannedEvent) {
            this.expectedPlannedEvent = expectedPlannedEvent;
        }

        public boolean matches(Object object) {
            if(!(object instanceof PlannedEvent)) {
                return false;
            }

            PlannedEvent actual =
                    (PlannedEvent) object;

            if (expectedPlannedEvent.getActivity() != null ?
                    !expectedPlannedEvent.getActivity().equals(actual.getActivity()) :
                    actual.getActivity() != null
                && expectedPlannedEvent.getDetails() != null ?
                    !expectedPlannedEvent.getDetails().equals(actual.getDetails()) :
                    actual.getDetails() != null
                )
                return false;


            return true;
        }

        public void appendTo(StringBuffer sb) {
            sb.append("PlannedEvent activity=").append(expectedPlannedEvent.getActivity());
        }
    }

    private void initCommand() {
        command = new ManagePeriodEventsCommand(period, plannedEventDao);
    }

    private PlannedEvent createPlannedEvent(int activityId, int day, Integer id) {
        return createPlannedEvent(activityId, day, null, id);
    }

    private PlannedEvent createPlannedEvent(int activityIndex, int day, String details, Integer id) {
        Activity activity = activities.get(activityIndex);
        PlannedEvent evt = Fixtures.createPlannedEvent(activity.getName(), day);
        evt.setId(id);
        evt.setActivity(activity);
        evt.setDetails(details);
        return evt;
    }

    private ManagePeriodEventsCommand.GridRow createGridRow(Activity activity, String details, Integer... eventIds) {
        ManagePeriodEventsCommand.GridRow row = new ManagePeriodEventsCommand.GridRow(activity, details, eventIds.length);
        for (int i = 0; i < eventIds.length; i++) {
            row.getEventIds().set(i, eventIds[i]);
        }
        return row;
    }
}
