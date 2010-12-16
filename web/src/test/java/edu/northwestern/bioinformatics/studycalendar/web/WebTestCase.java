package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.service.TestingTemplateService;
import org.springframework.mock.web.MockServletContext;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;

/**
 * @author Rhett Sutphin
 */
public abstract class WebTestCase extends StudyCalendarTestCase {
    protected MockHttpServletRequest request;
    protected MockHttpServletResponse response;
    protected MockServletContext servletContext;
    protected MockHttpSession session;
    protected ControllerTools controllerTools;
    protected TemplateService templateService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        servletContext = new MockServletContext();
        session = new MockHttpSession(servletContext);
        request = new MockHttpServletRequest(servletContext);
        request.setMethod("POST");
        request.setSession(session);
        response = new MockHttpServletResponse();
        templateService = new TestingTemplateService();
        controllerTools = new ControllerTools();
        controllerTools.setTemplateService(templateService);
    }

    public static String findWebappSrcDirectory() {
        File dir = new File("src/main/webapp");
        if (dir.exists()) {
            return dir.getPath();
        }
        dir = new File("web", dir.toString());
        if (dir.exists()) {
            return dir.getPath();
        }
        throw new IllegalStateException("Could not find webapp path");
    }
}
