package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.restlet.Filter;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.springframework.mail.MailSender;
import edu.northwestern.bioinformatics.studycalendar.utils.mail.MailMessageFactory;
import edu.northwestern.bioinformatics.studycalendar.utils.mail.ExceptionMailMessage;

import com.noelios.restlet.ext.servlet.ServletCall;

/**
 * @author Jalpa Patel
 */
public class ErrorMailingRestletFilter extends Filter {
    private MailSender mailSender;
    private MailMessageFactory mailMessageFactory;

    @Override
    protected int doHandle(Request request, Response response) {
        try {
            return super.doHandle(request,response);
        } catch (RuntimeException exception) {
            ExceptionMailMessage mailMessage = mailMessageFactory.createExceptionMailMessage(exception, ServletCall.getRequest(request));
            if (mailMessage != null) mailSender.send(mailMessage);
            throw exception;
        }
    }

    public void setMailSender(MailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void setMailMessageFactory(MailMessageFactory mailMessageFactory) {
        this.mailMessageFactory = mailMessageFactory;
    }
}
