package edu.northwestern.bioinformatics.studycalendar.web;

import junit.framework.TestCase;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;

/**
 * @author Rhett Sutphin
 */
public abstract class ControllerTestCase extends TestCase {
    protected MockHttpServletRequest request;
    protected MockHttpServletResponse response;
    protected MockServletContext servletContext;

    protected void setUp() throws Exception {
        super.setUp();
        servletContext = new MockServletContext();
        request = new MockHttpServletRequest(servletContext);
        request.setMethod("POST");
        response = new MockHttpServletResponse();
    }
}
