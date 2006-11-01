package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;

import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class DeleteCommandTest extends StudyCalendarTestCase {
    private DeleteCommand command;
    private Study study;

    protected void setUp() throws Exception {
        super.setUp();
        command = new DeleteCommand();
        study = Fixtures.createSingleEpochStudy("S", "E1", "A", "B", "C");
        study.getPlannedCalendar().addEpoch(Epoch.create("E2"));
    }

    public void testDeleteEpoch() throws Exception {
        List<Epoch> epochs = study.getPlannedCalendar().getEpochs();
        assertEquals(2, epochs.size());
        assertEquals("E1", epochs.get(0).getName());

        command.setEpoch(epochs.get(0));
        command.performEdit();

        assertEquals(1, epochs.size());
        assertEquals("E2", epochs.get(0).getName());
    }
    
    public void testDeleteLastEpochIsNoop() throws Exception {
        study.getPlannedCalendar().getEpochs().remove(1);

        List<Epoch> epochs = study.getPlannedCalendar().getEpochs();
        assertEquals(1, epochs.size());
        assertEquals("E1", epochs.get(0).getName());

        command.setEpoch(epochs.get(0));
        command.performEdit();

        assertEquals(1, epochs.size());
        assertEquals("E1", epochs.get(0).getName());
    }
    
    public void testDeleteArm() throws Exception {
        List<Arm> arms = study.getPlannedCalendar().getEpochs().get(0).getArms();
        assertEquals(3, arms.size());
        assertEquals("A", arms.get(0).getName());
        assertEquals("B", arms.get(1).getName());
        assertEquals("C", arms.get(2).getName());

        command.setArm(arms.get(1));
        command.performEdit();

        assertEquals(2, arms.size());
        assertEquals("A", arms.get(0).getName());
        assertEquals("C", arms.get(1).getName());
    }

    public void testDeleteLastArmIsNoop() throws Exception {
        List<Arm> arms = study.getPlannedCalendar().getEpochs().get(1).getArms();
        assertEquals(1, arms.size());
        assertEquals("E2", arms.get(0).getName());

        command.setArm(arms.get(0));
        command.performEdit();

        assertEquals(1, arms.size());
        assertEquals("E2", arms.get(0).getName());
    }

    public void testEpochView() throws Exception {
        command.setEpoch(study.getPlannedCalendar().getEpochs().get(1));
        assertEquals("deleteEpoch", command.getRelativeViewName());
    }

    public void testArmView() throws Exception {
        command.setArm(study.getPlannedCalendar().getEpochs().get(1).getArms().get(0));
        assertEquals("deleteArm", command.getRelativeViewName());
    }
}
