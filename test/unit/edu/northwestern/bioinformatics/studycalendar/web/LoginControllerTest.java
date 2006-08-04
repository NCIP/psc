package edu.northwestern.bioinformatics.studycalendar.web;

import org.easymock.classextension.EasyMock;

//import static org.easymock.EasyMock.expectLastCall;
//import static org.easymock.EasyMock.notNull;
import static org.easymock.classextension.EasyMock.*;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Rhett Sutphin
 */
public class LoginControllerTest extends ControllerTestCase {
    private LoginController controller;
   

    protected void setUp() throws Exception {
        super.setUp();
        controller = new LoginController();
    }

    public void testModelAndView() throws Exception {
        
    }
    public void testViewOnGet() throws Exception {
        request.setMethod("GET");
        ModelAndView view = controller.handleRequest(request, response);
        assertEquals("login", view.getViewName());
    }

    public void testViewOnSubmit() throws Exception {
        String userIdVal = "StudyCal1";
        String passwordVal = "StudyCal1";
        request.addParameter("userId", userIdVal);
        request.addParameter("password", passwordVal);
        ModelAndView mv = controller.handleRequest(request, response);
        assertEquals("login", mv.getViewName());
    }
 
}
