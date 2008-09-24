package edu.northwestern.bioinformatics.studycalendar.web.admin;

import edu.northwestern.bioinformatics.studycalendar.tools.configuration.Configuration;
import org.restlet.util.Template;
import org.apache.commons.lang.StringUtils;
//import gov.nih.nci.ccts.grid.smoketest.client.SmokeTestServiceClient;

/**
 * @author Saurabh Agrawal
 */
public class DiagnosticsCommand extends ConfigurationCommand {
    private String smtpException;

    public DiagnosticsCommand(Configuration configuration) {
        super(configuration);
    }




//    public boolean checkIfGridServicesIsConnecting() {
//        boolean testResult = false;
//        try {
//            SmokeTestServiceClient client = new SmokeTestServiceClient("");
//            client.ping();
//            testResult = true;
//        } catch (Exception e) {
//
//            //this.setSmokeTestServiceException(e);
//
//        }
//        return testResult;
//
//    }

    public void setSmtpException(String smtpException) {
        this.smtpException = smtpException;
    }

    public String getSmtpException() {
        return smtpException;
    }
}
