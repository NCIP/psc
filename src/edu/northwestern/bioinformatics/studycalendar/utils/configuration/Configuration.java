package edu.northwestern.bioinformatics.studycalendar.utils.configuration;

import org.springframework.core.io.ClassPathResource;

import java.util.List;

import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperties;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperty;
import gov.nih.nci.cabig.ctms.tools.configuration.DatabaseBackedConfiguration;

/**
 * @author Rhett Sutphin
 */
public class Configuration extends DatabaseBackedConfiguration {
    private static final ConfigurationProperties PROPERTIES
        = new ConfigurationProperties(new ClassPathResource("details.properties", Configuration.class));

    public static final ConfigurationProperty<String>
        DEPLOYMENT_NAME = PROPERTIES.add(new ConfigurationProperty.Text("deploymentName"));
    public static final ConfigurationProperty<String>
        MAIL_REPLY_TO = PROPERTIES.add(new ConfigurationProperty.Text("replyTo"));
    public static final ConfigurationProperty<List<String>>
        MAIL_EXCEPTIONS_TO = PROPERTIES.add(new ConfigurationProperty.Csv("mailExceptionsTo"));
    public static final ConfigurationProperty<String>
        SMTP_HOST = PROPERTIES.add(new ConfigurationProperty.Text("smtpHost"));
    public static final ConfigurationProperty<Integer>
        SMTP_PORT = PROPERTIES.add(new ConfigurationProperty.Int("smtpPort"));
    public static final ConfigurationProperty<Boolean>
        SHOW_FULL_EXCEPTIONS = PROPERTIES.add(new ConfigurationProperty.Bool("showFullExceptions"));
    public static final ConfigurationProperty<Boolean>
        SHOW_DEBUG_INFORMATION = PROPERTIES.add(new ConfigurationProperty.Bool("showDebugInformation"));

    public static final ConfigurationProperty<String>
        CAAERS_BASE_URL = PROPERTIES.add(new ConfigurationProperty.Text("caAERSBaseUrl"));
    public static final ConfigurationProperty<String>
        LABVIEWER_BASE_URL = PROPERTIES.add(new ConfigurationProperty.Text("labViewerBaseUrl"));

    ////// PSC-SPECIFIC LOGIC

    public boolean getExternalAppsConfigured() {
        return get(CAAERS_BASE_URL) != null || get(LABVIEWER_BASE_URL) != null;
    }

    @Override
    public ConfigurationProperties getProperties() {
        return PROPERTIES;
    }

}
