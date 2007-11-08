package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedEventDao;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.same;
import org.easymock.IArgumentMatcher;
import org.easymock.classextension.EasyMock;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import static gov.nih.nci.cabig.ctms.lang.ComparisonTools.nullSafeEquals;


public class DeleteFromPeriodCommandTest extends EditCommandTestCase {
    private DeleteFromPeriodCommand command = new DeleteFromPeriodCommand();
    private Period period;
    private PeriodDao periodDao;
    private PlannedEventDao plannedEventDao;
    public int PERIOD_ID = 10;
    public AmendmentService amendmentService;
    public StudyService studyService;
    public Activity activity;
    public String details;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        periodDao = registerMockFor(PeriodDao.class);
        plannedEventDao = registerMockFor(PlannedEventDao.class);
        amendmentService = registerMockFor(AmendmentService.class);
        studyService = registerMockFor(StudyService.class);

        activity = createNamedInstance("Three", Activity.class);
        activity.setId(3);
        details = "DETAILS";

        period = new Period();
        period.setName("period");
        period.setStartDay(13);
        period.getDuration().setQuantity(7);
        period.getDuration().setUnit(Duration.Unit.day);
        period.setId(PERIOD_ID);

        command.setPeriodDao(periodDao);
        command.setPlannedEventDao(plannedEventDao);
        command.setAmendmentService(amendmentService);
        command.setStudyService(studyService);
    }

    public void testPerformEdit() throws Exception {
        command.setId(period.getId());
        PlannedActivity eventOne = createPlannedEvent(1, 25);
        eventOne.setId(21);
        period.addPlannedEvent(eventOne);
        expect(periodDao.getById(PERIOD_ID)).andReturn(period).anyTimes();

        List<Integer> ids = new ArrayList<Integer>();
        ids.add(eventOne.getId());
        command.setEventIds(ids);
        command.setColumnNumber(0);
        expect(plannedEventDao.getById(21)).andReturn(eventOne);

        amendmentService.updateDevelopmentAmendment(same(period), removeFor(eventOne));
        replayMocks();
        command.performEdit();
        verifyMocks();

        assertEquals("Event should not be directly removed", 1, period.getPlannedEvents().size());
    }

    public void testGetLocalModel() throws Exception {
        PlannedActivity eventTwo = createPlannedEvent(2, 26);
        eventTwo.setId(22);
        period.addPlannedEvent(eventTwo);
        expect(periodDao.getById(PERIOD_ID)).andReturn(period).anyTimes();
        command.setId(period.getId());
        command.setColumnNumber(2);

        replayMocks();

        Map<String, Object> map = command.getLocalModel();
        verifyMocks();

        assertNotNull("Map is null", map);
        assertEquals("Map's row number is wrong", 0, map.get("rowNumber"));
        assertEquals("Map's column number is wrong", 2, map.get("columnNumber"));
        assertEquals("Map's id is wrong", -1, map.get("id"));
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
