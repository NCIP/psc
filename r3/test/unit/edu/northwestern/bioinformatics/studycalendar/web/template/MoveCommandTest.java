package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Named;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;

import java.util.List;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class MoveCommandTest extends StudyCalendarTestCase {
    private MoveCommand command;
    private Study study;

    protected void setUp() throws Exception {
        super.setUp();
        command = new MoveCommand();

        study = Fixtures.createSingleEpochStudy("A", "E1");
        study.getPlannedCalendar().addEpoch(
            Epoch.create("E2", "A", "B", "C", "D")
        );
        study.getPlannedCalendar().addEpoch(Epoch.create("E3"));
        assertOrder(study.getPlannedCalendar().getEpochs(), "E1", "E2", "E3");
    }

    public void testMoveEpochUp() throws Exception {
        List<Epoch> epochs = study.getPlannedCalendar().getEpochs();
        command.setOffset(-1);
        command.setEpoch(epochs.get(1));

        command.performEdit();
        assertOrder(epochs, "E2", "E1", "E3");
    }

    public void testMoveFirstEpochUp() throws Exception {
        List<Epoch> epochs = study.getPlannedCalendar().getEpochs();
        command.setOffset(-1);
        command.setEpoch(epochs.get(0));

        command.performEdit();
        assertOrder(epochs, "E1", "E2", "E3");
    }

    public void testMoveEpochDown() throws Exception {
        List<Epoch> epochs = study.getPlannedCalendar().getEpochs();
        command.setOffset(1);
        command.setEpoch(epochs.get(1));
        command.performEdit();
        assertOrder(epochs, "E1", "E3", "E2");
    }

    public void testMoveLastEpochDown() throws Exception {
        List<Epoch> epochs = study.getPlannedCalendar().getEpochs();
        command.setOffset(1);
        command.setEpoch(epochs.get(2));
        command.performEdit();
        assertOrder(epochs, "E1", "E2", "E3");
    }

    public void testMoveArmUp() throws Exception {
        List<Arm> arms = study.getPlannedCalendar().getEpochs().get(1).getArms();
        command.setOffset(-1);
        command.setArm(arms.get(2));

        command.performEdit();
        assertOrder(arms, "A", "C", "B", "D");
    }

    public void testMoveFirstArmUp() throws Exception {
        List<Arm> arms = study.getPlannedCalendar().getEpochs().get(1).getArms();
        command.setOffset(-1);
        command.setArm(arms.get(0));

        command.performEdit();
        assertOrder(arms, "A", "B", "C", "D");
    }

    public void testMoveArmDown() throws Exception {
        List<Arm> arms = study.getPlannedCalendar().getEpochs().get(1).getArms();
        command.setOffset(1);
        command.setArm(arms.get(0));

        command.performEdit();
        assertOrder(arms, "B", "A", "C", "D");
    }

    public void testMoveLastArmDown() throws Exception {
        List<Arm> arms = study.getPlannedCalendar().getEpochs().get(1).getArms();
        command.setOffset(1);
        command.setArm(arms.get(3));

        command.performEdit();
        assertOrder(arms, "A", "B", "C", "D");
    }

    public void testMoveEpochUpModel() throws Exception {
        List<Epoch> epochs = study.getPlannedCalendar().getEpochs();
        command.setOffset(-1);
        command.setEpoch(epochs.get(1));
        command.performEdit();

        Map<String, Object> model = command.getModel();
        assertEquals("before", model.get("position"));
        assertSame(epochs.get(1), model.get("relativeTo"));
    }

    public void testMoveEpochDownModel() throws Exception {
        List<Epoch> epochs = study.getPlannedCalendar().getEpochs();
        command.setOffset(1);
        command.setEpoch(epochs.get(0));
        command.performEdit();

        Map<String, Object> model = command.getModel();
        assertEquals("before", model.get("position"));
        assertSame(epochs.get(2), model.get("relativeTo"));
    }

    public void testMoveEpochToEndModel() throws Exception {
        List<Epoch> epochs = study.getPlannedCalendar().getEpochs();
        command.setOffset(1);
        command.setEpoch(epochs.get(1));
        command.performEdit();

        Map<String, Object> model = command.getModel();
        assertEquals("after", model.get("position"));
        assertSame(epochs.get(1), model.get("relativeTo"));
    }

    private void assertOrder(List<? extends Named> actualObjs, String... expectedOrder) {
        assertEquals("Wrong number of epochs", expectedOrder.length, actualObjs.size());
        for (int i = 0; i < expectedOrder.length; i++) {
            String expectedName = expectedOrder[i];
            Named actual = actualObjs.get(i);
            assertEquals("Mismatch at " + i, expectedName, actual.getName());
        }
    }
}
