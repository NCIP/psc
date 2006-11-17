package edu.northwestern.bioinformatics.studycalendar.web;

import static edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.LoginCheckInterceptor.REQUESTED_URL_ATTRIBUTE;
import edu.northwestern.bioinformatics.studycalendar.dao.LoginAuditDao;
import gov.nih.nci.security.exceptions.CSException;
import static org.easymock.classextension.EasyMock.expect;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Padmaja Vedula
 * @author Rhett Sutphin
 */
public class LoginControllerTest extends ControllerTestCase {
    private LoginController controller;
    private LoginCommand command;

    protected void setUp() throws Exception {
        super.setUp();

        command = registerMockFor(LoginCommand.class, LoginCommand.class.getMethod("login", String.class));
        controller = new LoginController() {
        	   
            protected Object formBackingObject(HttpServletRequest request) throws Exception {
                return command;
            }
        };
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

        assertTrue("failed indicator missing", (Boolean) actual.getModel().get("failed"));
        assertEquals("login", actual.getViewName());
    }

    public void testAjaxGet() throws Exception {
        request.setParameter("ajax", " ");
        request.setMethod("GET");
        ModelAndView view = controller.handleRequest(request, response);
        assertEquals("relogin", view.getViewName());
    }
    
    public void testAjaxPostBadCredentials() throws Exception {
        request.setParameter("ajax", " ");
        expectLogin(false);

        replayMocks();
        ModelAndView actual = controller.handleRequest(request, response);
        verifyMocks();

        assertTrue("failed indicator missing", (Boolean) actual.getModel().get("failed"));
        assertEquals("relogin", actual.getViewName());
    }

    private void expectLogin(boolean success) throws CSException {
    	expect(command.login(request.getRemoteAddr())).andReturn(success);
    }

}
