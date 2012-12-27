/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import static org.easymock.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
public class ChangePeriodRepetitionsMutatorTest extends PeriodMutatorTestCase<PropertyChange> {
    private TemplateService templateService;
    private SubjectService subjectService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        templateService = registerMockFor(TemplateService.class);
        subjectService = registerMockFor(SubjectService.class);
    }

    @Override
    protected PropertyChange createChange() {
        return PropertyChange.create("repetitions", "3", "5");
    }

    @Override
    protected Mutator createMutator() {
        return new ChangePeriodRepetitionsMutator(change, templateService, subjectService);
    }

    public void testIncreaseReps() throws Exception {
        expect(templateService.findParent(period0)).andReturn(studySegment);
        subjectService.schedulePeriod(period0, amendment, "Repetition 4 added in revision 02/04/1909 (Oops)", scheduledStudySegment, 3);
        subjectService.schedulePeriod(period0, amendment, "Repetition 5 added in revision 02/04/1909 (Oops)", scheduledStudySegment, 4);

        replayMocks();
        getMutator().apply(scheduledCalendar);
        verifyMocks();
    }

    public void testDecreaseReps() throws Exception {
        change.setNewValue("1");

        expect(templateService.findParent(period0)).andReturn(studySegment);
        expect(templateService.findParent(p0e0)).andReturn(period0).times(PERIOD_0_REPS - 1);
        expect(templateService.findParent(p0e1)).andReturn(period0).times(PERIOD_0_REPS - 1);
        expect(templateService.findParent(p1e0)).andReturn(period1).times(PERIOD_1_REPS - 1);
        expect(templateService.findParent(p1e1)).andReturn(period1).times(PERIOD_1_REPS - 1);

        getScheduledActivityFixture(p0e0, 2).unscheduleIfOutstanding("Repetition 3 removed in revision " + REVISION_DISPLAY_NAME);
        getScheduledActivityFixture(p0e0, 1).unscheduleIfOutstanding("Repetition 2 removed in revision " + REVISION_DISPLAY_NAME);
        getScheduledActivityFixture(p0e1, 2).unscheduleIfOutstanding("Repetition 3 removed in revision " + REVISION_DISPLAY_NAME);
        getScheduledActivityFixture(p0e1, 1).unscheduleIfOutstanding("Repetition 2 removed in revision " + REVISION_DISPLAY_NAME);

        replayMocks();
        getMutator().apply(scheduledCalendar);
        verifyMocks();
    }
}
