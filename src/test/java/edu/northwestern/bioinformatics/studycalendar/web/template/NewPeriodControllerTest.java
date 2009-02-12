package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;

/**
 * @author Rhett Sutphin
 */
public class NewPeriodControllerTest extends ControllerTestCase {
    private NewPeriodController controller;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        controller = new NewPeriodController();
    }

    public void testCommandForRegularNewPeriod() throws Exception {
        Object actual = controller.formBackingObject(request);
        assertTrue(actual instanceof NewPeriodCommand);
    }

    public void testCommandForCopyNewPeriod() throws Exception {
        request.setParameter("selectedPeriod", "7");
        Object actual = controller.formBackingObject(request);
        assertTrue(actual instanceof CopyPeriodCommand);
    }
}
