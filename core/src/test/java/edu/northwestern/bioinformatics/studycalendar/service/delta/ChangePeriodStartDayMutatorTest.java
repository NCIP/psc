/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.service.ScheduleService;

import static org.easymock.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
public class ChangePeriodStartDayMutatorTest extends PeriodMutatorTestCase<PropertyChange> {
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
        return PropertyChange.create("startDay", "1", "5");
    }

    @Override
    protected Mutator createMutator() {
        return new ChangePeriodStartDayMutator(change, templateService, scheduleService);
    }

    public void testDatesShiftedForward() throws Exception {
        expect(templateService.findParent(period0)).andReturn(studySegment);
        expect(templateService.findParent(p0e0)).andReturn(period0).times(1, PERIOD_0_REPS);
        expect(templateService.findParent(p0e1)).andReturn(period0).times(1, PERIOD_0_REPS);
        expect(templateService.findParent(p1e0)).andReturn(period1).times(1, PERIOD_1_REPS);
        expect(templateService.findParent(p1e1)).andReturn(period1).times(1, PERIOD_1_REPS);

        scheduleService.reviseDate(getScheduledActivityFixture(p0e0, 0), 4, amendment);
        scheduleService.reviseDate(getScheduledActivityFixture(p0e0, 1), 4, amendment);
        scheduleService.reviseDate(getScheduledActivityFixture(p0e0, 2), 4, amendment);
        scheduleService.reviseDate(getScheduledActivityFixture(p0e1, 0), 4, amendment);
        scheduleService.reviseDate(getScheduledActivityFixture(p0e1, 1), 4, amendment);
        scheduleService.reviseDate(getScheduledActivityFixture(p0e1, 2), 4, amendment);

        replayMocks();
        getMutator().apply(scheduledCalendar);
        verifyMocks();
    }

    public void testDatesShiftedBackwards() throws Exception {
        change.setNewValue("0");

        expect(templateService.findParent(period0)).andReturn(studySegment);
        expect(templateService.findParent(p0e0)).andReturn(period0).times(1, PERIOD_0_REPS);
        expect(templateService.findParent(p0e1)).andReturn(period0).times(1, PERIOD_0_REPS);
        expect(templateService.findParent(p1e0)).andReturn(period1).times(1, PERIOD_1_REPS);
        expect(templateService.findParent(p1e1)).andReturn(period1).times(1, PERIOD_1_REPS);

        scheduleService.reviseDate(getScheduledActivityFixture(p0e0, 0), -1, amendment);
        scheduleService.reviseDate(getScheduledActivityFixture(p0e0, 1), -1, amendment);
        scheduleService.reviseDate(getScheduledActivityFixture(p0e0, 2), -1, amendment);
        scheduleService.reviseDate(getScheduledActivityFixture(p0e1, 0), -1, amendment);
        scheduleService.reviseDate(getScheduledActivityFixture(p0e1, 1), -1, amendment);
        scheduleService.reviseDate(getScheduledActivityFixture(p0e1, 2), -1, amendment);

        replayMocks();
        getMutator().apply(scheduledCalendar);
        verifyMocks();
    }

    public void testDoesNothingWhenNewAndOldValueIsSame() throws Exception {
        change.setNewValue("1");
        change.setOldValue("1");

        // expect nothing to happen
        replayMocks();
        getMutator().apply(scheduledCalendar);
        verifyMocks();
    }
}
