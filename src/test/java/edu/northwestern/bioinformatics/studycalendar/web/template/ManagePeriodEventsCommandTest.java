package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.PlannedEventDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;

import java.util.List;
import java.util.ArrayList;

import static org.easymock.EasyMock.*;
import org.easymock.IArgumentMatcher;
import org.easymock.classextension.EasyMock;
import static gov.nih.nci.cabig.ctms.lang.ComparisonTools.*;

/**
 * @author Rhett Sutphin
 * @author Jaron Sampson
 */
public class ManagePeriodEventsCommandTest extends StudyCalendarTestCase {
    private ManagePeriodEventsCommand command;
    private Period period;
    private PlannedEventDao plannedEventDao;
    private AmendmentService amendmentService;

    private List<Activity> activities;
    private Study study;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        period = createPeriod("src", 13, 7, 2);
        plannedEventDao = registerMockFor(PlannedEventDao.class);
        amendmentService = registerMockFor(AmendmentService.class);

        study = Fixtures.createBasicTemplate();
        study.getPlannedCalendar().getEpochs().get(1).getArms().get(0).addPeriod(period);
        study.setDevelopmentAmendment(new Amendment("Dev"));
        initCommand();
        activities = new ArrayList<Activity>();
        activities.add(setId(3, createNamedInstance("Three", Activity.class)));
        activities.add(setId(4, createNamedInstance("Four", Activity.class)));
        activities.add(setId(5, createNamedInstance("Five", Activity.class)));
        activities.add(setId(6, createNamedInstance("Six", Activity.class)));
        activities.add(setId(7, createNamedInstance("Seven", Activity.class)));
    }

    public void testInitializeGrid() throws Exception {
        period.addPlannedEvent(createPlannedEvent( 0, 2, "Det A", 2, null));
        period.addPlannedEvent(createPlannedEvent( 0, 5, "Det B", 5, null));
        period.addPlannedEvent(createPlannedEvent( 2, 7, 27));
        period.addPlannedEvent(createPlannedEvent( 3, 5, 35));
        period.addPlannedEvent(createPlannedEvent( 4, 3, "Det C", 43, null));
        period.addPlannedEvent(createPlannedEvent( 4, 3, "Det C", 143, null));
        period.addPlannedEvent(createPlannedEvent( 4, 4, "Det C", 44, null));
        assertEquals(7, period.getPlannedEvents().size());

        initCommand();
        assertEquals("Wrong number of rows in grid", 6, command.getGrid().size());

        assertGridRow(command.getGrid().get(0), activities.get(0), "Det A", null, 2, null, null, null, null, null);
        assertGridRow(command.getGrid().get(1), activities.get(0), "Det B", null, null, null, null, 5, null, null);
        assertGridRow(command.getGrid().get(2), activities.get(2), null, null, null, null, null, null, null, 27);
        assertGridRow(command.getGrid().get(3), activities.get(3), null, null, null, null, null, 35, null, null);
        assertGridRow(command.getGrid().get(5), activities.get(4), "Det C", null, null, 143, null, null, null, null);
        assertGridRow(command.getGrid().get(4), activities.get(4), "Det C", null, null, 43, 44, null, null, null);
    }

    private void assertGridRow(
        ManagePeriodEventsCommand.GridRow actual, Activity expectedActivity, String expectedDetails,
        Integer... eventIds
    ) {
        assertEquals("Wrong activity", expectedActivity, actual.getActivity());
        assertEquals("Wrong details", expectedDetails, actual.getDetails());
        assertEqualArrays("Wrong counts", eventIds, actual.getEventIds().toArray(new Integer[0]));
    }

    public void testApplyToBlank() throws Exception {
        ManagePeriodEventsCommand.GridRow row1 =
                createGridRow(activities.get(0), "Det A", null, null, 2, null, null, null, null, null );
        row1.setUpdated(true);
        row1.setAddition(true);

        initCommand();

        command.getGrid().add(row1);
        PlannedActivity event1 = new PlannedActivity();
        event1.setDetails(row1.getDetails());
        event1.setActivity(row1.getActivity());
        amendmentService.updateDevelopmentAmendment(same(period), addFor(event1));

        replayMocks();
        command.apply();
        verifyMocks();

        assertEquals("Event was added directory" , 0, period.getPlannedEvents().size());
    }

    public void testApplyDoesNotAddOverExisting() throws Exception {
        PlannedActivity expectedEvent = createPlannedEvent(3, 4, 25);

        period.addPlannedEvent(expectedEvent);
        initCommand();

        replayMocks();
        command.apply();
        verifyMocks();

        assertEquals(1, period.getPlannedEvents().size());
        assertSame(expectedEvent, period.getPlannedEvents().get(0));
    }

    public void testApplyAddSecond() throws Exception {
        PlannedActivity expectedEvent = createPlannedEvent(3, 4, 25);

        period.addPlannedEvent(expectedEvent);
        initCommand();

        ManagePeriodEventsCommand.GridRow gr = command.getGrid().get(0);
        gr.setUpdated(true);
        gr.setAddition(true);

        amendmentService.updateDevelopmentAmendment(same(period), addFor(expectedEvent));

        replayMocks();
        command.apply();
        verifyMocks();

        assertEquals(1, period.getPlannedEvents().size());
        assertSame(period.getPlannedEvents().get(0), expectedEvent);
    }

    public void testApplyRemove() throws Exception {
        PlannedActivity existingEvent = createPlannedEvent(4, 2, 23);
        period.addPlannedEvent(existingEvent);
        initCommand();

        ManagePeriodEventsCommand.GridRow rowToRemove = command.getGrid().get(0);
        rowToRemove.setUpdated(true);
        rowToRemove.setColumnNumber(existingEvent.getDay()-1);
        rowToRemove.setAddition(false);

        amendmentService.updateDevelopmentAmendment(same(period), removeFor(existingEvent));

        replayMocks();
        command.apply();
        verifyMocks();
        assertEquals("Event should not be directly removed", 1, period.getPlannedEvents().size());
    }

    public void testApplyChangeDetailsDirectly() throws Exception {
        PlannedActivity existingEvent = createPlannedEvent(4, 2, "Det A", 23, null);
        period.addPlannedEvent(existingEvent);
        initCommand();

        ManagePeriodEventsCommand.GridRow rowToUpdate = command.getGrid().get(0);
        rowToUpdate.setUpdated(true);
        rowToUpdate.setColumnNumber(-1);
        rowToUpdate.setDetails("Det B");

        expect(plannedEventDao.getById(23)).andReturn(existingEvent);
        amendmentService.updateDevelopmentAmendment(existingEvent,
            PropertyChange.create("details", "Det A", "Det B"));

        replayMocks();
        command.apply();
        verifyMocks();

        assertEquals(1, period.getPlannedEvents().size());
        PlannedActivity actual = period.getPlannedEvents().get(0);
        assertEquals("Details should not be updated in place", "Det A", actual.getDetails());
    }

    public void testApplyConditionalDetailsDirectly() throws Exception {
        String initialConditionalDetails = "InitialConditionalDetails";
        String expectedConditionalDetails = "ExpectedConditionalDetails";
        PlannedActivity existingEvent = createPlannedEvent(4, 2, "Det A", 23, initialConditionalDetails);
        period.addPlannedEvent(existingEvent);
        initCommand();

        ManagePeriodEventsCommand.GridRow rowToUpdate = command.getGrid().get(0);
        rowToUpdate.setUpdated(true);
        rowToUpdate.setConditionalUpdated(true);
        rowToUpdate.setConditionalDetails(expectedConditionalDetails);

        expect(plannedEventDao.getById(23)).andReturn(existingEvent);
        amendmentService.updateDevelopmentAmendment(existingEvent,
            PropertyChange.create("condition", initialConditionalDetails, expectedConditionalDetails));

        replayMocks();
        command.apply();
        verifyMocks();

        assertEquals(1, period.getPlannedEvents().size());
        PlannedActivity actual = period.getPlannedEvents().get(0);
        assertEquals("ConditionalDetails should not be directly updated", initialConditionalDetails,
            actual.getCondition());
    }

    public void testApplyConditionalDetailsWithUncheckedCheckbox() throws Exception {
        String expectedConditionalDetails = "ExpectedConditionalDetails";
        PlannedActivity existingEvent = createPlannedEvent(4, 2, 23);
        period.addPlannedEvent(existingEvent);
        initCommand();

        ManagePeriodEventsCommand.GridRow rowToUpdate = command.getGrid().get(0);
        rowToUpdate.setUpdated(true);
        rowToUpdate.setConditionalUpdated(true);
        rowToUpdate.setConditionalDetails(expectedConditionalDetails);

        expect(plannedEventDao.getById(existingEvent.getId())).andReturn(existingEvent);
        amendmentService.updateDevelopmentAmendment(existingEvent,
            PropertyChange.create("condition", null, expectedConditionalDetails));

        replayMocks();
        command.apply();
        verifyMocks();
    }

    public void testApplyAnotherWithDifferentDetails() throws Exception {
        PlannedActivity existingEvent = createPlannedEvent(4, 2, "Det A", 2, null);
        period.addPlannedEvent(existingEvent);
        initCommand();

        ManagePeriodEventsCommand.GridRow row =
                createGridRow(activities.get(4), "Det B", null, null, null, 3, null, null, null, null);
        row.setUpdated(true);
        row.setAddition(true);
        command.setOldRow(row);
        command.getGrid().add(row);

        PlannedActivity addedEvent = createPlannedEvent(4, 2, "Det B", null, null);
        amendmentService.updateDevelopmentAmendment(same(period), addFor(addedEvent));

        replayMocks();
        command.apply();
        verifyMocks();

        assertEquals("New PE added directly to model", 1, period.getPlannedEvents().size());
        assertEquals("Details incorrectly changed on old event", "Det A",
            period.getPlannedEvents().get(0).getDetails());
    }

    private static abstract class AbstractPlannedEventMatcher implements IArgumentMatcher {
        protected PlannedActivity expectedPlannedEvent;

        public AbstractPlannedEventMatcher(PlannedActivity expectedPlannedEvent) {
            this.expectedPlannedEvent = expectedPlannedEvent;
        }

        protected boolean plannedEventMatches(PlannedActivity actual) {
            return nullSafeEquals(expectedPlannedEvent.getActivity(), actual.getActivity())
                    && nullSafeEquals(expectedPlannedEvent.getDetails(), actual.getDetails())
                    && nullSafeEquals(expectedPlannedEvent.getCondition(), actual.getCondition());
        }
    }

    public static PlannedActivity eqPlannedEvent(PlannedActivity event) {
        EasyMock.reportMatcher(new PlannedEventMatcher(event));
        return null;
    }

    private static class PlannedEventMatcher extends AbstractPlannedEventMatcher {
        public PlannedEventMatcher(PlannedActivity expectedPlannedEvent) {
            super(expectedPlannedEvent);
        }

        public boolean matches(Object object) {
            if (!(object instanceof PlannedActivity)) return false;

            return plannedEventMatches((PlannedActivity) object);
        }

        public void appendTo(StringBuffer sb) {
            sb.append("PlannedActivity activity=").append(expectedPlannedEvent.getActivity());
        }
    }

    public static Add addFor(PlannedActivity event) {
        EasyMock.reportMatcher(new AddForPlannedEventMatcher(event));
        return null;
    }

    private static class AddForPlannedEventMatcher extends AbstractPlannedEventMatcher {
        public AddForPlannedEventMatcher(PlannedActivity expectedPlannedEvent) {
            super(expectedPlannedEvent);
        }

        public boolean matches(Object object) {
            if (!(object instanceof Add)) return false;
            // Double cast to work around a javac bug
            return plannedEventMatches((PlannedActivity) (PlanTreeNode) ((Add) object).getChild());
        }

        public void appendTo(StringBuffer sb) {
            sb.append("Add for PlannedActivity activity=").append(expectedPlannedEvent.getActivity());
        }
    }

    public static Add removeFor(PlannedActivity event) {
        EasyMock.reportMatcher(new RemoveForPlannedEventMatcher(event));
        return null;
    }

    private static class RemoveForPlannedEventMatcher extends AbstractPlannedEventMatcher {
        public RemoveForPlannedEventMatcher(PlannedActivity expectedPlannedEvent) {
            super(expectedPlannedEvent);
        }

        public boolean matches(Object object) {
            if (!(object instanceof Remove)) return false;
            // Double cast to work around a javac bug
            return plannedEventMatches((PlannedActivity) (PlanTreeNode) ((Remove) object).getChild());
        }

        public void appendTo(StringBuffer sb) {
            sb.append("Remove for PlannedActivity activity=").append(expectedPlannedEvent.getActivity());
        }
    }

    private void initCommand() {
        command = new ManagePeriodEventsCommand(period, plannedEventDao, amendmentService);
    }

    private PlannedActivity createPlannedEvent(int activityId, int day, Integer id) {
        return createPlannedEvent(activityId, day, null, id, null);
    }

    private PlannedActivity createPlannedEvent(
        int activityIndex, int day, String details, Integer id, String conditionalDetails
    ) {
        Activity activity = activities.get(activityIndex);
        PlannedActivity evt = Fixtures.createPlannedEvent(activity.getName(), day);
        evt.setId(id);
        evt.setActivity(activity);
        evt.setDetails(details);
        evt.setCondition(conditionalDetails);
        return evt;
    }

    private ManagePeriodEventsCommand.GridRow createGridRow(
        Activity activity, String details, String conditionalDetails, Integer... eventIds
    ) {
        ManagePeriodEventsCommand.GridRow row = new ManagePeriodEventsCommand.GridRow(activity, details, eventIds.length, conditionalDetails);
        for (int i = 0; i < eventIds.length; i++) {
            row.getEventIds().set(i, eventIds[i]);
        }
        return row;
    }
}
