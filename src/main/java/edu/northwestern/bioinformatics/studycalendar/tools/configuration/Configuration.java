package edu.northwestern.bioinformatics.studycalendar.tools.configuration;

import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.util.List;
import java.util.Map;

import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperties;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperty;
import gov.nih.nci.cabig.ctms.tools.configuration.DatabaseBackedConfiguration;

/**
 * @author Rhett Sutphin
 */
public class Configuration extends DatabaseBackedConfiguration {
    public static final ConfigurationProperties PROPERTIES
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
        SHOW_DEBUG_INFORMATION = PROPERTIES.add(new ConfigurationProperty.Bool("showDebugInformation"));

    public static final ConfigurationProperty<String>
        CAAERS_BASE_URL = PROPERTIES.add(new ConfigurationProperty.Text("caAERSBaseUrl"));
    public static final ConfigurationProperty<String>
        LABVIEWER_BASE_URL = PROPERTIES.add(new ConfigurationProperty.Text("labViewerBaseUrl"));

    ////// PSC-SPECIFIC LOGIC

    public boolean getExternalAppsConfigured() {
        return get(CAAERS_BASE_URL) != null || get(LABVIEWER_BASE_URL) != null;
    }

    public ConfigurationProperties getProperties() {
        return PROPERTIES;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public Map<String, Object> getMap() {
        return super.getMap();
    }
}
