package edu.northwestern.bioinformatics.studycalendar.utils.mail;

import edu.northwestern.bioinformatics.studycalendar.utils.configuration.Configuration;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class MailMessageFactory implements ServletContextAware {
//    private static Log log = LogFactory.getLog(MailMessageFactory.class);
    private static Logger log = LoggerFactory.getLogger(MailMessageFactory.class);

    private freemarker.template.Configuration freemarkerConfiguration;
    private ServletContext servletContext;
    private Configuration configuration;

    ////// FACTORY

    public ExceptionMailMessage createExceptionMailMessage(Throwable exception, HttpServletRequest request) {
        List<String> to = configuration.get(Configuration.MAIL_EXCEPTIONS_TO);
        if (to == null) {
            log.error("Uncaught exception encountered, but report e-mail messages not configured.  To turn them on, set at least one address for the mailExceptionsTo property.");
            return null;
        } else {
            ExceptionMailMessage message = configureMessage(new ExceptionMailMessage());
            message.setTo(to.toArray(new String[to.size()]));
            message.setUncaughtException(exception);
            message.setRequest(request);
            message.setServletContext(servletContext);
            return message;
        }
    }

    private <T extends StudyCalendarMailMessage> T configureMessage(T message) {
        message.setFreemarkerConfiguration(freemarkerConfiguration);
        message.setConfiguration(configuration);
        message.setReplyTo(configuration.get(Configuration.MAIL_REPLY_TO));
        message.onInitialization();
        return message;
    }

    ////// CONFIGURATION

    public void setFreemarkerConfiguration(freemarker.template.Configuration freemarkerConfiguration) {
        this.freemarkerConfiguration = freemarkerConfiguration;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
