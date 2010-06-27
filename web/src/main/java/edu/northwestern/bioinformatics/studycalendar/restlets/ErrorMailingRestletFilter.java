package edu.northwestern.bioinformatics.studycalendar.restlets;

import com.noelios.restlet.ext.servlet.ServletCall;
import edu.northwestern.bioinformatics.studycalendar.utils.mail.ExceptionMailMessage;
import edu.northwestern.bioinformatics.studycalendar.utils.mail.MailMessageFactory;
import org.restlet.Filter;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailSender;

/**
 * @author Jalpa Patel
 */
public class ErrorMailingRestletFilter extends Filter {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private MailSender mailSender;
    private MailMessageFactory mailMessageFactory;

    @Override
    protected int doHandle(Request request, Response response) {
        try {
            return super.doHandle(request,response);
        } catch (RuntimeException exception) {
            sendMailIfPossible(request, exception);
            throw exception;
        }
    }

    private void sendMailIfPossible(Request request, RuntimeException exception) {
        ExceptionMailMessage mailMessage = mailMessageFactory.createExceptionMailMessage(exception, ServletCall.getRequest(request));
        if (mailMessage != null) {
            try {
                mailSender.send(mailMessage);
            } catch (Exception e) {
                log.error("Sending exception e-mail message failed: {}", e.getMessage());
                log.debug("Message-sending error detail:", e);
            }
        }
    }

    public void setMailSender(MailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void setMailMessageFactory(MailMessageFactory mailMessageFactory) {
        this.mailMessageFactory = mailMessageFactory;
    }
}
