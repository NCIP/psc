/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import static org.easymock.classextension.EasyMock.*;

/**
 * @author Rhett Sutphin
 */
public class RemovePeriodMutatorTest extends PeriodMutatorTestCase<Remove> {
    private PeriodDao periodDao;
    private TemplateService templateService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        periodDao = registerDaoMockFor(PeriodDao.class);
        templateService = registerMockFor(TemplateService.class);
    }

    @Override
    protected RemovePeriodMutator createMutator() {
        return new RemovePeriodMutator(change, periodDao, templateService);
    }

    @Override
    protected Remove createChange() {
        return Remove.create(period0);
    }

    @Override
    protected Delta<?> createDelta() {
        return Delta.createDeltaFor(studySegment, change);
    }

    public void testDoesNothingWhenStudySegmentNotUsed() throws Exception {
        scheduledCalendar.getScheduledStudySegments().clear();
        scheduledCalendar.addStudySegment(createScheduledStudySegment(createNamedInstance("Some other study segment", StudySegment.class)));

        replayMocks();
        getMutator().apply(scheduledCalendar);
        verifyMocks();
    }

    public void testUnschedulesScheduledActivitiesFromPeriodOnly() throws Exception {
        expect(templateService.findParent(p0e0)).andReturn(period0).times(3);
        expect(templateService.findParent(p0e1)).andReturn(period0).times(3);
        expect(templateService.findParent(p1e0)).andReturn(period1).times(2);
        expect(templateService.findParent(p1e1)).andReturn(period1).times(2);

        String expectedMessage = "Removed in revision " + REVISION_DISPLAY_NAME;
        getScheduledActivityFixture(p0e0, 0).unscheduleIfOutstanding(expectedMessage);
        getScheduledActivityFixture(p0e1, 0).unscheduleIfOutstanding(expectedMessage);
        getScheduledActivityFixture(p0e0, 1).unscheduleIfOutstanding(expectedMessage);
        getScheduledActivityFixture(p0e1, 1).unscheduleIfOutstanding(expectedMessage);
        getScheduledActivityFixture(p0e0, 2).unscheduleIfOutstanding(expectedMessage);
        getScheduledActivityFixture(p0e1, 2).unscheduleIfOutstanding(expectedMessage);

        replayMocks();
        getMutator().apply(scheduledCalendar);
        verifyMocks();
    }
}
