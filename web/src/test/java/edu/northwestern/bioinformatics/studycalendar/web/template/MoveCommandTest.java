/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Named;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.DeltaAssertions;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import gov.nih.nci.cabig.ctms.domain.DomainObject;

import java.util.List;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class MoveCommandTest extends EditCommandTestCase {
    private MoveCommand command;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        command = new MoveCommand();
        // Not using a mock here because we need to test use of the stored vs. revised template
        command.setDeltaService(getTestingDeltaService());

        study.getPlannedCalendar().addEpoch(Epoch.create("E1"));
        study.getPlannedCalendar().addEpoch(
            Epoch.create("E2", "A", "B", "C", "D")
        );
        study.getPlannedCalendar().addEpoch(Epoch.create("E3"));
        assignIds(study);

        command.setStudy(study);

        assertOrder(study.getPlannedCalendar().getEpochs(), "E1", "E2", "E3");
    }

    public void testMoveEpochUp() throws Exception {
        List<Epoch> epochs = study.getPlannedCalendar().getEpochs();
        command.setOffset(-1);
        command.setEpoch(epochs.get(1));

        doEdit();

        DeltaAssertions.assertReorder("Change not registered", epochs.get(1), 1, 0, lastChange());
    }

    public void testMoveFirstEpochUp() throws Exception {
        List<Epoch> epochs = study.getPlannedCalendar().getEpochs();
        command.setOffset(-1);
        command.setEpoch(epochs.get(0));

        doEdit();

        assertEquals("Should be no changes", 0, dev.getDeltas().size());
    }

    public void testMoveEpochDown() throws Exception {
        List<Epoch> epochs = study.getPlannedCalendar().getEpochs();
        command.setOffset(1);
        command.setEpoch(epochs.get(1));

        doEdit();

        DeltaAssertions.assertReorder("Change not registered", epochs.get(1), 1, 2, lastChange());
    }

    public void testMoveLastEpochDown() throws Exception {
        List<Epoch> epochs = study.getPlannedCalendar().getEpochs();
        command.setOffset(1);
        command.setEpoch(epochs.get(2));

        doEdit();

        assertEquals("Should be no changes", 0, dev.getDeltas().size());
    }

    public void testMoveStudySegmentUp() throws Exception {
        Epoch epoch = study.getPlannedCalendar().getEpochs().get(1);
        List<StudySegment> studySegments = epoch.getStudySegments();
        command.setOffset(-1);
        command.setStudySegment(studySegments.get(2));

        doEdit();

        DeltaAssertions.assertReorder("Change not registered", studySegments.get(2), 2, 1, lastChange());
    }

    public void testMoveFirstStudySegmentUp() throws Exception {
        List<StudySegment> studySegments = study.getPlannedCalendar().getEpochs().get(1).getStudySegments();
        command.setOffset(-1);
        command.setStudySegment(studySegments.get(0));

        doEdit();

        assertEquals("Should be no changes", 0, dev.getDeltas().size());
    }

    public void testMoveStudySegmentDown() throws Exception {
        Epoch epoch = study.getPlannedCalendar().getEpochs().get(1);
        List<StudySegment> studySegments = epoch.getStudySegments();
        command.setOffset(1);
        command.setStudySegment(studySegments.get(0));

        doEdit();

        DeltaAssertions.assertReorder("Change not registered", studySegments.get(0), 0, 1, lastChange());
    }

    public void testMoveLastStudySegmentDown() throws Exception {
        List<StudySegment> studySegments = study.getPlannedCalendar().getEpochs().get(1).getStudySegments();
        command.setOffset(1);
        command.setStudySegment(studySegments.get(3));

        doEdit();

        assertEquals(0, dev.getDeltas().size());
    }

    public void testMoveEpochUpModel() throws Exception {
        List<Epoch> epochs = study.getPlannedCalendar().getEpochs();
        command.setOffset(-1);
        command.setEpoch(epochs.get(1));

        doEdit();

        Map<String, Object> model = command.getModel();
        assertEquals("before", model.get("position"));
        assertEquals(epochs.get(0).getId(), ((DomainObject) model.get("relativeTo")).getId());
    }

    public void testMoveEpochDownModel() throws Exception {
        List<Epoch> epochs = study.getPlannedCalendar().getEpochs();
        command.setOffset(1);
        command.setEpoch(epochs.get(0));

        doEdit();

        Map<String, Object> model = command.getModel();
        assertEquals("before", model.get("position"));
        assertSame(epochs.get(2).getId(), ((DomainObject) model.get("relativeTo")).getId());
    }

    public void testMoveEpochToEndModel() throws Exception {
        List<Epoch> epochs = study.getPlannedCalendar().getEpochs();
        command.setOffset(1);
        command.setEpoch(epochs.get(1));
        doEdit();

        Map<String, Object> model = command.getModel();
        assertEquals("after", model.get("position"));
        assertSame(epochs.get(2).getId(), ((DomainObject) model.get("relativeTo")).getId());
    }

    public void testMoveNewlyAddedEpoch() throws Exception {
        assertEquals("Expected three epochs to begin with", 3,
            study.getPlannedCalendar().getEpochs().size());
        Epoch newlyAdded = setId(74, Epoch.create("New"));
        command.updateRevision(study.getPlannedCalendar(), Add.create(newlyAdded));
        command.setOffset(-1);
        command.setEpoch(newlyAdded);

        doEdit();

        DeltaAssertions.assertReorder("Change not registered", newlyAdded, 3, 2, lastChange());
    }

    private void assertOrder(List<? extends Named> actualObjs, String... expectedOrder) {
        assertEquals("Wrong number of epochs", expectedOrder.length, actualObjs.size());
        for (int i = 0; i < expectedOrder.length; i++) {
            String expectedName = expectedOrder[i];
            Named actual = actualObjs.get(i);
            assertEquals("Mismatch at " + i, expectedName, actual.getName());
        }
    }

    private void doEdit() {
        command.performEdit();
    }

}
