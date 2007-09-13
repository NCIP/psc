package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.Duration;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import static org.easymock.classextension.EasyMock.*;

/**
 * @author Rhett Sutphin
 */
public class EditPeriodCommandTest extends StudyCalendarTestCase {
    private static final Duration.Unit DURATION_UNIT = Duration.Unit.day;
    private static final Integer DURATION_QUANTITY = 71;
    private static final Integer START_DAY = 9;
    private static final String NAME = "Unethical";

    private EditPeriodCommand command;
    private Period period;
    private AmendmentService amendmentService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        period = new Period();
        period.setName(NAME);
        period.setStartDay(START_DAY);
        period.getDuration().setQuantity(DURATION_QUANTITY);
        period.getDuration().setUnit(DURATION_UNIT);

        amendmentService = registerMockFor(AmendmentService.class);
        command = new EditPeriodCommand(period, amendmentService);
    }

    public void testOriginalPeriodClonedIntoCommand() throws Exception {
        assertEquals(NAME, command.getPeriod().getName());
        assertEquals(START_DAY, command.getPeriod().getStartDay());
        assertEquals(DURATION_QUANTITY, command.getPeriod().getDuration().getQuantity());
        assertEquals(DURATION_UNIT, command.getPeriod().getDuration().getUnit());

        command.getPeriod().setName("Alternate");
        assertEquals("Original period changed on set", NAME, period.getName());
        assertEquals("Command period not changed on set", "Alternate", command.getPeriod().getName());
    }

    public void testApplyNameChange() throws Exception {
        command.getPeriod().setName("Ethical");

        expectPropertyUpdate("name", NAME, "Ethical");

        replayMocks();
        command.apply();
        verifyMocks();
    }

    public void testApplyStartDayChange() throws Exception {
        command.getPeriod().setStartDay(42);

        expectPropertyUpdate("startDay", START_DAY.toString(), Integer.toString(42));

        replayMocks();
        command.apply();
        verifyMocks();
    }

    public void testApplyDurationChange() throws Exception {
        command.getPeriod().getDuration().setUnit(Duration.Unit.week);
        command.getPeriod().getDuration().setQuantity(10);

        expectPropertyUpdate("duration.unit", "day", "week");
        expectPropertyUpdate("duration.quantity", "71", "10");

        replayMocks();
        command.apply();
        verifyMocks();
    }

    public void testApplyRepetitionsChange() throws Exception {
        command.getPeriod().setRepetitions(1000);

        expectPropertyUpdate("repetitions", "1", "1000");

        replayMocks();
        command.apply();
        verifyMocks();
    }

    public void testPurgeOldPlannedEvents() throws Exception {
        PlannedEvent chem = Fixtures.createPlannedEvent("Chem-7", 3);
        PlannedEvent cbc = Fixtures.createPlannedEvent("CBC", 70);
        period.addPlannedEvent(chem);
        period.addPlannedEvent(cbc);
        command.getPeriod().getDuration().setQuantity(60);

        expectPropertyUpdate("duration.quantity", "71", "60");
        amendmentService.updateDevelopmentAmendment(period, Remove.create(cbc));

        replayMocks();
        command.apply();
        verifyMocks();
    }

    private void expectPropertyUpdate(String property, String oldV, String newV) {
        amendmentService.updateDevelopmentAmendment(
            same(period), eq(PropertyChange.create(property, oldV, newV)));
    }
}
