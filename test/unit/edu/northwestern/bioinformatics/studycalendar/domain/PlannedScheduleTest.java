package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static org.easymock.classextension.EasyMock.*;

/**
 * @author Rhett Sutphin
 */
public class PlannedScheduleTest extends StudyCalendarTestCase {
    private PlannedSchedule schedule;

    protected void setUp() throws Exception {
        super.setUp();
        schedule = new PlannedSchedule();
    }

    public void testAddArm() throws Exception {
        Arm arm = new Arm();
        schedule.addArm(arm);
        assertEquals("Wrong number of arms", 1, schedule.getArms().size());
        assertSame("Wrong arm present", arm, schedule.getArms().get(0));
        assertSame("Bidirectional relationship not maintained", schedule, arm.getPlannedSchedule());
    }

    public void testLength() throws Exception {
        Arm a1 = registerMockFor(Arm.class);
        expect(a1.getLengthInDays()).andReturn(45);
        a1.setPlannedSchedule(schedule);

        Arm a2 = registerMockFor(Arm.class);
        expect(a2.getLengthInDays()).andReturn(13);
        a2.setPlannedSchedule(schedule);

        replayMocks();

        schedule.addArm(a1);
        schedule.addArm(a2);
        assertEquals(45, schedule.getLengthInDays());
        verifyMocks();
    }
}
