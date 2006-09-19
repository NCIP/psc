package edu.northwestern.bioinformatics.studycalendar.web;

import junit.framework.TestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;

import java.util.List;
import java.util.Arrays;

/**
 * @author Rhett Sutphin
 */
public class NewStudyCommandTest extends TestCase {
    private NewStudyCommand command = new NewStudyCommand();

    public void testCreateSingleEpochStudyWithNoArms() throws Exception {
        String expectedStudyName = "'armless";
        String expectedEpochName = "Holocene";
        command.setStudyName(expectedStudyName);
        command.getEpochNames().set(0, expectedEpochName);
        command.getArms().set(0, false);

        Study actual = command.createStudy();
        assertEquals(expectedStudyName, actual.getName());
        assertNotNull("Should have schedule", actual.getPlannedCalendar());
        assertSame("Study <-> schedule relationship not bidirectional", actual, actual.getPlannedCalendar().getStudy());
        List<Epoch> actualEpochs = actual.getPlannedCalendar().getEpochs();
        assertEquals("Should have one epoch", 1, actualEpochs.size());
        assertNoArmEpoch(actualEpochs.get(0), expectedEpochName);
    }

    public void testCreateSingleEpochStudyWithArms() throws Exception {
        String expectedStudyName = "'armful";
        String expectedEpochName = "Eocene";
        List<String> expectedArmNames = Arrays.asList("Left arm", "Right arm", "Gripping arm");
        command.setStudyName(expectedStudyName);
        command.getEpochNames().set(0, expectedEpochName);
        command.getArms().set(0, true);
        for (String armName : expectedArmNames) {
            ((List) command.getArmNames().get(0)).add(armName);
        }

        Study actual = command.createStudy();
        assertNotNull("Study is null", actual);
        assertNotNull("Schedule is null", actual.getPlannedCalendar());
        assertEquals(expectedStudyName, actual.getName());

        assertEquals("Should have one epoch", 1, actual.getPlannedCalendar().getEpochs().size());
        assertMultipleArmEpoch(actual.getPlannedCalendar().getEpochs().get(0),
            expectedEpochName, expectedArmNames);
    }

    public void testCreateSingleEpochStudyWithNoArmsButWithArmNames() throws Exception {
        String expectedStudyName = "'armless";
        String expectedEpochName = "Holocene";
        command.setStudyName(expectedStudyName);
        command.getEpochNames().set(0, expectedEpochName);
        command.getArms().set(0, false);
        ((List) command.getArmNames().get(0)).addAll(Arrays.asList("These", "should", "be", "ignored"));

        Study actual = command.createStudy();
        assertEquals(expectedStudyName, actual.getName());
        assertNotNull("Should have schedule", actual.getPlannedCalendar());
        assertSame("Study <-> schedule relationship not bidirectional", actual, actual.getPlannedCalendar().getStudy());
        List<Epoch> actualEpochs = actual.getPlannedCalendar().getEpochs();
        assertEquals("Should have one epoch", 1, actualEpochs.size());
        assertNoArmEpoch(actualEpochs.get(0), expectedEpochName);
    }

    public void testMultipleEpochs() throws Exception {
        List<String> expectedEpochNames = Arrays.asList("Paleocene", "Eocene", "Oligocene");
        List<String> expectedEoceneArmNames = Arrays.asList("Priabonian", "Lutetian");
        command.setStudyName("Complex");
        command.getEpochNames().clear();
        command.getEpochNames().addAll(0, expectedEpochNames);

        command.getArms().add(false); // Paleocene
        command.getArms().add(true);  // Eocene
                                      // Null for Oligocene

        ((List) command.getArmNames().get(1)).addAll(expectedEoceneArmNames);

        Study actual = command.createStudy();
        assertNotNull("No schedule", actual.getPlannedCalendar());
        List<Epoch> actualEpochs = actual.getPlannedCalendar().getEpochs();
        assertEquals("Wrong number of epochs", expectedEpochNames.size(), actualEpochs.size());

        assertNoArmEpoch(actualEpochs.get(0), expectedEpochNames.get(0));
        assertMultipleArmEpoch(actualEpochs.get(1), expectedEpochNames.get(1), expectedEoceneArmNames);
        assertNoArmEpoch(actualEpochs.get(2), expectedEpochNames.get(2));
    }

    private void assertMultipleArmEpoch(Epoch actualEpoch, String expectedEpochName, List<String> expectedArmNames) {
        assertEquals("Epoch has wrong name", expectedEpochName, actualEpoch.getName());

        assertEquals("Wrong number of arms", expectedArmNames.size(), actualEpoch.getArms().size());
        for (int i = 0; i < expectedArmNames.size(); i++) {
            Arm actualArm = actualEpoch.getArms().get(i);
            assertEquals("Wrong arm name at index " + i, expectedArmNames.get(i), actualArm.getName());
            assertSame("Relationship not bidirectional at arm " + i, actualEpoch, actualArm.getEpoch());
        }
    }

    private void assertNoArmEpoch(Epoch actualEpoch, String expectedEpochName) {
        assertEquals("Epoch has wrong name", expectedEpochName, actualEpoch.getName());
        assertEquals("Epoch should have one arm", 1, actualEpoch.getArms().size());
        assertEquals("Should have one arm named after epoch", expectedEpochName, actualEpoch.getArms().get(0).getName());
    }

}
