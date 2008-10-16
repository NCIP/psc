package edu.northwestern.bioinformatics.studycalendar.web.admin;

import edu.northwestern.bioinformatics.studycalendar.web.WebTestCase;
import edu.northwestern.bioinformatics.studycalendar.tools.configuration.Configuration;
import edu.northwestern.bioinformatics.studycalendar.utils.mail.StudyCalendarJavaMailSender;
import org.springframework.mail.MailSender;

/**
 * @author Saurabh Agrawal
 * @crated Sep 25, 2008
 */
public class DiagnosticsControllerTest extends WebTestCase {

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
