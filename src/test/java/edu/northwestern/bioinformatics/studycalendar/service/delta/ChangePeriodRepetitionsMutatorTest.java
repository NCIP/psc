package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.service.ParticipantService;
import static org.easymock.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
public class ChangePeriodRepetitionsMutatorTest extends PeriodMutatorTestCase<PropertyChange> {
    private TemplateService templateService;
    private ParticipantService participantService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        templateService = registerMockFor(TemplateService.class);
        participantService = registerMockFor(ParticipantService.class);
    }

    @Override
    protected PropertyChange createChange() {
        return PropertyChange.create("repetitions", "3", "5");
    }

    @Override
    protected Mutator createMutator() {
        return new ChangePeriodRepetitionsMutator(change, templateService, participantService);
    }

    public void testIncreaseReps() throws Exception {
        expect(templateService.findParent(period0)).andReturn(arm);
        participantService.schedulePeriod(period0, amendment, scheduledArm, 3);
        participantService.schedulePeriod(period0, amendment, scheduledArm, 4);

        replayMocks();
        getMutator().apply(scheduledCalendar);
        verifyMocks();
    }

    public void testDecreaseReps() throws Exception {
        change.setNewValue("1");

        expect(templateService.findParent(period0)).andReturn(arm);
        expect(templateService.findParent(p0e0)).andReturn(period0).times(PERIOD_0_REPS - 1);
        expect(templateService.findParent(p0e1)).andReturn(period0).times(PERIOD_0_REPS - 1);
        expect(templateService.findParent(p1e0)).andReturn(period1).times(PERIOD_1_REPS - 1);
        expect(templateService.findParent(p1e1)).andReturn(period1).times(PERIOD_1_REPS - 1);

        getScheduledEventFixture(p0e0, 2).unscheduleIfOutstanding("Repetition 3 removed in revision " + REVISION_DISPLAY_NAME);
        getScheduledEventFixture(p0e0, 1).unscheduleIfOutstanding("Repetition 2 removed in revision " + REVISION_DISPLAY_NAME);
        getScheduledEventFixture(p0e1, 2).unscheduleIfOutstanding("Repetition 3 removed in revision " + REVISION_DISPLAY_NAME);
        getScheduledEventFixture(p0e1, 1).unscheduleIfOutstanding("Repetition 2 removed in revision " + REVISION_DISPLAY_NAME);

        replayMocks();
        getMutator().apply(scheduledCalendar);
        verifyMocks();
    }
}
