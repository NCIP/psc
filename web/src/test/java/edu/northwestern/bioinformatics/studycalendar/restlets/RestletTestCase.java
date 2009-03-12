package edu.northwestern.bioinformatics.studycalendar.restlets;

import com.noelios.restlet.ext.servlet.ServletCall;
import com.noelios.restlet.http.HttpRequest;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;

/**
 * @author Rhett Sutphin
 */
public abstract class RestletTestCase extends StudyCalendarTestCase {
    protected static final String HOST = "trials.etc.edu";
    protected static final String ROOT_URI = "http://" + HOST + "/studycalendar/api/v1";
    protected static final String BASE_URI = ROOT_URI + "/the/path/to/the/resource";

    protected Request request;
    protected Response response;
    protected ServletCall servletCall;
    protected Context context;
    protected Application application;
    
    protected MockHttpServletRequest servletRequest;
    protected MockHttpServletResponse servletResponse;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        servletRequest = new MockHttpServletRequest();
        servletRequest.addHeader("Host", HOST);
        servletResponse = new MockHttpServletResponse();

        context = new Context();
        application = new Application(context);
        Application.setCurrent(application);
        Server server = new Server(context, Protocol.HTTP, application);
        servletCall = new ServletCall(server, servletRequest, servletResponse);

        request = new Request();
        request.setRootRef(new Reference(ROOT_URI));
        request.setResourceRef(new Reference(new Reference(BASE_URI), ""));
        response = new Response(request);
    }

    protected void useHttpRequest() {
        request = new HttpRequest(context, servletCall);
    }

    protected void assertResponseStatus(Status status) {
        assertEquals("Wrong response status", status, response.getStatus());
    }

    @SuppressWarnings({ "deprecation" }) // URLEncoder.encode is deprecated for stupid reasons
    protected void expectRequestEntityFormAttribute(String name, String value) throws IOException {
        StringBuilder form = new StringBuilder();
        if (request.getEntity() != null) {
            form.append(request.getEntity().getText()).append('&');
        }
        form.append(URLEncoder.encode(name)).
            append('=').append(URLEncoder.encode(value));
        request.setEntity(form.toString(), MediaType.APPLICATION_WWW_FORM);
    }
}
