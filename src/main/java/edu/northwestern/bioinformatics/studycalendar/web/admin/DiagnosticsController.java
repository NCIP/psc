package edu.northwestern.bioinformatics.studycalendar.web.admin;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.beans.factory.annotation.Required;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.util.Template;

import javax.servlet.http.HttpServletRequest;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.InternetAddress;

import edu.northwestern.bioinformatics.studycalendar.tools.configuration.Configuration;
import static edu.northwestern.bioinformatics.studycalendar.tools.configuration.Configuration.MAIL_EXCEPTIONS_TO;
import static edu.northwestern.bioinformatics.studycalendar.tools.configuration.Configuration.MAIL_REPLY_TO;
import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
import edu.northwestern.bioinformatics.studycalendar.utils.mail.MailMessageFactory;
import edu.northwestern.bioinformatics.studycalendar.utils.mail.ExceptionMailMessage;
import edu.northwestern.bioinformatics.studycalendar.utils.mail.StudyCalendarMailMessage;
import edu.northwestern.bioinformatics.studycalendar.utils.mail.StudyCalendarJavaMailSender;
import gov.nih.nci.ccts.grid.smoketest.client.SmokeTestServiceClient;

import java.util.List;

public class DiagnosticsController extends PscSimpleFormController {

    private Configuration configuration;
    protected final Log log = LogFactory.getLog(getClass());

    private StudyCalendarJavaMailSender mailSender;


    public DiagnosticsController() {
        setCommandClass(DiagnosticsCommand.class);
        setFormView("admin/diagnostics");
    }

    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        DiagnosticsCommand diagnosticsCommand = new DiagnosticsCommand(configuration);
        testSmtp(diagnosticsCommand, request);
        checkIfGridServicesIsConnecting(diagnosticsCommand);
        return diagnosticsCommand;
    }


    public void checkIfGridServicesIsConnecting(DiagnosticsCommand diagnosticsCommand) {
        try {
            Template template = configuration.get(Configuration.SMOKE_SERVICE_BASE_URL);
            if (template != null) {
                SmokeTestServiceClient client = new SmokeTestServiceClient(template.getPattern());
                client.ping();
            }
        } catch (Exception e) {
            log.error("Error connecting to SmoteTestService.", e);
            diagnosticsCommand.setSmokeTestServiceException(e.getMessage());

        }

    }

    private void testSmtp(DiagnosticsCommand diagnosticsCommand, HttpServletRequest request) {
        try {
            String message = "Testing the email configuration..";

            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
            simpleMailMessage.setSubject(message);
            simpleMailMessage.setText("Email Configuration is correct.");

            simpleMailMessage.setReplyTo(configuration.get(MAIL_REPLY_TO));
            simpleMailMessage.setFrom(configuration.get(MAIL_REPLY_TO));
            List<String> to = configuration.get(MAIL_EXCEPTIONS_TO);
            simpleMailMessage.setTo(to.toArray(new String[to.size()]));


            mailSender.send(simpleMailMessage);
        } catch (Exception e) {
            log.error(" Error in sending email , please check the configuration " + e);
            diagnosticsCommand.setSmtpException(e.getMessage());
        }


    }

    @Required
    public void setMailSender(StudyCalendarJavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Required
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }


}
