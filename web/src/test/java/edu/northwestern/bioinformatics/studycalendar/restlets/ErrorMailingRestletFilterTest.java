/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.utils.mail.ExceptionMailMessage;
import edu.northwestern.bioinformatics.studycalendar.utils.mail.MailMessageFactory;
import org.springframework.mail.MailSender;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

/**
 * @author Jalpa Patel
 */
public class ErrorMailingRestletFilterTest extends RestletTestCase {
    private ErrorMailingRestletFilter filter;
    private MailSender mailSender;
    private MailMessageFactory mailMessageFactory;
    private MockRestlet next;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        useHttpRequest();
        
        mailSender = registerMockFor(MailSender.class);
        mailMessageFactory = registerMockFor(MailMessageFactory.class);
        next = new MockRestlet();

        filter = new ErrorMailingRestletFilter();
        filter.setMailMessageFactory(mailMessageFactory);
        filter.setMailSender(mailSender);
        filter.setNext(next);
    }

    public void testMailWhenException() throws Exception {
        RuntimeException exception =  new RuntimeException("Uncaught exception");
        ExceptionMailMessage expectedMessage = new ExceptionMailMessage();
        expect(mailMessageFactory.createExceptionMailMessage(exception, servletRequest)).
                andReturn(expectedMessage);
        mailSender.send(expectedMessage);

        try {
            replayMocks();
            next.setException(exception);
            filter.doHandle(request, response);
            verifyMocks();
        } catch (RuntimeException re) {
            assertEquals("Same exception was not thrown", exception, re);
        }
    }

    public void testOriginalExceptionIsThrownWhenMailingThrowsItsOwnException() throws Exception {
        RuntimeException exception =  new RuntimeException("Uncaught exception");
        ExceptionMailMessage expectedMessage = new ExceptionMailMessage();
        expect(mailMessageFactory.createExceptionMailMessage(exception, servletRequest)).
                andReturn(expectedMessage);
        mailSender.send(expectedMessage);
        expectLastCall().andThrow(new IllegalStateException("Not sending today"));

        try {
            replayMocks();
            next.setException(exception);
            filter.doHandle(request, response);
            verifyMocks();
        } catch (RuntimeException re) {
            assertEquals("Same exception was not thrown", exception, re);
        }
    }

    public void testNoMailWhenNoException() throws Exception {
        replayMocks();
        filter.doHandle(request, response);
        verifyMocks();
    }
}
