package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.Duration;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import static org.easymock.EasyMock.expect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author nshurupova
 */
public class MovePlannedActivityCommandTest extends EditCommandTestCase {
    private MovePlannedActivityCommand command = new MovePlannedActivityCommand();
    private Period period;
    private PeriodDao periodDao;
    private PlannedActivityDao plannedActivityDao;
    private static int PERIOD_ID = 10;
    private AmendmentService amendmentService;
    private Activity activity;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        periodDao = registerMockFor(PeriodDao.class);
        plannedActivityDao = registerMockFor(PlannedActivityDao.class);
        amendmentService = registerMockFor(AmendmentService.class);

        activity = createNamedInstance("Three", Activity.class);
        activity.setId(3);

        period = new Period();
        period.setName("period");
        period.setStartDay(13);
        period.getDuration().setQuantity(7);
        period.getDuration().setUnit(Duration.Unit.day);
        period.setId(PERIOD_ID);

        command.setPeriodDao(periodDao);
        command.setPlannedActivityDao(plannedActivityDao);
        command.setAmendmentService(amendmentService);
    }

    public void testPerformEdit() throws Exception {
        command.setId(period.getId());
        PlannedActivity eventOne = createPlannedActivity(1, 25);
        eventOne.setId(21);
        period.addPlannedActivity(eventOne);

        List<Integer> ids = new ArrayList<Integer>();
        ids.add(eventOne.getId());
        command.setEventIds(ids);
        command.setMoveFrom(1);
        command.setMoveTo(4);

        expect(periodDao.getById(PERIOD_ID)).andReturn(period).anyTimes();
        expect(plannedActivityDao.getById(21)).andReturn(eventOne);

        amendmentService.updateDevelopmentAmendment(eventOne,
            PropertyChange.create("day", command.getMoveFrom() + 1, command.getMoveTo() + 1));

        replayMocks();
        command.performEdit();
        verifyMocks();

        assertEquals(1, period.getPlannedActivities().size());
        assertEquals("Details should not be updated in place", 1, (int) command.getPeriod().getPlannedActivities().get(0).getDay());
    }

    public void testGetLocalModel() throws Exception {
        PlannedActivity eventTwo = createPlannedActivity(2, 26);
        eventTwo.setId(22);

        command.setMovedPlannedActivity(eventTwo);
        period.addPlannedActivity(eventTwo);
        expect(periodDao.getById(PERIOD_ID)).andReturn(period).anyTimes();
        command.setId(period.getId());
        command.setMoveFrom(2);
        command.setMoveTo(5);

        replayMocks();

        Map<String, Object> map = command.getLocalModel();
        verifyMocks();
        assertNotNull("Map is null", map);
        assertEquals("Map's moved event is wrong", eventTwo, map.get("movedEvent"));
        assertEquals("Map's moveFrom is wrong", 2, map.get("moveFrom"));
        assertEquals("Map's moveTo is wrong", 5, map.get("moveTo"));        
        assertEquals("Map's column number is wrong", 0, map.get("columnNumber"));
        assertEquals("Map's row number is wrong", 0, map.get("rowNumber"));
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


