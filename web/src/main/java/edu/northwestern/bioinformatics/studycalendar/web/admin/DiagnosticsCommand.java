/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.admin;

import edu.northwestern.bioinformatics.studycalendar.configuration.Configuration;
//import gov.nih.nci.ccts.grid.smoketest.client.SmokeTestServiceClient;

/**
 * @author Saurabh Agrawal
 */
public class DiagnosticsCommand extends ConfigurationCommand {
    private String smtpException;
    private String smokeTestServiceException;

    public DiagnosticsCommand(Configuration configuration) {
        super(configuration);
    }


    public BindableConfiguration getConfiguration() {
        return getConf();
    }

    public void setSmtpException(String smtpException) {
        this.smtpException = smtpException;
    }

    public String getSmtpException() {
        return smtpException;
    }

    public String getSmokeTestServiceException() {
        return smokeTestServiceException;
    }

    public void setSmokeTestServiceException(String smokeTestServiceException) {
        this.smokeTestServiceException = smokeTestServiceException;
    }
}
