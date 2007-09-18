package edu.northwestern.bioinformatics.studycalendar.web.delta;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Reorder;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.ChildrenChange;
import edu.northwestern.bioinformatics.studycalendar.dao.DynamicMockDaoFinder;

import java.util.List;

import static org.easymock.classextension.EasyMock.*;

/**
 * @author Rhett Sutphin
 */
public class RevisionChangesTest extends StudyCalendarTestCase {
    private Study study;
    private Amendment rev;
    private Epoch treatment;
    private Arm armB;
    private DynamicMockDaoFinder mockDaoFinder;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        rev = new Amendment();
        study = createBasicTemplate();
        study.setDevelopmentAmendment(rev);
        treatment = study.getPlannedCalendar().getEpochs().get(1);
        armB = treatment.getArms().get(1);

        mockDaoFinder = new DynamicMockDaoFinder();
    }

    public void testChangesFlattened() throws Exception {
        getTestingDeltaService().updateRevision(rev, treatment, PropertyChange.create("name", "Treatment", "Megatreatment"));
        getTestingDeltaService().updateRevision(rev, treatment, Reorder.create(treatment.getArms().get(0), 0, 1));

        RevisionChanges c = new RevisionChanges(mockDaoFinder, rev, study);
        List<RevisionChanges.Flat> flattened = c.getFlattened();

        assertEquals(2, flattened.size());
        assertTrue(flattened.get(0).getChange() instanceof PropertyChange);
        assertEquals(treatment, flattened.get(0).getNode());
        assertTrue(flattened.get(1).getChange() instanceof Reorder);
        assertEquals(treatment, flattened.get(1).getNode());
    }
    
    public void testNodeNameForNamed() throws Exception {
        assertEquals("Test setup failure", "Treatment", treatment.getName());
        assertEquals("epoch Treatment", RevisionChanges.getNodeName(treatment));
    }

    public void testNodeNameForNamedWithNoName() throws Exception {
        assertEquals("unnamed arm", RevisionChanges.getNodeName(new Arm()));
        assertEquals("unnamed period", RevisionChanges.getNodeName(new Period()));
    }

    public void testNodeNameForPlannedCalendar() throws Exception {
        assertEquals("the template", RevisionChanges.getNodeName(new PlannedCalendar()));
    }

    public void testNodeNameForPlannedEventWithActivity() throws Exception {
        PlannedEvent pe = Fixtures.createPlannedEvent("CBC", 4);
        assertEquals("a planned CBC", RevisionChanges.getNodeName(pe));
    }
    
    public void testNodeNameForPlannedEventWithoutActivity() throws Exception {
        assertEquals("a planned activity", RevisionChanges.getNodeName(new PlannedEvent()));
    }

    public void testSentenceForReorderUp() throws Exception {
        getTestingDeltaService().updateRevision(rev, treatment, Reorder.create(treatment.getArms().get(2), 2, 0));
        assertSingleSentence("Move arm C up 2 spaces in epoch Treatment");
    }

    public void testSentenceForReorderDown() throws Exception {
        getTestingDeltaService().updateRevision(rev, treatment, Reorder.create(armB, 1, 2));
        assertSingleSentence("Move arm B down 1 space in epoch Treatment");
    }

    public void testSentenceForPropChange() throws Exception {
        getTestingDeltaService().updateRevision(rev, treatment, PropertyChange.create("name", "Treatment", "Fixing"));
        assertSingleSentence("Epoch Treatment name changed from \"Treatment\" to \"Fixing\"");
    }

    public void testSentenceForAdd() throws Exception {
        getTestingDeltaService().updateRevision(rev, treatment, Add.create(armB));
        assertSingleSentence("Add arm B to epoch Treatment");
    }

    public void testSentenceForRemove() throws Exception {
        getTestingDeltaService().updateRevision(rev, treatment, Remove.create(armB));
        assertSingleSentence("Remove arm B from epoch Treatment");
    }

    public void testChildChangesResolvedIfNecessary() throws Exception {
        int expectedRemoveChildId = 17;
        Arm expectedArm = new Arm();

        Remove remove = new Remove();
        remove.setChildId(expectedRemoveChildId);
        getTestingDeltaService().updateRevision(rev, treatment, remove);

        expect(mockDaoFinder.expectDaoFor(Arm.class).getById(expectedRemoveChildId))
            .andReturn(expectedArm);

        replayMocks();
        List<RevisionChanges.Flat> actualFlattened
            = new RevisionChanges(mockDaoFinder, rev, study).getFlattened();
        verifyMocks();

        assertEquals(1, actualFlattened.size());
        PlanTreeNode<?> actualChild = ((ChildrenChange) actualFlattened.get(0).getChange()).getChild();
        assertSame("Child not realized", expectedArm, actualChild);
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
