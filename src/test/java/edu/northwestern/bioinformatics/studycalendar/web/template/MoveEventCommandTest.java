package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedEventDao;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import static org.easymock.EasyMock.expect;

/**
 * Created by IntelliJ IDEA.
 * User: nshurupova
 * Date: Sep 25, 2007
 * Time: 4:15:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class MoveEventCommandTest extends EditCommandTestCase {
    private MoveEventCommand command = new MoveEventCommand();
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

    public void testPerformEdit() throws Exception {
        command.setId(period.getId());
        PlannedEvent eventOne = createPlannedEvent(1, 25);
        eventOne.setId(21);
        eventOne.setDetails(eventDetails);
        period.addPlannedEvent(eventOne);

        List<Integer> ids = new ArrayList<Integer>();
        ids.add(eventOne.getId());
        command.setEventIds(ids);
        command.setMoveFrom(1);
        command.setMoveTo(4);

        expect(periodDao.getById(PERIOD_ID)).andReturn(period).anyTimes();
        expect(plannedEventDao.getById(21)).andReturn(eventOne);

        amendmentService.updateDevelopmentAmendment(eventOne,
            PropertyChange.create("day", command.getMoveFrom() + 1, command.getMoveTo() + 1));

        replayMocks();
        command.performEdit();
        verifyMocks();

        assertEquals(1, period.getPlannedEvents().size());
        assertEquals("Details should not be updated in place", 1, (int) command.getPeriod().getPlannedEvents().get(0).getDay());        
    }

    public void testGetLocalModel() throws Exception {
        PlannedEvent eventTwo = createPlannedEvent(2, 26);
        eventTwo.setId(22);

        command.setNewEvent(eventTwo);
        command.setDetails(eventDetails);
        period.addPlannedEvent(eventTwo);
        expect(periodDao.getById(PERIOD_ID)).andReturn(period).anyTimes();
        command.setId(period.getId());
        command.setMoveFrom(2);
        command.setMoveTo(5);

        replayMocks();

        Map<String, Object> map = command.getLocalModel();
        verifyMocks();
        assertNotNull("Map is null", map);
        assertEquals("Map's id is wrong", 22, map.get("id"));
        assertEquals("Map's moveFrom is wrong", 2, map.get("moveFrom"));
        assertEquals("Map's moveTo is wrong", 5, map.get("moveTo"));        
        assertEquals("Map's column number is wrong", 0, map.get("columnNumber"));
        assertEquals("Map's row number is wrong", 0, map.get("rowNumber"));
     }

    private PlannedEvent createPlannedEvent(int day, Integer id) {
        return createPlannedEvent(day, null, id, null);
    }

    private PlannedEvent createPlannedEvent(
        int day, String details, Integer id, String conditionalDetails
    ) {
        PlannedEvent evt = Fixtures.createPlannedEvent(activity.getName(), day);
        evt.setId(id);
        evt.setActivity(activity);
        evt.setDetails(details);
        evt.setCondition(conditionalDetails);
        return evt;
    }

}


