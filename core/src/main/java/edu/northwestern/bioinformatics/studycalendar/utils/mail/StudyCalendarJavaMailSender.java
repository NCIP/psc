/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.utils.mail;

import org.springframework.mail.javamail.JavaMailSenderImpl;
import edu.northwestern.bioinformatics.studycalendar.configuration.Configuration;

/**
 * @author Rhett Sutphin
 */
public class StudyCalendarJavaMailSender extends JavaMailSenderImpl {
    private Configuration configuration;

    public String getHost() {
        return configuration.get(Configuration.SMTP_HOST);
    }

    public void setHost(String host) {
        throw unsupported("host");
    }

    public int getPort() {
        return configuration.get(Configuration.SMTP_PORT);
    }

    public void setPort(int port) {
        throw unsupported("port");
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    private UnsupportedOperationException unsupported(String prop) {
        return new UnsupportedOperationException(prop + " is set through the application configuration");
    }
}
