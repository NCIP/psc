package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;

import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class ScheduledArmTest extends StudyCalendarTestCase {
    private ScheduledCalendar scheduledCalendar = new ScheduledCalendar();
    private ScheduledArm scheduledArm = new ScheduledArm();

    protected void setUp() throws Exception {
        super.setUp();
        scheduledCalendar.addArm(scheduledArm);
    }

    public void testNameWithMultiArmEpoch() throws Exception {
        Epoch multi = createEpoch("Treatment", "A", "B", "C");
        scheduledArm.setArm(multi.getArms().get(1));
        assertName("Treatment: B");
    }

    public void testNameWithZeroArmEpoch() throws Exception {
        Epoch single = createEpoch("Screening");
        scheduledArm.setArm(single.getArms().get(0));
        assertName("Screening");
    }

    public void testNameWhenRepeated() throws Exception {
        Epoch epoch = createEpoch("Treatment", "A", "B", "C");
        scheduledCalendar.getScheduledArms().clear();
        scheduledCalendar.addArm(createScheduledArm(epoch.getArms().get(1)));
        scheduledCalendar.addArm(createScheduledArm(epoch.getArms().get(0)));
        scheduledCalendar.addArm(scheduledArm);
        scheduledArm.setArm(epoch.getArms().get(1));

        List<ScheduledArm> arms = scheduledCalendar.getScheduledArms();
        assertName("Treatment: B (1)", arms.get(0));
        assertName("Treatment: A", arms.get(1));
        assertName("Treatment: B (2)", arms.get(2));
    }

    private void assertName(String expectedName) {
        assertName(expectedName, this.scheduledArm);
    }

    private static void assertName(String expectedName, ScheduledArm scheduledArm) {
        assertEquals("Wrong name", expectedName, scheduledArm.getName());
    }
}
