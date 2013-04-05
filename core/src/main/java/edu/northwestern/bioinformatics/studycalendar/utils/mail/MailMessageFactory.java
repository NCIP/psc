/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.utils.mail;

import edu.northwestern.bioinformatics.studycalendar.domain.Notification;
import static edu.northwestern.bioinformatics.studycalendar.configuration.Configuration.MAIL_EXCEPTIONS_TO;
import static edu.northwestern.bioinformatics.studycalendar.configuration.Configuration.MAIL_REPLY_TO;

import edu.northwestern.bioinformatics.studycalendar.tools.spring.ApplicationPathAware;
import gov.nih.nci.cabig.ctms.tools.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class MailMessageFactory implements ServletContextAware, ApplicationPathAware {
    private static Logger log = LoggerFactory.getLogger(MailMessageFactory.class);

    private freemarker.template.Configuration freemarkerConfiguration;
    private ServletContext servletContext;
    private Configuration configuration;
    private String applicationPath;

    ////// FACTORY

    public ExceptionMailMessage createExceptionMailMessage(Throwable exception, HttpServletRequest request) {
        List<String> to = configuration.get(MAIL_EXCEPTIONS_TO);
        if (to == null) {
            log.error("Uncaught exception encountered, but report e-mail messages not configured.  To turn them on, set at least one address for the mailExceptionsTo property.", exception);
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

    public NotificationMailMessage createNotificationMailMessage(String subjectHeader, String message) {
        NotificationMailMessage mailMessage = new NotificationMailMessage();
        mailMessage.setSubjectHeader(subjectHeader);
        mailMessage.setMessage(message);
        mailMessage.setApplicationPath(applicationPath);
        return configureMessage(mailMessage);
    }

    private <T extends StudyCalendarMailMessage> T configureMessage(T message) {
        message.setFreemarkerConfiguration(freemarkerConfiguration);
        message.setConfiguration(configuration);
        message.setReplyTo(configuration.get(MAIL_REPLY_TO));
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

    public void setApplicationPath(String applicationPath) {
        this.applicationPath = applicationPath;
    }
}
