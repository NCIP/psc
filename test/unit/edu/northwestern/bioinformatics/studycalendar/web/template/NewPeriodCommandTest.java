package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;

/**
 * @author Rhett Sutphin
 */
public class NewPeriodCommandTest extends StudyCalendarTestCase {
    private NewPeriodCommand command;

    protected void setUp() throws Exception {
        super.setUp();
        command = new NewPeriodCommand();
    }
    
    public void testApply() throws Exception {
        Arm arm = new Arm();
        assertEquals(0, arm.getPeriods().size());

        command.setArm(arm);
        command.apply();

        assertEquals(1, arm.getPeriods().size());
        assertSame(command, arm.getPeriods().first());
    }
}
