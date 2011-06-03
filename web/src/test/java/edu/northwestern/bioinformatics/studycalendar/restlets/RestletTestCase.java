package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.web.WebTestCase;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Server;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.engine.http.HttpRequest;
import org.restlet.engine.http.HttpResponse;
import org.restlet.engine.http.header.HeaderConstants;
import org.restlet.ext.servlet.internal.ServletCall;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public abstract class RestletTestCase extends StudyCalendarTestCase {
    protected static final String HOST = "trials.etc.edu";
    protected static final String ROOT_URI = "http://" + HOST + "/studycalendar/api/v1";
    protected static final String BASE_URI = ROOT_URI + "/the/path/to/the/resource";

    protected HttpRequest request;
    protected HttpResponse response;
    protected ServletCall servletCall;
    protected Context context;
    protected Application application;
    
    protected MockHttpServletRequest servletRequest;
    protected MockHttpServletResponse servletResponse;
    protected MockServletContext servletContext;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        servletContext = new MockServletContext(
            WebTestCase.findWebappSrcDirectory(), new FileSystemResourceLoader());
        servletRequest = new MockHttpServletRequest(servletContext);
        servletRequest.addHeader("Host", HOST);
        servletRequest.setContent(new byte[0]);
        servletResponse = new MockHttpServletResponse();

        context = new Context();
        application = new Application(context);
        Application.setCurrent(application);
        Server server = new Server(context, Protocol.HTTP, application);
        servletCall = new ServletCall(server, servletRequest, servletResponse);

        request = new HttpRequest(context, servletCall);
        request.setRootRef(new Reference(ROOT_URI));
        request.setResourceRef(new Reference(new Reference(BASE_URI), ""));
        request.getAttributes().put(HeaderConstants.ATTRIBUTE_HEADERS, new Form());
        response = new HttpResponse(servletCall, request);

        // Most of the tests expect XML to be the most-preferred variant.
        // This default seems to have changed in Restlet 2.0.
        setAcceptedMediaTypes(MediaType.TEXT_XML);
    }

    protected void useHttpRequest() {
        request = new HttpRequest(context, servletCall);
    }

    protected void assertResponseStatus(Status status) {
        assertEquals("Wrong response status", status, response.getStatus());
    }

    protected void assertResponseStatus(Status expectedStatus, String expectedMessage) {
        assertEquals("Wrong response status", expectedStatus, response.getStatus());
        assertEquals("Wrong response message", expectedMessage, response.getStatus().getDescription());
    }

    @SuppressWarnings({ "deprecation" }) // URLEncoder.encode is deprecated for stupid reasons
    protected void expectRequestEntityFormAttribute(String name, String value) throws IOException {
        StringBuilder form = new StringBuilder();
        if (request.getEntity() != null && request.getEntity().getSize() > 0) {
            form.append(request.getEntity().getText()).append('&');
        }
        form.append(URLEncoder.encode(name)).
            append('=').append(URLEncoder.encode(value));
        request.setEntity(form.toString(), MediaType.APPLICATION_WWW_FORM);
    }

    protected void setAcceptedMediaTypes(MediaType... expectedTypes) {
        List<Preference<MediaType>> prefs = new ArrayList<Preference<MediaType>>(expectedTypes.length);
        for (MediaType type : expectedTypes) {
            prefs.add(new Preference<MediaType>(type));
        }
        request.getClientInfo().setAcceptedMediaTypes(prefs);
    }
}
