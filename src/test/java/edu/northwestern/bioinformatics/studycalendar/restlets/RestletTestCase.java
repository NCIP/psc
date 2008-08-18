package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import org.restlet.Application;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

/**
 * @author Rhett Sutphin
 */
public abstract class RestletTestCase extends StudyCalendarTestCase {
    protected static final String ROOT_URI = "http://trials.etc.edu/studycalendar/api/v1";
    protected static final String BASE_URI = ROOT_URI + "/the/path/to/the/resource";

    protected Request request;
    protected Response response;
    protected Application application;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        application = new Application();
        Application.setCurrent(application);
        request = new Request();
        request.setRootRef(new Reference(ROOT_URI));
        request.setResourceRef(new Reference(new Reference(BASE_URI), ""));
        response = new Response(request);
    }

    protected void assertResponseStatus(Status status) {
        assertEquals("Wrong response status", status, response.getStatus());
    }
}
