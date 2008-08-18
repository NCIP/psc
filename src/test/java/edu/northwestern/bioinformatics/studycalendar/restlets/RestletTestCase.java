package edu.northwestern.bioinformatics.studycalendar.restlets;

import com.noelios.restlet.ext.servlet.ServletCall;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

/**
 * @author Rhett Sutphin
 */
public abstract class RestletTestCase extends StudyCalendarTestCase {
    protected static final String ROOT_URI = "http://trials.etc.edu/studycalendar/api/v1";
    protected static final String BASE_URI = ROOT_URI + "/the/path/to/the/resource";

    protected Request request;
    protected Response response;
    protected ServletCall servletCall;
    protected Context context;
    protected Application application;

    protected MockHttpServletRequest servletRequest;
    protected MockHttpServletResponse servletResponse;
    protected MockHttpSession servletSession;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
//        servletRequest = new MockHttpServletRequest();
//        servletResponse = new MockHttpServletResponse();
//        servletSession = new MockHttpSession(); // not associated with servletRequest by default

        context = new Context();
        application = new Application(context);
        Application.setCurrent(application);

//        servletCall = new ServletCall(null, servletRequest, servletResponse);

        request = new Request();
        request.setRootRef(new Reference(ROOT_URI));
        request.setResourceRef(new Reference(new Reference(BASE_URI), ""));
        response = new Response(request);
    }

    protected void assertResponseStatus(Status status) {
        assertEquals("Wrong response status", status, response.getStatus());
    }
}
