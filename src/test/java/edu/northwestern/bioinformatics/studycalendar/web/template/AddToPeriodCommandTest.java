package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;

import java.util.Map;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.same;
import org.easymock.classextension.EasyMock;
import org.easymock.IArgumentMatcher;
import static gov.nih.nci.cabig.ctms.lang.ComparisonTools.nullSafeEquals;


public class AddToPeriodCommandTest extends EditCommandTestCase {
    private AddToPeriodCommand command = new AddToPeriodCommand();
    private Period period;
    private PeriodDao periodDao;
    public int PERIOD_ID = 88;
    public AmendmentService amendmentService;
    public Activity activity;
    public String details;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        periodDao = registerMockFor(PeriodDao.class);
        amendmentService = registerMockFor(AmendmentService.class);
        period = new Period();
        activity = createNamedInstance("Three", Activity.class);
        activity.setId(3);
        details = "DETAILS";
        period.setName("period");
        period.setStartDay(13);
        period.getDuration().setQuantity(7);
        period.getDuration().setUnit(Duration.Unit.day);
        period.setId(PERIOD_ID);
        command.setPeriodDao(periodDao);
        command.setAmendmentService(amendmentService);
    }

    public void testAddToPeriodPerformEdit() throws Exception {
        command.setId(period.getId());
        command.setColumnNumber(2);

        command.setActivity(activity);
        command.setDetails(details);
        command.setConditionalDetails(null);
        expect(periodDao.getById(PERIOD_ID)).andReturn(period).anyTimes();

        PlannedActivity event = new PlannedActivity();
        event.setDay(command.getColumnNumber()+1);
        event.setActivity(command.getActivity());
        event.setDetails(command.getDetails());
        event.setCondition(command.getConditionalDetails());
        period.addPlannedActivity(event);
        amendmentService.updateDevelopmentAmendment(same(period), addFor(event));

        replayMocks();
        PlannedActivity expectedEvent = command.performEdit();
        verifyMocks();

        assertEquals(1, command.getPeriod().getPlannedActivities().size());
        assertEquals("Event's activity is wrong", expectedEvent.getActivity(), activity);
        assertEquals("Event's details are wrong", expectedEvent.getDetails(), details );
        assertEquals("Event's day is wrong", (int) expectedEvent.getDay(), 3 );

    }
 
    public void testGetLocalModel() throws Exception {
        command.setId(period.getId());
        command.setColumnNumber(2);

        command.setActivity(activity);
        command.setDetails(details);
        command.setConditionalDetails(null);
        expect(periodDao.getById(PERIOD_ID)).andReturn(period).anyTimes();

        PlannedActivity event = new PlannedActivity();
        event.setDay(command.getColumnNumber()+1);
        event.setActivity(command.getActivity());
        event.setDetails(command.getDetails());
        event.setCondition(command.getConditionalDetails());
        period.addPlannedActivity(event);
        amendmentService.updateDevelopmentAmendment(same(period), addFor(event));

        replayMocks();
        PlannedActivity expectedEvent = command.performEdit();
        Map<String, Object> map = command.getLocalModel();
        verifyMocks();


        assertNotNull("Map is null", map);
        assertEquals("Map's row number is wrong", 0, map.get("rowNumber"));
        assertEquals("Map's column number is wrong", 2, map.get("columnNumber"));
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

    public static Add addFor(PlannedActivity event) {
        EasyMock.reportMatcher(new AddForPlannedActivityMatcher(event));
        return null;
    }

    private static class AddForPlannedActivityMatcher extends AbstractPlannedActivityMatcher {
        public AddForPlannedActivityMatcher(PlannedActivity expectedPlannedActivity) {
            super(expectedPlannedActivity);
        }

        public boolean matches(Object object) {
            if (!(object instanceof Add)) return false;
            // Double cast to work around a javac bug
            return plannedActivityMatches((PlannedActivity) (PlanTreeNode) ((Add) object).getChild());
        }

        public void appendTo(StringBuffer sb) {
            sb.append("Add for PlannedActivity activity=").append(expectedPlannedActivity.getActivity());
        }
    }
}
