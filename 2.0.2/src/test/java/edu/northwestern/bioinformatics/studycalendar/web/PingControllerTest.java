package edu.northwestern.bioinformatics.studycalendar.web;

/**
 * @author John Dzak
 */
public class PingControllerTest extends WebTestCase {
    private PingController controller;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        controller = new PingController();
    }

    public void testPing() throws Exception {
        assertNotNull("Request return should be not null", controller.handleRequest(request, response));
    }
}
