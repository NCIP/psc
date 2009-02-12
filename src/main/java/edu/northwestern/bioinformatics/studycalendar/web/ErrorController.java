package edu.northwestern.bioinformatics.studycalendar.web;

import edu.nwu.bioinformatics.commons.ThrowableUtils;

import edu.northwestern.bioinformatics.studycalendar.utils.mail.MailMessageFactory;
import edu.northwestern.bioinformatics.studycalendar.utils.mail.ExceptionMailMessage;
import org.springframework.mail.MailSender;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class ErrorController extends AbstractController {
    private MailSender mailSender;
    private MailMessageFactory mailMessageFactory;

    protected ModelAndView handleRequestInternal(
        HttpServletRequest request, HttpServletResponse response
    ) throws Exception {
        Throwable exception = (Throwable) request.getAttribute("javax.servlet.error.exception");
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        String message = (String) request.getAttribute("javax.servlet.error.message");

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("stackTrace", exception == null ? null : ThrowableUtils.createStackTraceHtml(exception));
        model.put("statusCode", statusCode);
        model.put("statusName", getName(statusCode));
        model.put("message", message);

        if (exception != null) {
            ExceptionMailMessage mailMessage = mailMessageFactory.createExceptionMailMessage(exception, request);
            if (mailMessage != null) mailSender.send(mailMessage);
            model.put("notified", mailMessage != null);
        }

        return new ModelAndView("error", model);
    }

    private String getName(Integer statusCode) {
        switch (statusCode) {
            case HttpServletResponse.SC_INTERNAL_SERVER_ERROR: return "Internal server error";
            case HttpServletResponse.SC_NOT_FOUND: return "Not found";
            case HttpServletResponse.SC_FORBIDDEN: return "Forbidden";
            case HttpServletResponse.SC_BAD_REQUEST: return "Bad request";
            default: return "HTTP " + statusCode;
        }
    }

    ////// CONFIGURATION

    public void setMailSender(MailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void setMailMessageFactory(MailMessageFactory mailMessageFactory) {
        this.mailMessageFactory = mailMessageFactory;
    }
}
