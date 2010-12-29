package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.utils.mail.ExceptionMailMessage;
import edu.northwestern.bioinformatics.studycalendar.utils.mail.MailMessageFactory;
import org.easymock.EasyMock;
import org.springframework.mail.MailSender;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.support.StaticWebApplicationContext;
import org.springframework.web.servlet.ModelAndView;

import static org.easymock.classextension.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
public class ErrorControllerTest extends ControllerTestCase {
    private ErrorController controller;

    private MailSender mailSender;
    private MailMessageFactory mailMessageFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mailSender = registerMockFor(MailSender.class);
        mailMessageFactory = registerMockFor(MailMessageFactory.class);

        controller = new ErrorController();
        controller.setMailSender(mailSender);
        controller.setMailMessageFactory(mailMessageFactory);
        ConfigurableWebApplicationContext context = new StaticWebApplicationContext();
        context.setServletContext(servletContext);
        controller.setApplicationContext(context);

        request.setAttribute("javax.servlet.error.status_code", 500);
        request.setAttribute("javax.servlet.error.message", "Internal Server Error");
    }

    public void testMailSentOnException() throws Exception {
        Exception exception = new Exception("Whose bright idea?");
        ExceptionMailMessage expectedMessage = new ExceptionMailMessage();
        expect(mailMessageFactory.createExceptionMailMessage(exception, request)).
                andReturn(expectedMessage);
        mailSender.send(expectedMessage);
        replayMocks();

        request.setAttribute("javax.servlet.error.exception", exception);
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();
        assertTrue((Boolean) mv.getModel().get("notified"));
    }

    public void testNoExceptionThrownIfSenderThrowsException() throws Exception {
        Exception exception = new Exception("Whose bright idea?");
        ExceptionMailMessage expectedMessage = new ExceptionMailMessage();
        expect(mailMessageFactory.createExceptionMailMessage(exception, request)).
                andReturn(expectedMessage);
        mailSender.send(expectedMessage);
        EasyMock.expectLastCall().andThrow(new IllegalStateException("I don't care to send"));
        replayMocks();

        request.setAttribute("javax.servlet.error.exception", exception);
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();
        assertFalse((Boolean) mv.getModel().get("notified"));
    }

    public void testNoMailWhenNoException() throws Exception {
        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();
        assertNull(mv.getModel().get("errorNumber"));
    }
}
