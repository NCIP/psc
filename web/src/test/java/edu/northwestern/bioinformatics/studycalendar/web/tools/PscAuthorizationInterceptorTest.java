package edu.northwestern.bioinformatics.studycalendar.web.tools;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.SecurityContextHolderTestHelper;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.LegacyModeSwitch;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.web.WebTestCase;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.easymock.EasyMock.*;

/**
 * @author Rhett Sutphin
 */
@SuppressWarnings({ "unchecked" })
public class PscAuthorizationInterceptorTest extends WebTestCase {
    private PscAuthorizationInterceptor interceptor;
    private PscAuthorizedHandler controller;
    private LegacyModeSwitch legacyModeSwitch;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        controller = registerMockFor(PscAuthorizedHandler.class);
        legacyModeSwitch = new LegacyModeSwitch(false);

        interceptor = new PscAuthorizationInterceptor();
        interceptor.setLegacyModeSwitch(legacyModeSwitch);
        interceptor.setApplicationSecurityManager(applicationSecurityManager);

        SecurityContextHolderTestHelper.setSecurityContext(
            AuthorizationObjectFactory.createPscUser("alice", PscRole.STUDY_TEAM_ADMINISTRATOR), "");
    }

    public void testPreHandleReturnsTrueWhenUserAuthorized() throws Exception {
        expectControllerRequiresRole(PscRole.STUDY_TEAM_ADMINISTRATOR);
        replayMocks();

        assertTrue("Should continue", interceptor.preHandle(request, response, controller));
        verifyMocks();
    }

    public void testPreHandleReturnsFalseWhenUserNotAuthorized() throws Exception {
        expectControllerRequiresRole(PscRole.DATA_READER);
        replayMocks();

        assertFalse("Should not continue", interceptor.preHandle(request, response, controller));
        verifyMocks();
    }

    public void testPreHandleReturnsFalseWhenNoUser() throws Exception {
        applicationSecurityManager.removeUserSession();
        replayMocks();

        assertFalse("Should not continue", interceptor.preHandle(request, response, controller));
        verifyMocks();

        assertEquals("Wrong response status", 403, response.getStatus());
    }

    public void testPreHandleReturnsTrueInLegacyMode() throws Exception {
        legacyModeSwitch.setOn(true);
        replayMocks();

        assertTrue("Should continue", interceptor.preHandle(request, response, controller));
        verifyMocks();
    }

    public void testNonPscAuthorizedHandlersAreBlocked() throws Exception {
        replayMocks();

        assertFalse("Should not continue", interceptor.preHandle(request, response, new Object()));
        verifyMocks();

        assertEquals("Wrong response status", 403, response.getStatus());
    }

    public void testPreHandleDoesNotCareAboutNonAuthorizedHandlersInLegacyMode() throws Exception {
        legacyModeSwitch.setOn(true);
        replayMocks();

        assertTrue("Should continue", interceptor.preHandle(request, response, new Object()));
        verifyMocks();
    }

    public void testAuthorizationsReceivesCorrectParameters() throws Exception {
        request.setMethod("POST");
        Map<String, String[]> expectedParameters = Collections.singletonMap("foo", new String[] { "quux" });
        request.addParameters(expectedParameters);

        expect(controller.authorizations("POST", expectedParameters)).andReturn(
            Collections.singleton(ResourceAuthorization.create(PscRole.USER_ADMINISTRATOR)));
        replayMocks();

        assertFalse("Should not have continued", interceptor.preHandle(request, response, controller));
        verifyMocks();
    }

    private void expectControllerRequiresRole(PscRole expectedRole) throws Exception {
        expect(controller.authorizations((String) notNull(), (Map<String, String[]>) notNull())).
            andReturn(Arrays.asList(ResourceAuthorization.create(expectedRole)));
    }
}
