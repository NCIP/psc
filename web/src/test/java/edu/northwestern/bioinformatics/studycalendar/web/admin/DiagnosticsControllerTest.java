package edu.northwestern.bioinformatics.studycalendar.web.admin;

import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import edu.northwestern.bioinformatics.studycalendar.web.WebTestCase;
import edu.northwestern.bioinformatics.studycalendar.configuration.Configuration;
import edu.northwestern.bioinformatics.studycalendar.utils.mail.StudyCalendarJavaMailSender;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import java.util.Collection;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.SYSTEM_ADMINISTRATOR;

/**
 * @author Saurabh Agrawal
 * @crated Sep 25, 2008
 */
public class DiagnosticsControllerTest extends ControllerTestCase {

    private DiagnosticsController diagnosticsController;
    private Configuration configuration

            = (Configuration) getDeployedApplicationContext().getBean("configuration");

    private StudyCalendarJavaMailSender mailSender = (StudyCalendarJavaMailSender) getDeployedApplicationContext().getBean("mailSender");

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        diagnosticsController = new DiagnosticsController();
        diagnosticsController.setConfiguration(configuration);
        diagnosticsController.setMailSender(mailSender);

    }

    public void testAuthorizedRoles() {
        Collection<ResourceAuthorization> actualAuthorizations = diagnosticsController.authorizations(null, null);
        assertRolesAllowed(actualAuthorizations, SYSTEM_ADMINISTRATOR);
    }

    public void testInvokeSmokeTestService() throws Exception {
        request.setMethod("GET");
        diagnosticsController.handleRequest(request, response);
    }
//
//    public void testSupportedMethods() throws Exception {
//        assertEquals("GET Must be supported", 1, diagnosticsController.getSupportedMethods().length);
//        assertEquals("Only GET is supported","GET", diagnosticsController.getSupportedMethods()[0]);
//    }
}
