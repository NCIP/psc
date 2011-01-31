package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.web.StudyCalendarTestWebApplicationContextBuilder;
import edu.northwestern.bioinformatics.studycalendar.xml.CapturingStudyCalendarXmlFactoryStub;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlCollectionSerializer;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ServerResource;
import org.springframework.context.ApplicationContext;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import static org.easymock.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
public abstract class ResourceTestCase<R extends ServerResource> extends RestletTestCase {
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
        getResource().init(context, request, response);
    }

    protected void doGet() {
        doRequest(Method.GET);
    }

    protected void doPut() {
        doRequest(Method.PUT);
    }

    protected void doPost() {
        doRequest(Method.POST);
    }

    protected void doDelete() {
        doRequest(Method.DELETE);
    }

    protected void doRequest(Method method) {
        replayMocks();
        request.setMethod(method);
        simulateFinderHandle();
        verifyMocks();
    }

    protected void simulateFinderHandle() {
        doInitOnly();
        if (response.getStatus().isSuccess()) {
            getResource().handle();
        }
    }

    protected void assertAllowedMethods(String... allowedMethods) throws Exception {
        doInitOnly();
        Set<Method> actual = getResource().getAllowedMethods();
        assertEquals("Wrong number of allowed methods: " + actual,
            allowedMethods.length, actual.size());
        for (String allowedMethod : allowedMethods) {
            assertTrue("Missing " + allowedMethod + " from " + actual,
                actual.contains(Method.valueOf(allowedMethod)));
        }
    }

    protected void expectReadXmlFromRequestAs(Object expectedRead) throws Exception {
        final InputStream in = registerMockFor(InputStream.class);
        // Restlet 2 doesn't ensure enum instance identity in media types when parsing responses
        request.setEntity(new InputRepresentation(in, new MediaType(MediaType.TEXT_XML.getName())));

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
    
    protected void assertClientErrorReason(String expected) {
        assertTrue("No client error reason set",
            request.getAttributes().containsKey(PscStatusService.CLIENT_ERROR_REASON_KEY));
        assertEquals("Wrong client error reason", expected,
            request.getAttributes().get(PscStatusService.CLIENT_ERROR_REASON_KEY));
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
