package edu.northwestern.bioinformatics.studycalendar.web.admin;

import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import edu.northwestern.bioinformatics.studycalendar.tools.configuration.MockConfiguration;
import edu.northwestern.bioinformatics.studycalendar.tools.configuration.Configuration;

import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class ConfigurationControllerTest extends ControllerTestCase {
    private ConfigurationController controller;
    private MockConfiguration configuration;

    protected void setUp() throws Exception {
        super.setUp();
        configuration = new MockConfiguration();

        controller = new ConfigurationController();
        controller.setConfiguration(configuration);
    }

    public void testBindListProperty() throws Exception {
        request.setParameter("conf[mailExceptionsTo].value", "a, b, d");

        controller.handleRequest(request, response);

        List<String> bound = configuration.get(Configuration.MAIL_EXCEPTIONS_TO);
        assertNotNull(bound);
        assertEquals(3, bound.size());
        assertEquals("a", bound.get(0));
        assertEquals("b", bound.get(1));
        assertEquals("d", bound.get(2));
    }
    
    public void testBindIntProperty() throws Exception {
        request.setParameter("conf[smtpPort].value", "123");
        controller.handleRequest(request, response);
        assertEquals(123, (int) configuration.get(Configuration.SMTP_PORT));
    }
}
