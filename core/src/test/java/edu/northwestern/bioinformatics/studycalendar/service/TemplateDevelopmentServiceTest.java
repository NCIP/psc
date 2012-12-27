/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.Child;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import org.easymock.IArgumentMatcher;
import org.easymock.classextension.EasyMock;
import static org.easymock.classextension.EasyMock.same;

/**
 * @author Rhett Sutphin
 */
public class TemplateDevelopmentServiceTest extends StudyCalendarTestCase {
    private TemplateDevelopmentService service;

    private DeltaService deltaService;
    private AmendmentService amendmentService;
    private Study studyA;
    private Study studyB;
    private Period source;
    private Activity a1;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        a1 = createActivity("1");

        studyA = assignIds(createBasicTemplate(), 10); studyA.setName("A");
        studyB = assignIds(createBasicTemplate(), 20); studyB.setName("B");
        source = createPeriod(4, 7, 2);

        studyA.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(0).addPeriod(source);

        amendmentService = registerMockFor(AmendmentService.class);
        deltaService = registerMockFor(DeltaService.class);

        service = new TemplateDevelopmentService();
        service.setAmendmentService(amendmentService);
        service.setTemplateService(new TestingTemplateService());
        service.setDeltaService(deltaService);
    }

    private Period expectedPeriodCopy() {
        return createPeriod(4, 7, 2);
    }

    private Period doCopyPeriod(Period expectedCopy, StudySegment expectedTarget) {
        return doCopyPeriod(source, expectedCopy, expectedTarget);
    }

    private Period doCopyPeriod(Period expectedSource, Period expectedCopy, StudySegment expectedTarget) {
        EasyMock.expect(
            amendmentService.updateDevelopmentAmendmentAndSave(same(expectedTarget), addFor(expectedCopy))).
            andReturn(null /* DC */);

        replayMocks();
        Period result = service.copyPeriod(expectedSource, expectedTarget);
        verifyMocks();
        return result;
    }
    
    public void testCopyPeriodReturnsNewPeriod() throws Exception {
        StudySegment target = studyB.getPlannedCalendar().getEpochs().get(1).getStudySegments().get(0);
        Period expectedCopyTemplate = expectedPeriodCopy();
        Period actual = doCopyPeriod(expectedCopyTemplate, target);

        assertNotSame(source, actual);
        assertNull(actual.getId());
    }

    public void testCopyPeriodToDifferentStudy() throws Exception {
        source.addPlannedActivity(setIds(7, createPlannedActivity(a1, 4)));
        StudySegment target = studyB.getPlannedCalendar().getEpochs().get(1).getStudySegments().get(0);

        Period expectedCopy = expectedPeriodCopy();
        expectedCopy.addPlannedActivity(createPlannedActivity(a1, 4));

        doCopyPeriod(expectedCopy, target);
    }

    public void testCopyPeriodToDifferentStudyWithPopulationWhenPlannedActivityDoesNotHaveDetails() throws Exception {
        Population pop = createPopulation("P", "People");
        PlannedActivity pa = createPlannedActivity(a1, 4);
        pa.setPopulation(pop);
        source.addPlannedActivity(setIds(7, pa));

        StudySegment target = studyB.getPlannedCalendar().getEpochs().get(1).getStudySegments().get(0);

        Period expectedCopy = expectedPeriodCopy();
        PlannedActivity expectedPA = createPlannedActivity(a1, 4);
        expectedPA.setDetails("Copied from A, where it was restricted to P: People");
        expectedCopy.addPlannedActivity(expectedPA);

        doCopyPeriod(expectedCopy, target);
    }

    public void testCopyPeriodToDifferentStudyWithPopulationWhenPlannedActivityHasDetails() throws Exception {
        Population pop = createPopulation("P", "People");
        PlannedActivity pa = createPlannedActivity(a1, 4);
        pa.setDetails("Existing details");
        pa.setPopulation(pop);
        source.addPlannedActivity(setIds(7, pa));

        StudySegment target = studyB.getPlannedCalendar().getEpochs().get(1).getStudySegments().get(0);

        Period expectedCopy = expectedPeriodCopy();
        PlannedActivity expectedPA = createPlannedActivity(a1, 4);
        expectedPA.setDetails("Existing details (Copied from A, where it was restricted to P: People)");
        expectedCopy.addPlannedActivity(expectedPA);

        doCopyPeriod(expectedCopy, target);
    }

    public void testCopyPeriodToSameStudy() throws Exception {
        source.addPlannedActivity(setIds(7, createPlannedActivity(a1, 4)));
        StudySegment target = studyA.getPlannedCalendar().getEpochs().get(1).getStudySegments().get(0);

        Period expectedCopy = expectedPeriodCopy();
        expectedCopy.addPlannedActivity(createPlannedActivity(a1, 4));

        Period revisedPeriod = (Period) source.transientClone();
        EasyMock.expect(deltaService.revise(source)).andReturn(revisedPeriod);
        doCopyPeriod(revisedPeriod, expectedCopy, target);
    }

    public void testCopyPeriodToSameStudyWithPopulation() throws Exception {
        Population pop = createPopulation();
        PlannedActivity pa = createPlannedActivity(a1, 4);
        pa.setPopulation(pop);
        source.addPlannedActivity(setIds(7, pa));

        StudySegment target = studyA.getPlannedCalendar().getEpochs().get(1).getStudySegments().get(0);

        Period expectedCopy = expectedPeriodCopy();
        PlannedActivity expectedPA = createPlannedActivity(a1, 4);
        expectedPA.setPopulation(pop);
        expectedCopy.addPlannedActivity(expectedPA);

        Period revisedPeriod = (Period) source.transientClone();
        EasyMock.expect(deltaService.revise(source)).andReturn(revisedPeriod);
        doCopyPeriod(revisedPeriod, expectedCopy, target);
    }

    public void testCopyPeriodDetachesCopyFromSourceStudy() throws Exception {
        source.addPlannedActivity(setIds(7, createPlannedActivity(a1, 4)));
        StudySegment target = studyB.getPlannedCalendar().getEpochs().get(1).getStudySegments().get(0);

        Period expectedCopy = expectedPeriodCopy();
        expectedCopy.addPlannedActivity(createPlannedActivity(a1, 4));
        expectedCopy.setStudySegment(new StudySegment());

        Period copy = doCopyPeriod(expectedCopy, target);
        assertNull(copy.getStudySegment());
    }

    public static Add addFor(Period expected) {
        EasyMock.reportMatcher(new AddForPeriodMatcher(expected));
        return null;
    }

    private static class AddForPeriodMatcher implements IArgumentMatcher {
        private Period expected;

        public AddForPeriodMatcher(Period expected) {
            this.expected = expected;
        }

        public boolean matches(Object o) {
            assertTrue("Object is not an Add", o instanceof Add);
            Child<?> child = ((Add) o).getChild();
            assertNotNull("Add has no child", child);
            assertTrue("Add is not for a period", child instanceof Period);
            Period actual = (Period) child;
            assertEquals("Period properties don't match", expected, actual);
            assertEquals("Wrong number of planned activities",
                expected.getPlannedActivities().size(), actual.getPlannedActivities().size());
            for (int i = 0; i < expected.getPlannedActivities().size(); i++) {
                PlannedActivity expectedPA = expected.getPlannedActivities().get(i);
                PlannedActivity actualPA = actual.getPlannedActivities().get(i);
                assertSame("Population not same at PA " + i,
                    expectedPA.getPopulation(), actualPA.getPopulation());
                assertEquals("Mismatch at planned activity " + i, expectedPA, actualPA);
            }
            return true;
        }

        public void appendTo(StringBuffer sb) {
            sb.append("add change for ").append(expected);
        }
    }
}
