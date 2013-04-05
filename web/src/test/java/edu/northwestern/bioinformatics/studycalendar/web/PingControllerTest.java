/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

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
