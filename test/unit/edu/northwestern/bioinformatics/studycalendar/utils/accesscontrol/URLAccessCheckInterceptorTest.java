package edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol;

import edu.northwestern.bioinformatics.studycalendar.web.WebTestCase;
import gov.nih.nci.security.AuthorizationManager;
import static org.easymock.classextension.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
public class URLAccessCheckInterceptorTest extends WebTestCase {
    private static final String USER = "jimbo";

    private URLAccessCheckInterceptor interceptor;
    private AuthorizationManager authorizationManager;

    protected void setUp() throws Exception {
        super.setUp();
        ApplicationSecurityManager.setUser(request, USER);
        authorizationManager = registerMockFor(AuthorizationManager.class);

        interceptor = new URLAccessCheckInterceptor();
        interceptor.setAuthorizationManager(authorizationManager);
    }

    public void testAccessCheckForSingleSubdir() throws Exception {
        request.setContextPath("/studycalendar");
        request.setRequestURI("/studycalendar/pages/template");

        expect(authorizationManager.checkPermission(USER, "/pages/template", "ACCESS")).andReturn(true);

        replayMocks();
        interceptor.preHandle(request, response, null);
        verifyMocks();
    }

    public void testAccessCheckForRootDeployment() throws Exception {
        request.setContextPath("");
        request.setRequestURI("/pages/template");

        expect(authorizationManager.checkPermission(USER, "/pages/template", "ACCESS")).andReturn(true);

        replayMocks();
        interceptor.preHandle(request, response, null);
        verifyMocks();
    }

    public void testAccessCheckForNullCp() throws Exception {
        request.setContextPath(null);
        request.setRequestURI("/pages/template");

        expect(authorizationManager.checkPermission(USER, "/pages/template", "ACCESS")).andReturn(true);

        replayMocks();
        interceptor.preHandle(request, response, null);
        verifyMocks();
    }
}
