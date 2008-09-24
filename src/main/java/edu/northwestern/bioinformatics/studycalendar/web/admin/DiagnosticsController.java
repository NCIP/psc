package edu.northwestern.bioinformatics.studycalendar.web.admin;

import edu.northwestern.bioinformatics.studycalendar.tools.configuration.Configuration;
import edu.northwestern.bioinformatics.studycalendar.utils.mail.ExceptionMailMessage;
import edu.northwestern.bioinformatics.studycalendar.utils.mail.MailMessageFactory;
import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.mail.MailSender;

import javax.servlet.http.HttpServletRequest;

public class DiagnosticsController extends PscSimpleFormController {

    private Configuration configuration;
    protected final Log log = LogFactory.getLog(getClass());

    private MailSender mailSender;
    private MailMessageFactory mailMessageFactory;


    public DiagnosticsController() {
        setCommandClass(DiagnosticsCommand.class);
        setFormView("admin/diagnostics");
    }

    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        DiagnosticsCommand diagnosticsCommand = new DiagnosticsCommand(configuration);
        testSmtp(diagnosticsCommand, request);
        return diagnosticsCommand;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }


    private void testSmtp(DiagnosticsCommand diagnosticsCommand, HttpServletRequest request) {
        try {
            ExceptionMailMessage exceptionMailMessage = mailMessageFactory.createExceptionMailMessage(new Exception("Testing the grid service configuration.."), request);
            mailSender.send(exceptionMailMessage);
        } catch (Exception e) {
            log.error(" Error in sending email , please check the confiuration " + e);
            diagnosticsCommand.setSmtpException(e.getMessage());
        }
    }

    @Required
    public void setMailSender(MailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Required
    public void setMailMessageFactory(MailMessageFactory mailMessageFactory) {
        this.mailMessageFactory = mailMessageFactory;
    }
}
