package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Method;
import org.restlet.resource.Resource;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Rhett Sutphin
 */
public abstract class ResourceTestCase<R extends Resource> extends StudyCalendarTestCase {
    private R resource;
    protected Request request;
    protected Response response;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        request = new Request();
        response = new Response(request);
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
}
