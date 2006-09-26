package edu.northwestern.bioinformatics.studycalendar.web;

import org.easymock.classextension.EasyMock;

//import static org.easymock.EasyMock.expectLastCall;
//import static org.easymock.EasyMock.notNull;
import static org.easymock.classextension.EasyMock.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import gov.nih.nci.security.AuthenticationManager;
import gov.nih.nci.security.exceptions.CSException;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.LoginCheckInterceptor;
import static edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.LoginCheckInterceptor.*;

/**
 * @author Padmaja Vedula
 */
public class LoginControllerTest extends ControllerTestCase {
    private static final String PASSWORD = "wonderland";
    private static final String USERNAME = "alice";

    private LoginController controller;
    private AuthenticationManager authenticationManager;

    protected void setUp() throws Exception {
        super.setUp();

        authenticationManager = registerMockFor(AuthenticationManager.class);

        controller = new LoginController();
        controller.setAuthenticationManager(authenticationManager);
    }

    public void testViewOnGet() throws Exception {
        request.setMethod("GET");
        ModelAndView view = controller.handleRequest(request, response);
        assertEquals("login", view.getViewName());
    }

    public void testPostWithValidCredentialsAndNoRequestedUrl() throws Exception {
        expectLogin(true);
        session.setAttribute(REQUESTED_URL_ATTRIBUTE, null);

        replayMocks();
        ModelAndView actual = controller.handleRequest(request, response);
        verifyMocks();

        assertTrue("Default view not a redirect", actual.getView() instanceof RedirectView);
        RedirectView actualView = (RedirectView) actual.getView();
        assertEquals("/pages/studyList", actualView.getUrl());
    }

    public void testPostWithValidCredentialsAndRequestedUrl() throws Exception {
        expectLogin(true);
        String expectedRequestedUrl = "http://go/here/now";
        session.setAttribute(REQUESTED_URL_ATTRIBUTE, expectedRequestedUrl);

        replayMocks();
        ModelAndView actual = controller.handleRequest(request, response);
        verifyMocks();

        assertTrue("View not a redirect", actual.getView() instanceof RedirectView);
        RedirectView actualView = (RedirectView) actual.getView();
        assertEquals(expectedRequestedUrl, actualView.getUrl());
        assertNull(session.getAttribute(REQUESTED_URL_ATTRIBUTE));
    }

    public void testPostWithBadCredentials() throws Exception {
        expectLogin(false);

        replayMocks();
        ModelAndView actual = controller.handleRequest(request, response);
        verifyMocks();

        // TODO: this behavior should change (there should be some notification that the creds weren't accepted)

        assertEquals("login", actual.getViewName());
        //TODO: I am not sure how to get this assertion to pass using the AbstractFormController - jaron
        //assertTrue("Command missing", actual.getModel().containsKey("command"));
    }

    private void expectLogin(boolean success) throws CSException {
        request.setParameter("userId", USERNAME);
        request.setParameter("password", PASSWORD);
        expect(authenticationManager.login(USERNAME, PASSWORD)).andReturn(success);
    }

}
