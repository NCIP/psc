package edu.northwestern.bioinformatics.studycalendar.tools.configuration;

import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.restlet.util.Template;

import java.util.List;
import java.util.Map;

import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperties;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperty;
import gov.nih.nci.cabig.ctms.tools.configuration.DatabaseBackedConfiguration;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationEntry;

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


    public static final ConfigurationProperty<Template>
        PATIENT_PAGE_URL = PROPERTIES.add(new TemplateConfigurationProperty("patientPageUrl"));
    public static final ConfigurationProperty<String>
        CTMS_NAME= PROPERTIES.add(new ConfigurationProperty.Text("ctmsName"));
    public static final ConfigurationProperty<String>
        BASE_CTMS_URL = PROPERTIES.add(new ConfigurationProperty.Text("ctmsUrl"));


    public static final ConfigurationProperty<Template>
        STUDY_PAGE_URL = PROPERTIES.add(new TemplateConfigurationProperty("studyPageUrl"));
    

    ////// PSC-SPECIFIC LOGIC

    public boolean getExternalAppsConfigured() {
        return get(CAAERS_BASE_URL) != null || get(LABVIEWER_BASE_URL) != null || get(PATIENT_PAGE_URL) != null;
    }

    public boolean getCtmsConfigured() {
        return get(CTMS_NAME) != null && get(BASE_CTMS_URL) != null;
    }

    public boolean getStudyPageUrlConfigured() {
        return get(STUDY_PAGE_URL) != null;
    }

    public ConfigurationProperties getProperties() {
        return PROPERTIES;
    }

    protected Class<? extends ConfigurationEntry> getConfigurationEntryClass() {
        return PscConfigurationEntry.class;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public Map<String, Object> getMap() {
        return super.getMap();
    }
}
