package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.utils.mail.ExceptionMailMessage;
import edu.northwestern.bioinformatics.studycalendar.utils.mail.MailMessageFactory;
import edu.nwu.bioinformatics.commons.ThrowableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final Logger log = LoggerFactory.getLogger(getClass());

    private MailSender mailSender;
    private MailMessageFactory mailMessageFactory;

    @Override
    protected ModelAndView handleRequestInternal(
        HttpServletRequest request, HttpServletResponse response
    ) throws Exception {
        Throwable exception = (Throwable) request.getAttribute("javax.servlet.error.exception");
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        String message = (String) request.getAttribute("javax.servlet.error.message");
        String requestPath = (String) request.getAttribute("javax.servlet.error.request_uri");

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("stackTrace", exception == null ? null : ThrowableUtils.createStackTraceHtml(exception));
        model.put("statusCode", statusCode);
        model.put("statusName", getName(statusCode));
        model.put("message", message);
        model.put("redirectToSwitchboardAfter", redirectToSwitchboardAfter(statusCode, requestPath));

        if (exception != null) {
            log.error("Uncaught exception in web stack", exception);
            ExceptionMailMessage mailMessage = mailMessageFactory.createExceptionMailMessage(exception, request);
            if (mailMessage != null) {
                try {
                    mailSender.send(mailMessage);
                    model.put("notified", true);
                } catch (Exception e) {
                    log.error("Sending exception e-mail message failed: {}", e.getMessage());
                    log.debug("Message-sending error detail:", e);
                }
            }
            if (!model.containsKey("notified")) model.put("notified", false);
        }

        return new ModelAndView("error", model);
    }

    private Integer redirectToSwitchboardAfter(Integer statusCode, String requestPath) {
        if (statusCode == HttpServletResponse.SC_FORBIDDEN && !requestPath.endsWith("/pages/switchboard")) {
            return 20;
        } else {
            return null;
        }
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
