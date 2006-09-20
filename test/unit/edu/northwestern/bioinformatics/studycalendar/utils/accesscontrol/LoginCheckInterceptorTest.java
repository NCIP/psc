package edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol;

import static edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.LoginCheckInterceptor.REQUESTED_URL_ATTRIBUTE;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.ModelAndViewDefiningException;

/**
 * @author Rhett Sutphin
 */
public class LoginCheckInterceptorTest extends ControllerTestCase {
    private LoginCheckInterceptor interceptor = new LoginCheckInterceptor();

    public void testPreHandleWhenLoggedIn() throws Exception {
        ApplicationSecurityManager.setUser(request, "Authenticable");
        assertTrue("Should be fine if logged in", interceptor.preHandle(request, response, null));
    }

    public void testRedirectWhenNotLoggedIn() throws Exception {
        ApplicationSecurityManager.removeUserSession(request);

        try {
            interceptor.preHandle(request, response, null);
            fail("Exception not thrown");
        } catch (ModelAndViewDefiningException e) {
            ModelAndView actual = e.getModelAndView();
            assertEquals("redirectToLogin", actual.getViewName());
        }
    }

    public void testTargetUrlStored() throws Exception {
        request.setQueryString("id=9&name=nine");
        request.setScheme("http");
        request.setServerName("server");
        request.setServerPort(123);
        request.setRequestURI("/app/path/to/request");
        try {
            interceptor.preHandle(request, response, null);
            fail("Exception not thrown");
        } catch (Exception e) {
            // don't care
        }

        String actualRequestUrl = (String) session.getAttribute(REQUESTED_URL_ATTRIBUTE);
        assertNotNull("Attribute missing", actualRequestUrl);
        assertEquals("http://server:123/app/path/to/request?id=9&name=nine", actualRequestUrl);
    }
}
