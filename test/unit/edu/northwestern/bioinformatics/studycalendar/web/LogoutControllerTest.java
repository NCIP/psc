package edu.northwestern.bioinformatics.studycalendar.web;

import org.easymock.classextension.EasyMock;

import static org.easymock.classextension.EasyMock.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import gov.nih.nci.security.AuthenticationManager;
import gov.nih.nci.security.exceptions.CSException;
import edu.northwestern.bioinformatics.studycalendar.web.LogoutController;
import edu.northwestern.bioinformatics.studycalendar.web.LoginController;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;

/**
 * @author Jaron Sampson
 */
public class LogoutControllerTest extends ControllerTestCase {

    private LogoutController controller;

    protected void setUp() throws Exception {
        super.setUp();
        controller = new LogoutController();        
    }

    public void testLogout() throws Exception {
    	ApplicationSecurityManager.setUser(request, "jack");
    	controller.handleRequest(request, response);
    	assertNull(ApplicationSecurityManager.getUser(request));
    }

    public void testRedirect() throws Exception {
    	ApplicationSecurityManager.setUser(request, "jill");
    	ModelAndView mv = new ModelAndView();
    	mv = controller.handleRequest(request, response);
    	assertEquals("Did not redirect to the login page", "redirectToLogin", mv.getViewName()); 
    }

}
