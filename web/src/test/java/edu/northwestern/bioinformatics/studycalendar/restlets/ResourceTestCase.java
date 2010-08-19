package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.web.StudyCalendarTestWebApplicationContextBuilder;
import edu.northwestern.bioinformatics.studycalendar.xml.CapturingStudyCalendarXmlFactoryStub;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlCollectionSerializer;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.resource.InputRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.StringRepresentation;
import org.springframework.context.ApplicationContext;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.easymock.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
public abstract class ResourceTestCase<R extends Resource> extends RestletTestCase {
    protected static final String MOCK_XML = "<foo></foo>";
    protected static final Representation MOCK_XML_REP = new MockXmlRepresentation(MOCK_XML, MediaType.TEXT_XML);

    private R resource;

    protected StudyCalendarXmlCollectionSerializer xmlSerializer;
    protected CapturingStudyCalendarXmlFactoryStub xmlSerializerStub;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        xmlSerializer = registerMockFor(StudyCalendarXmlCollectionSerializer.class);
        xmlSerializerStub = new CapturingStudyCalendarXmlFactoryStub();
    }

    protected abstract R createResource();

    protected R getResource() {
        if (resource == null) {
            resource = createResource();
            resource.setRequest(request);
            resource.setResponse(response);
        }
        return resource;
    }

    protected void doInit() {
        replayMocks();
        doInitOnly();
        verifyMocks();
    }

    protected void doInitOnly() {
        getResource().init(null, request, response);
    }

    protected void doGet() {
        replayMocks();
        request.setMethod(Method.GET);
        doInitOnly();
        getResource().handleGet();
        verifyMocks();
    }

    protected void doPut() {
        replayMocks();
        request.setMethod(Method.PUT);
        doInitOnly();
        getResource().handlePut();
        verifyMocks();
    }

    protected void doPost() {
        replayMocks();
        request.setMethod(Method.POST);
        doInitOnly();
        getResource().handlePost();
        verifyMocks();
    }

    protected void doDelete() {
        replayMocks();
        request.setMethod(Method.DELETE);
        doInitOnly();
        getResource().handleDelete();
        verifyMocks();
    }

    private static final String[] ALL_METHODS = { "GET", "PUT", "POST", "DELETE" };

    protected void assertAllowedMethods(String... allowedMethods) throws Exception {
        List<String> disallowedMethods = new ArrayList<String>(Arrays.asList(ALL_METHODS));
        for (int i = 0; i < allowedMethods.length; i++) {
            allowedMethods[i] = allowedMethods[i].toUpperCase();
            disallowedMethods.remove(allowedMethods[i]);
        }
        doInitOnly();
        for (String method : allowedMethods) {
            assertTrue(method + " should be allowed", isMethodAllowed(method));
        }
        for (String method : disallowedMethods) {
            assertFalse(method + " should not be allowed", isMethodAllowed(method));
        }
    }

    private boolean isMethodAllowed(String method) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        StringBuilder checkerName = new StringBuilder(method.toLowerCase());
        checkerName.setCharAt(0, Character.toUpperCase(checkerName.charAt(0)));
        checkerName.insert(0, "allow");
        java.lang.reflect.Method checker = getResource().getClass().getMethod(checkerName.toString());
        Boolean result = (Boolean) checker.invoke(getResource());
        assertNotNull("Test execution failure", result);
        return result;
    }

    protected void expectReadXmlFromRequestAs(Object expectedRead) throws Exception {
        final InputStream in = registerMockFor(InputStream.class);
        request.setEntity(new InputRepresentation(in, MediaType.TEXT_XML));

        expect(xmlSerializer.readDocument(in)).andReturn(expectedRead);
    }

    @SuppressWarnings({ "unchecked" })
    protected void expectObjectXmlized(Object o) {
        expect(xmlSerializer.createDocumentString(o)).andReturn(MOCK_XML);
    }

    protected void assertResponseIsCreatedXml() throws IOException {
        assertEquals("Result is not right content type", MediaType.TEXT_XML, response.getEntity().getMediaType());
        String actualEntityBody = response.getEntity().getText();
        assertEquals("Wrong text", MOCK_XML, actualEntityBody);
    }

    protected void assertResponseIsStubbedXml() throws IOException {
        assertEquals("Result is not right content type", MediaType.TEXT_XML, response.getEntity().getMediaType());
        String actualEntityBody = response.getEntity().getText();
        assertEquals("Wrong text", CapturingStudyCalendarXmlFactoryStub.XML_STRING, actualEntityBody);
    }
    
    protected void assertEntityTextContains(String expected) throws IOException {
        Representation entity = response.getEntity();
        assertNotNull("No entity returned", entity);
        assertContains("Missing content from entity", entity.getText(), expected);
    }

    protected ApplicationContext getApiServletApplicationContext() {
        return StudyCalendarTestWebApplicationContextBuilder.
            createWebApplicationContextForServlet("restful-api", servletContext);
    }

    protected void setAccept(MediaType requestType) {
        request.getClientInfo().setAcceptedMediaTypes(Arrays.asList(new Preference<MediaType>(requestType)));
    }

    private static class MockXmlRepresentation extends StringRepresentation {
        private static final InputStream stream = new ByteArrayInputStream(MOCK_XML.getBytes());

        public MockXmlRepresentation(CharSequence text, MediaType mediaType) {
            super(text, mediaType);
        }

        @Override
        public InputStream getStream() throws IOException {
            return stream;
        }
    }
}
