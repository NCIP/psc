/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.service.ScheduleService;
import org.easymock.EasyMock;

/**
 * @author Rhett Sutphin
 */
public class ChangePeriodDurationUnitMutatorTest extends PeriodMutatorTestCase<PropertyChange> {
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
        return PropertyChange.create("duration.unit", "day", "week");
    }

    @Override
    protected Mutator createMutator() {
        return new ChangePeriodDurationUnitMutator(change, templateService, scheduleService);
    }

    public void testChangeFromDaysToWeeks() throws Exception {
        EasyMock.expect(templateService.findParent(period0)).andReturn(studySegment);
        EasyMock.expect(templateService.findParent(p0e0)).andReturn(period0).times(1, PERIOD_0_REPS);
        EasyMock.expect(templateService.findParent(p0e1)).andReturn(period0).times(1, PERIOD_0_REPS);
        EasyMock.expect(templateService.findParent(p1e0)).andReturn(period1).times(1, PERIOD_1_REPS);
        EasyMock.expect(templateService.findParent(p1e1)).andReturn(period1).times(1, PERIOD_1_REPS);

        scheduleService.reviseDate(getScheduledActivityFixture(p0e0, 0),   0, amendment);
        scheduleService.reviseDate(getScheduledActivityFixture(p0e1, 0),   0, amendment);
        scheduleService.reviseDate(getScheduledActivityFixture(p0e0, 1),  54, amendment);
        scheduleService.reviseDate(getScheduledActivityFixture(p0e1, 1),  54, amendment);
        scheduleService.reviseDate(getScheduledActivityFixture(p0e0, 2), 108, amendment);
        scheduleService.reviseDate(getScheduledActivityFixture(p0e1, 2), 108, amendment);

        replayMocks();
        getMutator().apply(scheduledCalendar);
        verifyMocks();
    }

    public void testDecreaseDays() throws Exception {
        period0.getDuration().setQuantity(3);
        change.setOldValue("week");
        change.setNewValue("day");

        EasyMock.expect(templateService.findParent(period0)).andReturn(studySegment);
        EasyMock.expect(templateService.findParent(p0e0)).andReturn(period0).times(1, PERIOD_0_REPS);
        EasyMock.expect(templateService.findParent(p0e1)).andReturn(period0).times(1, PERIOD_0_REPS);
        EasyMock.expect(templateService.findParent(p1e0)).andReturn(period1).times(1, PERIOD_1_REPS);
        EasyMock.expect(templateService.findParent(p1e1)).andReturn(period1).times(1, PERIOD_1_REPS);

        scheduleService.reviseDate(getScheduledActivityFixture(p0e0, 0),   0, amendment);
        scheduleService.reviseDate(getScheduledActivityFixture(p0e1, 0),   0, amendment);
        scheduleService.reviseDate(getScheduledActivityFixture(p0e0, 1), -18, amendment);
        scheduleService.reviseDate(getScheduledActivityFixture(p0e1, 1), -18, amendment);
        scheduleService.reviseDate(getScheduledActivityFixture(p0e0, 2), -36, amendment);
        scheduleService.reviseDate(getScheduledActivityFixture(p0e1, 2), -36, amendment);

        replayMocks();
        getMutator().apply(scheduledCalendar);
        verifyMocks();
    }
}
