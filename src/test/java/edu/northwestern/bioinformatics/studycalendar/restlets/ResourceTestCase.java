package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.xml.CapturingStudyCalendarXmlFactoryStub;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlCollectionSerializer;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.data.Reference;
import org.restlet.resource.Resource;
import org.restlet.resource.ReaderRepresentation;
import static org.easymock.EasyMock.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.Reader;
import java.io.IOException;

/**
 * @author Rhett Sutphin
 */
public abstract class ResourceTestCase<R extends Resource> extends StudyCalendarTestCase {
    protected static final String MOCK_XML = "<foo></foo>";
    protected static final String ROOT_URI = "http://trials.etc.edu/studycalendar/api/v1";
    protected static final String BASE_URI = ROOT_URI + "/the/path/to/the/resource";

    private R resource;
    protected Request request;
    protected Response response;

    protected StudyCalendarXmlCollectionSerializer xmlSerializer;
    protected CapturingStudyCalendarXmlFactoryStub xmlSerializerStub;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        request = new Request();
        request.setRootRef(new Reference(ROOT_URI));
        request.setResourceRef(new Reference(new Reference(BASE_URI), ""));
        response = new Response(request);
        xmlSerializer = registerMockFor(StudyCalendarXmlCollectionSerializer.class);
        xmlSerializerStub = new CapturingStudyCalendarXmlFactoryStub();
    }

    protected abstract R createResource();

    protected R getResource() {
        if (resource == null) resource = createResource();
        return resource;
    }

    private void doInit() {
        getResource().init(null, request, response);
    }

    protected void doGet() {
        replayMocks();
        request.setMethod(Method.GET);
        doInit();
        getResource().handleGet();
        verifyMocks();
    }

    protected void doPut() {
        replayMocks();
        request.setMethod(Method.PUT);
        doInit();
        getResource().handlePut();
        verifyMocks();
    }

    protected void doPost() {
        replayMocks();
        request.setMethod(Method.POST);
        doInit();
        getResource().handlePost();
        verifyMocks();
    }

    protected void doDelete() {
        replayMocks();
        request.setMethod(Method.DELETE);
        doInit();
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
        doInit();
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

    protected void assertResponseStatus(Status status) {
        assertEquals("Response should be " + status, status, response.getStatus());
    }

    protected void expectReadXmlFromRequestAs(Object expectedRead) throws Exception {
        final Reader reader = registerMockFor(Reader.class);
        request.setEntity(new ReaderRepresentation(reader, MediaType.TEXT_XML));

        expect(xmlSerializer.readDocument(reader)).andReturn(expectedRead);
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
    
    protected static ApplicationContext getApiServletApplicationContext() {
        ApplicationContext parent = getDeployedApplicationContext();
        FileSystemXmlApplicationContext context
            = new FileSystemXmlApplicationContext("src/main/webapp/WEB-INF/restful-api-servlet.xml");
        context.setParent(parent);
        context.refresh();
        return context;
    }
}
