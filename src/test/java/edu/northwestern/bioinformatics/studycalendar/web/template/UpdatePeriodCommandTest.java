package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedEventDao;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import static org.easymock.EasyMock.expect;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class UpdatePeriodCommandTest  extends EditCommandTestCase {
    private UpdatePeriodCommand command = new UpdatePeriodCommand();
    private Period period;
    private PeriodDao periodDao;
    private PlannedEventDao plannedEventDao;
    public int PERIOD_ID = 10;
    public AmendmentService amendmentService;
    public Activity activity;
    public String periodDetails;

    public String detailsToChange;
    public String eventDetails;
    public String eventConditionalDetails;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        periodDao = registerMockFor(PeriodDao.class);
        plannedEventDao = registerMockFor(PlannedEventDao.class);
        amendmentService = registerMockFor(AmendmentService.class);

        activity = createNamedInstance("Three", Activity.class);
        activity.setId(3);
        periodDetails = "DETAILS";

        detailsToChange = "New event details";
        eventDetails = "Event details";
        eventConditionalDetails = "Event conditional details";        

        period = new Period();
        period.setName("period");
        period.setStartDay(13);
        period.getDuration().setQuantity(7);
        period.getDuration().setUnit(Duration.Unit.day);
        period.setId(PERIOD_ID);

        command.setPeriodDao(periodDao);
        command.setPlannedEventDao(plannedEventDao);
        command.setAmendmentService(amendmentService);
    }

    public void testPerformEditOnDetails() throws Exception {
        command.setId(period.getId());
        PlannedActivity eventOne = createPlannedEvent(1, 25);
        eventOne.setId(21);
        eventOne.setDetails(eventDetails);
        period.addPlannedEvent(eventOne);

        List<Integer> ids = new ArrayList<Integer>();
        ids.add(eventOne.getId());
        command.setEventIds(ids);
        command.setDetails(detailsToChange);
        command.setColumnNumber(-1);
        
        expect(periodDao.getById(PERIOD_ID)).andReturn(period).anyTimes();
        expect(plannedEventDao.getById(21)).andReturn(eventOne);

        amendmentService.updateDevelopmentAmendment(eventOne,
            PropertyChange.create("details", eventDetails, detailsToChange));

        replayMocks();
        command.performEdit();
        verifyMocks();
        
        assertEquals(1, period.getPlannedEvents().size());
        assertEquals("Details should not be updated in place", eventDetails, command.getPeriod().getPlannedEvents().get(0).getDetails());
    }

    public void testPerformEditOnConditionalDetails() throws Exception {
        command.setId(period.getId());
        PlannedActivity eventOne = createPlannedEvent(1, 25);
        eventOne.setId(21);
        eventOne.setCondition(eventConditionalDetails);
        period.addPlannedEvent(eventOne);

        List<Integer> ids = new ArrayList<Integer>();
        ids.add(eventOne.getId());
        command.setEventIds(ids);
        command.setConditionalDetails(detailsToChange);
        command.setConditionalUpdated(true);
        command.setColumnNumber(0);

        expect(periodDao.getById(PERIOD_ID)).andReturn(period).anyTimes();
        expect(plannedEventDao.getById(21)).andReturn(eventOne);

        amendmentService.updateDevelopmentAmendment(eventOne,
            PropertyChange.create("condition", eventConditionalDetails, detailsToChange));

        replayMocks();
        command.performEdit();
        verifyMocks();

        assertEquals(1, period.getPlannedEvents().size());
        assertEquals("Details should not be updated in place", eventConditionalDetails, command.getPeriod().getPlannedEvents().get(0).getCondition());
    }

    public void testGetLocalModelWithDetails() throws Exception {
        PlannedActivity eventTwo = createPlannedEvent(2, 26);
        eventTwo.setId(22);
        command.setDetails(eventDetails);
        period.addPlannedEvent(eventTwo);
        expect(periodDao.getById(PERIOD_ID)).andReturn(period).anyTimes();
        command.setId(period.getId());
        command.setColumnNumber(-1);
        command.setRowNumber(3);

        replayMocks();

        Map<String, Object> map = command.getLocalModel();
        verifyMocks();

        assertNotNull("Map is null", map);
        assertEquals("Map's details is wrong", eventDetails, map.get("details"));
        assertEquals("Map's column number is wrong", -1, map.get("columnNumber"));
        assertEquals("Map's row number is wrong", 3, map.get("rowNumber"));
     }

    public void testGetLocalModelWithConditionalDetails() throws Exception {
        PlannedActivity eventTwo = createPlannedEvent(2, 26);
        eventTwo.setId(22);
        command.setConditionalDetails(eventConditionalDetails);
        command.setConditionalUpdated(true);
        period.addPlannedEvent(eventTwo);
        expect(periodDao.getById(PERIOD_ID)).andReturn(period).anyTimes();
        command.setId(period.getId());
        command.setColumnNumber(1);
        command.setRowNumber(3);

        replayMocks();

        Map<String, Object> map = command.getLocalModel();
        verifyMocks();
        System.out.println("map " + map);
        assertNotNull("Map is null", map);
        assertEquals("Map's details is wrong", eventConditionalDetails, map.get("conditionalDetails"));
        assertEquals("Map's column number is wrong", 1, map.get("columnNumber"));
        assertEquals("Map's row number is wrong", 3, map.get("rowNumber"));
     }

    private PlannedActivity createPlannedEvent(int day, Integer id) {
        return createPlannedEvent(day, null, id, null);
    }

    private PlannedActivity createPlannedEvent(
        int day, String details, Integer id, String conditionalDetails
    ) {
        PlannedActivity evt = Fixtures.createPlannedEvent(activity.getName(), day);
        evt.setId(id);
        evt.setActivity(activity);
        evt.setDetails(details);
        evt.setCondition(conditionalDetails);
        return evt;
    }

}

