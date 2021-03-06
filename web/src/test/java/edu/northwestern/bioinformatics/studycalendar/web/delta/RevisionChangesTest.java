/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.delta;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.DynamicMockDaoFinder;
import edu.northwestern.bioinformatics.studycalendar.domain.Child;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivityLabel;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.ChildrenChange;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Reorder;

import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import static org.easymock.classextension.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
public class RevisionChangesTest extends StudyCalendarTestCase {
    private Study study;
    private Amendment rev;
    private Epoch treatment;
    private StudySegment studySegmentB;
    private DynamicMockDaoFinder mockDaoFinder;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        rev = new Amendment();
        study = createBasicTemplate();
        study.setDevelopmentAmendment(rev);
        treatment = study.getPlannedCalendar().getEpochs().get(0);
        studySegmentB = treatment.getStudySegments().get(1);
        edu.northwestern.bioinformatics.studycalendar.core.Fixtures.assignIds(study);

        mockDaoFinder = new DynamicMockDaoFinder();
    }

    public void testChangesFlattened() throws Exception {
        getTestingDeltaService().updateRevision(rev, treatment, PropertyChange.create("name", "Treatment", "Megatreatment"));
        getTestingDeltaService().updateRevision(rev, treatment, Reorder.create(treatment.getStudySegments().get(1), 1, 2));

        RevisionChanges c = new RevisionChanges(mockDaoFinder, rev, study);
        List<RevisionChanges.Flat> flattened = c.getFlattened();

        assertEquals(2, flattened.size());
        assertTrue(flattened.get(0).getChange() instanceof PropertyChange);
        assertEquals(treatment, flattened.get(0).getNode());
        assertTrue(flattened.get(1).getChange() instanceof Reorder);
        assertEquals(treatment, flattened.get(1).getNode());
    }

    public void testChangesOnlyForTargetNodeAndChildrenIfProvided() throws Exception {
        Period p1 = setId(1, Fixtures.createPeriod("P1", 3, 6, 1));
        Period p2 = setId(2, Fixtures.createPeriod("P2", 1, 17, 42));
        PlannedActivity pa1 = setId(3, createPlannedActivity("PA1", 5));
        PlannedActivity pa2 = setId(4, createPlannedActivity("PA2", 7));
        studySegmentB.addPeriod(p1);
        p1.addPlannedActivity(pa1);
        studySegmentB.addPeriod(p2);
        p2.addPlannedActivity(pa2);

        getTestingDeltaService().updateRevision(rev, treatment, PropertyChange.create("name", "Treatment", "Megatreatment"));
        getTestingDeltaService().updateRevision(rev, studySegmentB, PropertyChange.create("name", "B", "Beta"));
        getTestingDeltaService().updateRevision(rev, p1, PropertyChange.create("name", null, "Aleph"));
        getTestingDeltaService().updateRevision(rev, p2, PropertyChange.create("name", null, "Zep"));
        getTestingDeltaService().updateRevision(rev, pa1, PropertyChange.create("day", "5", "9"));
        getTestingDeltaService().updateRevision(rev, pa2, PropertyChange.create("day", "7", "4"));

        List<RevisionChanges.Flat> forPeriod = new RevisionChanges(mockDaoFinder, rev, study, p1).getFlattened();
        assertEquals("Wrong list: " + forPeriod, 2, forPeriod.size());
        assertEquals(p1, forPeriod.get(0).getNode());
        assertEquals("name", ((PropertyChange) forPeriod.get(0).getChange()).getPropertyName());
        assertEquals(pa1, forPeriod.get(1).getNode());
        assertEquals("day", ((PropertyChange) forPeriod.get(1).getChange()).getPropertyName());

        assertEquals(1, new RevisionChanges(mockDaoFinder, rev, study, pa1).getFlattened().size());
        assertEquals(5, new RevisionChanges(mockDaoFinder, rev, study, studySegmentB).getFlattened().size());
        assertEquals(6, new RevisionChanges(mockDaoFinder, rev, study, treatment).getFlattened().size());
        assertEquals(6, new RevisionChanges(mockDaoFinder, rev, study, study.getPlannedCalendar()).getFlattened().size());
    }
    
    public void testNodeNameForNamed() throws Exception {
        assertEquals("Test setup failure", "Treatment", treatment.getName());
        assertEquals("epoch Treatment", RevisionChanges.getNodeName(treatment));
    }

    public void testNodeNameForNamedWithNoName() throws Exception {
        assertEquals("unnamed period", RevisionChanges.getNodeName(new Period()));
    }

    public void testNodeNameForNamedWithDynamicSubclasses() throws Exception {
        assertEquals("epoch 7", RevisionChanges.getNodeName(new Epoch() { @Override public String getName() { return "7"; } }));
        assertEquals("studySegment 7", RevisionChanges.getNodeName(new StudySegment() { @Override public String getName() { return "7"; } }));
        assertEquals("period 7", RevisionChanges.getNodeName(new Period() { @Override public String getName() { return "7"; } }));
    }

    public void testNodeNameForPlannedCalendar() throws Exception {
        assertEquals("the template", RevisionChanges.getNodeName(new PlannedCalendar()));
    }

    public void testNodeNameForPlannedActivityWithActivity() throws Exception {
        PlannedActivity pe = Fixtures.createPlannedActivity("CBC", 4);
        assertEquals("a planned CBC", RevisionChanges.getNodeName(pe));
    }
    
    public void testNodeNameForPlannedActivityWithoutActivity() throws Exception {
        assertEquals("a planned activity", RevisionChanges.getNodeName(new PlannedActivity()));
    }

    public void testNodeNameForPopulation() throws Exception {
        assertEquals("unnamed population", RevisionChanges.getNodeName(new Population()));
    }

    public void testNodeNameForLabel() throws Exception {
        PlannedActivityLabel label = new PlannedActivityLabel();
        label.setLabel("foo");
        assertEquals("the label foo on all", RevisionChanges.getNodeName(label));
    }

    public void testSentenceForReorderUp() throws Exception {
        getTestingDeltaService().updateRevision(rev, treatment, Reorder.create(treatment.getStudySegments().get(2), 2, 0));
        assertSingleSentence("Move studySegment C up 2 spaces in epoch Treatment");
    }

    public void testSentenceForReorderDown() throws Exception {
        getTestingDeltaService().updateRevision(rev, treatment, Reorder.create(studySegmentB, 1, 2));
        assertSingleSentence("Move studySegment B down 1 space in epoch Treatment");
    }

    public void testSentenceForPropChange() throws Exception {
        getTestingDeltaService().updateRevision(rev, treatment, PropertyChange.create("name", "Treatment", "Fixing"));
        assertSingleSentence("Epoch Treatment name changed from \"Treatment\" to \"Fixing\"");
    }

    public void testSentenceForAdd() throws Exception {
        getTestingDeltaService().updateRevision(rev, treatment, Add.create(studySegmentB));
        assertSingleSentence("Add studySegment B to epoch Treatment");
    }

    public void testSentenceForRemove() throws Exception {
        getTestingDeltaService().updateRevision(rev, treatment, Remove.create(studySegmentB));
        assertSingleSentence("Remove studySegment B from epoch Treatment");
    }

    public void testChildChangesResolvedIfNecessary() throws Exception {
        int expectedRemoveChildId = 17;
        StudySegment expectedStudySegment = new StudySegment();

        Remove remove = new Remove();
        remove.setChildId(expectedRemoveChildId);
        getTestingDeltaService().updateRevision(rev, treatment, remove);

        expect(mockDaoFinder.expectDaoFor(StudySegment.class).getById(expectedRemoveChildId))
            .andReturn(expectedStudySegment);

        replayMocks();
        List<RevisionChanges.Flat> actualFlattened
            = new RevisionChanges(mockDaoFinder, rev, study).getFlattened();
        verifyMocks();

        assertEquals(1, actualFlattened.size());
        Child<?> actualChild = ((ChildrenChange) actualFlattened.get(0).getChange()).getChild();
        assertSame("Child not realized", expectedStudySegment, actualChild);
    }

    private void assertSingleSentence(String expected) {
        List<RevisionChanges.Flat> actual
            = new RevisionChanges(mockDaoFinder, rev, study).getFlattened();
        assertEquals(1, actual.size());
        assertEquals(expected, actual.get(0).getSentence());
    }

    @Override
    protected void replayMocks() {
        super.replayMocks();
        mockDaoFinder.replayAll();
    }

    @Override
    protected void verifyMocks() {
        super.verifyMocks();
        mockDaoFinder.verifyAll();
    }

    @Override
    protected void resetMocks() {
        super.resetMocks();
        mockDaoFinder.resetAll();
    }
}
