package edu.northwestern.bioinformatics.studycalendar.utils.mail;

import static edu.northwestern.bioinformatics.studycalendar.tools.configuration.Configuration.MAIL_EXCEPTIONS_TO;
import static edu.northwestern.bioinformatics.studycalendar.tools.configuration.Configuration.MAIL_REPLY_TO;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import gov.nih.nci.cabig.ctms.tools.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ServletContextAware;
import org.apache.commons.lang.StringUtils;

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
        List<String> to = configuration.get(MAIL_EXCEPTIONS_TO);
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

    public ScheduleNotificationMailMessage createScheduleNotificationMailMessage(String toAddress,final StudySubjectAssignment studySubjectAssignment) {
        if (toAddress == null || StringUtils.isEmpty(toAddress)) {
            log.error("to address is null or empty. can not send email for new schedules. ");
            return null;
        } else {
            ScheduleNotificationMailMessage message = configureMessage(new ScheduleNotificationMailMessage());
            message.setTo(toAddress);
            message.setStudySubjectAssignment(studySubjectAssignment);
            return message;
        }
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
}
