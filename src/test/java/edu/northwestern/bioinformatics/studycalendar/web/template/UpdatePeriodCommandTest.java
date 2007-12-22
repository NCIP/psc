package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import static org.easymock.EasyMock.expect;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class UpdatePeriodCommandTest  extends EditCommandTestCase {
    private static final int PERIOD_ID = 10;

    private UpdatePeriodCommand command = new UpdatePeriodCommand();
    private Period period;
    private PlannedActivityDao plannedActivityDao;
    private AmendmentService amendmentService;
    private Activity activity;

    private String detailsToChange;
    private String eventDetails;
    private String eventConditionalDetails;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        plannedActivityDao = registerDaoMockFor(PlannedActivityDao.class);
        amendmentService = registerMockFor(AmendmentService.class);

        activity = createNamedInstance("Three", Activity.class);
        activity.setId(3);

        detailsToChange = "New event details";
        eventDetails = "Event details";
        eventConditionalDetails = "Event conditional details";        

        period = new Period();
        period.setName("period");
        period.setStartDay(13);
        period.getDuration().setQuantity(7);
        period.getDuration().setUnit(Duration.Unit.day);
        period.setId(PERIOD_ID);

        command.setPlannedActivityDao(plannedActivityDao);
        command.setAmendmentService(amendmentService);

        command.setPeriod(period);
    }

    public void testPerformEditOnDetails() throws Exception {
        PlannedActivity eventOne = createPlannedActivity(1, 25);
        eventOne.setId(21);
        eventOne.setDetails(eventDetails);
        period.addPlannedActivity(eventOne);

        List<Integer> ids = new ArrayList<Integer>();
        ids.add(eventOne.getId());
        command.setPlannedActivities(ids);
        command.setDetails(detailsToChange);
        command.setColumnNumber(-1);
        
        expect(plannedActivityDao.getById(21)).andReturn(eventOne).anyTimes();

        amendmentService.updateDevelopmentAmendment(eventOne,
            PropertyChange.create("details", eventDetails, detailsToChange));
        amendmentService.updateDevelopmentAmendment(eventOne,
            PropertyChange.create("condition", null, null));

        replayMocks();
        command.performEdit();
        verifyMocks();
        
        assertEquals(1, period.getPlannedActivities().size());
        assertEquals("Details should not be updated in place", eventDetails, command.getPeriod().getPlannedActivities().get(0).getDetails());
    }

    public void testPerformEditOnConditionalDetails() throws Exception {
        PlannedActivity eventOne = createPlannedActivity(1, 25);
        eventOne.setId(21);
        eventOne.setCondition(eventConditionalDetails);
        period.addPlannedActivity(eventOne);

        List<Integer> ids = new ArrayList<Integer>();
        ids.add(eventOne.getId());
        command.setPlannedActivities(ids);
        command.setConditionalDetails(detailsToChange);
        command.setColumnNumber(0);

        expect(plannedActivityDao.getById(21)).andReturn(eventOne).anyTimes();

        amendmentService.updateDevelopmentAmendment(eventOne,
            PropertyChange.create("details", null, null));
        amendmentService.updateDevelopmentAmendment(eventOne,
            PropertyChange.create("condition", eventConditionalDetails, detailsToChange));

        replayMocks();
        command.performEdit();
        verifyMocks();

        assertEquals(1, period.getPlannedActivities().size());
        assertEquals("Details should not be updated in place", eventConditionalDetails, command.getPeriod().getPlannedActivities().get(0).getCondition());
    }

    public void testGetLocalModelWithDetails() throws Exception {
        PlannedActivity eventTwo = createPlannedActivity(2, 26);
        eventTwo.setId(22);
        command.setDetails(eventDetails);
        period.addPlannedActivity(eventTwo);
        command.setColumnNumber(-1);
        command.setRowNumber(3);

        replayMocks();

        Map<String, Object> map = command.getLocalModel();
        verifyMocks();

        assertNotNull("Map is null", map);
        assertEquals("Map's column number is wrong", -1, map.get("columnNumber"));
        assertEquals("Map's row number is wrong", 3, map.get("rowNumber"));
     }

    public void testGetLocalModelWithConditionalDetails() throws Exception {
        PlannedActivity eventTwo = createPlannedActivity(2, 26);
        eventTwo.setId(22);
        command.setConditionalDetails(eventConditionalDetails);
        period.addPlannedActivity(eventTwo);
        command.setColumnNumber(1);
        command.setRowNumber(3);

        replayMocks();

        Map<String, Object> map = command.getLocalModel();
        verifyMocks();
        System.out.println("map " + map);
        assertNotNull("Map is null", map);
        assertEquals("Map's column number is wrong", 1, map.get("columnNumber"));
        assertEquals("Map's row number is wrong", 3, map.get("rowNumber"));
     }

    private PlannedActivity createPlannedActivity(int day, Integer id) {
        return createPlannedActivity(day, null, id, null);
    }

    private PlannedActivity createPlannedActivity(
        int day, String details, Integer id, String conditionalDetails
    ) {
        PlannedActivity evt = Fixtures.createPlannedActivity(activity.getName(), day);
        evt.setId(id);
        evt.setActivity(activity);
        evt.setDetails(details);
        evt.setCondition(conditionalDetails);
        return evt;
    }

}

