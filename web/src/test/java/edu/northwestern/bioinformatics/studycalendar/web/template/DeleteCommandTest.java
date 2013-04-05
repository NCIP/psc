/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.setId;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.DeltaAssertions;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;

import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class DeleteCommandTest extends EditCommandTestCase {
    private DeleteCommand command;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        command = new DeleteCommand();
        command.setDeltaService(Fixtures.getTestingDeltaService());
        study.getPlannedCalendar().addEpoch(Epoch.create("E1", "A", "B", "C"));
        study.getPlannedCalendar().addEpoch(Epoch.create("E2"));
        Fixtures.assignIds(study);
        command.setStudy(study);
    }

    public void testDeleteEpoch() throws Exception {
        List<Epoch> epochs = study.getPlannedCalendar().getEpochs();
        assertEquals(2, epochs.size());
        assertEquals("E1", epochs.get(0).getName());

        command.setEpoch(epochs.get(0));
        command.performEdit();

        DeltaAssertions.assertRemove("Wrong change", epochs.get(0), lastChange());
    }
    public void testDeleteLastEpochIsNoop() throws Exception {
        study.getPlannedCalendar().getEpochs().remove(1);

        List<Epoch> epochs = study.getPlannedCalendar().getEpochs();
        assertEquals(1, epochs.size());
        assertEquals("E1", epochs.get(0).getName());

        command.setEpoch(epochs.get(0));
        command.performEdit();

        assertEquals("Should be no deltas", 0, dev.getDeltas().size());
    }
    
    public void testDeleteStudySegment() throws Exception {
        List<StudySegment> studySegments = study.getPlannedCalendar().getEpochs().get(0).getStudySegments();
        assertEquals(3, studySegments.size());
        assertEquals("A", studySegments.get(0).getName());
        assertEquals("B", studySegments.get(1).getName());
        assertEquals("C", studySegments.get(2).getName());

        command.setStudySegment(studySegments.get(1));
        command.performEdit();

        DeltaAssertions.assertRemove("Wrong change", studySegments.get(1), lastChange());
    }

    public void testDeleteLastStudySegmentIsNoop() throws Exception {
        List<StudySegment> studySegments = study.getPlannedCalendar().getEpochs().get(1).getStudySegments();
        assertEquals(1, studySegments.size());
        assertEquals("E2", studySegments.get(0).getName());

        command.setStudySegment(studySegments.get(0));
        command.performEdit();

        assertEquals("Should be no deltas", 0, dev.getDeltas().size());
    }

    public void testEpochView() throws Exception {
        command.setEpoch(study.getPlannedCalendar().getEpochs().get(1));
        assertEquals("deleteEpoch", command.getRelativeViewName());
    }

    public void testStudySegmentView() throws Exception {
        command.setStudySegment(study.getPlannedCalendar().getEpochs().get(1).getStudySegments().get(0));
        assertEquals("deleteStudySegment", command.getRelativeViewName());
    }

    public void testDeleteNewlyAddedEpoch() throws Exception {
        assertEquals("Expected three epochs to begin with", 2,
            study.getPlannedCalendar().getEpochs().size());
        Epoch newlyAdded = setId(74, Epoch.create("New"));
        command.updateRevision(study.getPlannedCalendar(), Add.create(newlyAdded));
        command.setEpoch(newlyAdded);

        command.performEdit();

        // deleting a newly added object cancels the add, so...
        assertEquals("Add not canceled: " + dev.getDeltas().get(0).getChanges(), 0,
            dev.getDeltas().get(0).getChanges().size());
    }
}
