package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
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
    private PlannedActivityDao plannedActivityDao;
    private static final int PERIOD_ID = 10;
    private AmendmentService amendmentService;
    private StudyService studyService;
    private Activity activity;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        plannedActivityDao = registerMockFor(PlannedActivityDao.class);
        amendmentService = registerMockFor(AmendmentService.class);
        studyService = registerMockFor(StudyService.class);

        activity = createNamedInstance("Three", Activity.class);
        activity.setId(3);

        period = new Period();
        period.setName("period");
        period.setStartDay(13);
        period.getDuration().setQuantity(7);
        period.getDuration().setUnit(Duration.Unit.day);
        period.setId(PERIOD_ID);

        command.setPlannedActivityDao(plannedActivityDao);
        command.setAmendmentService(amendmentService);
        command.setStudyService(studyService);
    }

    public void testPerformEdit() throws Exception {
        command.setPeriod(period);
        PlannedActivity eventOne = createPlannedActivity(1, 25);
        eventOne.setId(21);
        period.addPlannedActivity(eventOne);

        List<Integer> ids = new ArrayList<Integer>();
        ids.add(eventOne.getId());
        command.setPlannedActivities(ids);
        command.setColumnNumber(0);
        expect(plannedActivityDao.getById(21)).andReturn(eventOne);

        amendmentService.updateDevelopmentAmendment(same(period), removeFor(eventOne));
        replayMocks();
        command.performEdit();
        verifyMocks();

        assertEquals("Event should not be directly removed", 1, period.getPlannedActivities().size());
    }

    public void testGetLocalModel() throws Exception {
        PlannedActivity eventTwo = createPlannedActivity(2, 26);
        eventTwo.setId(22);
        period.addPlannedActivity(eventTwo);
        command.setPeriod(period);
        command.setColumnNumber(2);

        replayMocks();

        Map<String, Object> map = command.getLocalModel();
        verifyMocks();

        assertNotNull("Map is null", map);
        assertEquals("Map's row number is wrong", 0, map.get("rowNumber"));
        assertEquals("Map's column number is wrong", 2, map.get("columnNumber"));
        assertEquals("Map's id is wrong", -1, map.get("id"));
     }



    private static abstract class AbstractPlannedActivityMatcher implements IArgumentMatcher {
        protected PlannedActivity expectedPlannedActivity;

        public AbstractPlannedActivityMatcher(PlannedActivity expectedPlannedActivity) {
            this.expectedPlannedActivity = expectedPlannedActivity;
        }

        protected boolean plannedActivityMatches(PlannedActivity actual) {
            return nullSafeEquals(expectedPlannedActivity.getActivity(), actual.getActivity())
                    && nullSafeEquals(expectedPlannedActivity.getDetails(), actual.getDetails())
                    && nullSafeEquals(expectedPlannedActivity.getCondition(), actual.getCondition());
        }
    }

    public static Add removeFor(PlannedActivity event) {
        EasyMock.reportMatcher(new RemoveForPlannedActivityMatcher(event));
        return null;
    }

    private static class RemoveForPlannedActivityMatcher extends AbstractPlannedActivityMatcher {
        public RemoveForPlannedActivityMatcher(PlannedActivity expectedPlannedActivity) {
            super(expectedPlannedActivity);
        }

        public boolean matches(Object object) {
            if (!(object instanceof Remove)) return false;
            // Double cast to work around a javac bug
            return plannedActivityMatches((PlannedActivity) ((Remove) object).getChild());
        }

        public void appendTo(StringBuffer sb) {
            sb.append("Remove for PlannedActivity activity=").append(expectedPlannedActivity.getActivity());
        }
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
