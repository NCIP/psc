package edu.northwestern.bioinformatics.studycalendar.web.admin;

import edu.northwestern.bioinformatics.studycalendar.configuration.Configuration;
import static edu.northwestern.bioinformatics.studycalendar.configuration.Configuration.*;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.SYSTEM_ADMINISTRATOR;

import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.utils.mail.StudyCalendarJavaMailSender;
import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.mail.SimpleMailMessage;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@AccessControl(roles = Role.SYSTEM_ADMINISTRATOR)
public class DiagnosticsController extends PscSimpleFormController implements PscAuthorizedHandler {

    private Configuration configuration;
    private final Logger log = LoggerFactory.getLogger(getClass());

    private StudyCalendarJavaMailSender mailSender;


    public DiagnosticsController() {
        setCommandClass(DiagnosticsCommand.class);
        setFormView("admin/diagnostics");
    }

    public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
        return ResourceAuthorization.createCollection(SYSTEM_ADMINISTRATOR);
    }    

    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        DiagnosticsCommand diagnosticsCommand = new DiagnosticsCommand(configuration);
        testSmtp(diagnosticsCommand, request);
        checkIfGridServicesIsConnecting(diagnosticsCommand);
        return diagnosticsCommand;
    }

    /*
     * TODO: push the smoke service invocation into an OSGi plugin so that it can use the OSGi-layer grid libraries
     */

    public void checkIfGridServicesIsConnecting(DiagnosticsCommand diagnosticsCommand) {
        diagnosticsCommand.setSmokeTestServiceException("Grid service smoke test temporarily disabled");
        /*
        try {
            Template template = configuration.get(Configuration.SMOKE_SERVICE_BASE_URL);
            if (template != null) {
                gov.nih.nci.ccts.grid.smoketest.client.SmokeTestServiceClient client
                    = new gov.nih.nci.ccts.grid.smoketest.client.SmokeTestServiceClient(template.getPattern());
                client.ping();
            }
        } catch (Exception e) {
            log.error("Error connecting to SmoteTestService.", e);
            diagnosticsCommand.setSmokeTestServiceException(e.getMessage());

        }
        */
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
