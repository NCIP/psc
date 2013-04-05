/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.domain.Duration;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.service.ScheduleService;
import static org.easymock.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
public class ChangePeriodDurationQuantityMutatorTest extends PeriodMutatorTestCase<PropertyChange> {
    private TemplateService templateService;
    private ScheduleService scheduleService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        templateService = registerMockFor(TemplateService.class);
        scheduleService = registerMockFor(ScheduleService.class);
    }

    @Override
    protected PropertyChange createChange() {
        return PropertyChange.create("duration.quantity", "9", "14");
    }

    @Override
    protected Mutator createMutator() {
        return new ChangePeriodDurationQuantityMutator(change, templateService, scheduleService);
    }

    public void testIncreaseDays() throws Exception {
        expect(templateService.findParent(period0)).andReturn(studySegment);
        expect(templateService.findParent(p0e0)).andReturn(period0).times(1, PERIOD_0_REPS);
        expect(templateService.findParent(p0e1)).andReturn(period0).times(1, PERIOD_0_REPS);
        expect(templateService.findParent(p1e0)).andReturn(period1).times(1, PERIOD_1_REPS);
        expect(templateService.findParent(p1e1)).andReturn(period1).times(1, PERIOD_1_REPS);

        scheduleService.reviseDate(getScheduledActivityFixture(p0e0, 0),  0, amendment);
        scheduleService.reviseDate(getScheduledActivityFixture(p0e1, 0),  0, amendment);
        scheduleService.reviseDate(getScheduledActivityFixture(p0e0, 1),  5, amendment);
        scheduleService.reviseDate(getScheduledActivityFixture(p0e1, 1),  5, amendment);
        scheduleService.reviseDate(getScheduledActivityFixture(p0e0, 2), 10, amendment);
        scheduleService.reviseDate(getScheduledActivityFixture(p0e1, 2), 10, amendment);

        replayMocks();
        getMutator().apply(scheduledCalendar);
        verifyMocks();
    }

    public void testDecreaseDays() throws Exception {
        change.setNewValue("8");

        expect(templateService.findParent(period0)).andReturn(studySegment);
        expect(templateService.findParent(p0e0)).andReturn(period0).times(1, PERIOD_0_REPS);
        expect(templateService.findParent(p0e1)).andReturn(period0).times(1, PERIOD_0_REPS);
        expect(templateService.findParent(p1e0)).andReturn(period1).times(1, PERIOD_1_REPS);
        expect(templateService.findParent(p1e1)).andReturn(period1).times(1, PERIOD_1_REPS);

        scheduleService.reviseDate(getScheduledActivityFixture(p0e0, 0),  0, amendment);
        scheduleService.reviseDate(getScheduledActivityFixture(p0e1, 0),  0, amendment);
        scheduleService.reviseDate(getScheduledActivityFixture(p0e0, 1), -1, amendment);
        scheduleService.reviseDate(getScheduledActivityFixture(p0e1, 1), -1, amendment);
        scheduleService.reviseDate(getScheduledActivityFixture(p0e0, 2), -2, amendment);
        scheduleService.reviseDate(getScheduledActivityFixture(p0e1, 2), -2, amendment);

        replayMocks();
        getMutator().apply(scheduledCalendar);
        verifyMocks();
    }
    
    public void testHandleWeeks() throws Exception {
        period0.getDuration().setUnit(Duration.Unit.week);
        period0.getDuration().setQuantity(3);

        change.setOldValue("3"); change.setNewValue("7");
        // increased by 28 days

        expect(templateService.findParent(period0)).andReturn(studySegment);
        expect(templateService.findParent(p0e0)).andReturn(period0).times(1, PERIOD_0_REPS);
        expect(templateService.findParent(p0e1)).andReturn(period0).times(1, PERIOD_0_REPS);
        expect(templateService.findParent(p1e0)).andReturn(period1).times(1, PERIOD_1_REPS);
        expect(templateService.findParent(p1e1)).andReturn(period1).times(1, PERIOD_1_REPS);

        scheduleService.reviseDate(getScheduledActivityFixture(p0e0, 0),  0, amendment);
        scheduleService.reviseDate(getScheduledActivityFixture(p0e1, 0),  0, amendment);
        scheduleService.reviseDate(getScheduledActivityFixture(p0e0, 1), 28, amendment);
        scheduleService.reviseDate(getScheduledActivityFixture(p0e1, 1), 28, amendment);
        scheduleService.reviseDate(getScheduledActivityFixture(p0e0, 2), 56, amendment);
        scheduleService.reviseDate(getScheduledActivityFixture(p0e1, 2), 56, amendment);

        replayMocks();
        getMutator().apply(scheduledCalendar);
        verifyMocks();
    }
}
