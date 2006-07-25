package edu.northwestern.bioinformatics.studycalendar.web;

import junit.framework.TestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;

import java.util.List;
import java.util.Arrays;

/**
 * @author Rhett Sutphin
 */
public class NewStudyCommandTest extends TestCase {
    private NewStudyCommand command = new NewStudyCommand();

    public void testCreateStudyWithNoArms() throws Exception {
        String expectedStudyName = "'armless";
        command.setStudyName(expectedStudyName);
        command.setArms(false);

        Study actual = command.createStudy();
        assertEquals(expectedStudyName, actual.getName());
        assertEquals("Should have no arms", 0, actual.getArms().size());
    }

    public void testCreateStudyWithArms() throws Exception {
        String expectedStudyName = "'armful";
        List<String> expectedArmNames = Arrays.asList("Left arm", "Right arm", "Gripping arm");
        command.setStudyName(expectedStudyName);
        command.setArms(true);
        for (String armName : expectedArmNames) {
            command.getArmNames().add(armName);
        }

        Study actual = command.createStudy();
        assertNotNull("Study is null", actual);
        assertEquals(expectedStudyName, actual.getName());
        for (int i = 0; i < expectedArmNames.size(); i++) {
            Arm actualArm = actual.getArms().get(i);
            assertEquals("Wrong arm name at index " + i, expectedArmNames.get(i), actualArm.getName());
            assertSame("Relationship not bidirectional at arm " + i, actual, actualArm.getStudy());
        }
    }
    
    public void testCreateStudyWithNoArmsButWithArmNames() throws Exception {
        String expectedStudyName = "'armful";
        List<String> expectedArmNames = Arrays.asList("Left arm", "Right arm", "Gripping arm");
        command.setStudyName(expectedStudyName);
        command.setArms(false);
        for (String armName : expectedArmNames) {
            command.getArmNames().add(armName);
        }

        Study actual = command.createStudy();
        assertNotNull("Study is null", actual);
        assertEquals(expectedStudyName, actual.getName());
        assertEquals("Should be no arms", 0, actual.getArms().size());
    }
}
