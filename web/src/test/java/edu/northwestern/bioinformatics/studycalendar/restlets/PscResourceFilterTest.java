package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.springframework.mail.MailSender;
import static org.easymock.EasyMock.expect;
import edu.northwestern.bioinformatics.studycalendar.utils.mail.MailMessageFactory;
import edu.northwestern.bioinformatics.studycalendar.utils.mail.ExceptionMailMessage;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Jalpa Patel
 */
public class PscResourceFilterTest extends RestletTestCase {
    private PscResourceFilter pscResourceFilter;
    private MailSender mailSender;
    private MailMessageFactory mailMessageFactory;
    private HttpServletRequest httpRequest;
    private MockRestlet next;

    public void setUp() throws Exception {
        super.setUp();
        super.setUp();
        mailSender = registerMockFor(MailSender.class);
        mailMessageFactory = registerMockFor(MailMessageFactory.class);

        pscResourceFilter = new PscResourceFilter();
        pscResourceFilter.setMailMessageFactory(mailMessageFactory);
        pscResourceFilter.setMailSender(mailSender);
        next = new MockRestlet();
        pscResourceFilter.setNext(next);
    }

    public void testMailWhenException() throws Exception {
        RuntimeException exception =  new RuntimeException("Uncaught exception");
        ExceptionMailMessage expectedMessage = new ExceptionMailMessage();
        expect(mailMessageFactory.createExceptionMailMessage(exception,httpRequest)).
                andReturn(expectedMessage);
        mailSender.send(expectedMessage);
        try {
            replayMocks();
            next.setException(exception);
            pscResourceFilter.doHandle(request,response);
            verifyMocks();
        } catch (RuntimeException re) {
            assertEquals("Same exception should not thrown", exception, re);
        }
    }

    public void testNoMailWhenNoException() throws Exception {
        replayMocks();
        pscResourceFilter.doHandle(request,response);
        verifyMocks();
    }

}
