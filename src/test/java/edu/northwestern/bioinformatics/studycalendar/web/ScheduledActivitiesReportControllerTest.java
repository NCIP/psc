package edu.northwestern.bioinformatics.studycalendar.web;

/**
 * @author John Dzak
 */
public class ScheduledActivitiesReportControllerTest extends ControllerTestCase {
    private ScheduledActivitiesReportController controller;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        controller = new ScheduledActivitiesReportController();
    }

    public void testCreateModel() throws Exception {
        assertNotNull("Model should contain modes", controller.createModel().get("modes"));
    }
}
