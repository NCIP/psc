package edu.northwestern.bioinformatics.studycalendar.security.plugin.websso;

import edu.northwestern.bioinformatics.studycalendar.security.plugin.cas.CasAuthenticationSystem;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperties;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperty;
import gov.nih.nci.cabig.ctms.tools.configuration.DefaultConfigurationProperties;
import gov.nih.nci.cabig.ctms.tools.configuration.DefaultConfigurationProperty;
import org.springframework.core.io.ClassPathResource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * @author Kruttik Aggarwal
 * @author Rhett Sutphin
 */
public class WebSSOAuthenticationSystem extends CasAuthenticationSystem {
    private static final DefaultConfigurationProperties WEBSSO_PROPERTIES =
        new DefaultConfigurationProperties(new ClassPathResource(
            absoluteClasspathResourceNameFor("websso-details.properties" , WebSSOAuthenticationSystem.class),
            WebSSOAuthenticationSystem.class));
    public static final ConfigurationProperty<String> HOST_KEY =
        WEBSSO_PROPERTIES.add(new DefaultConfigurationProperty.Text("websso.hostkey"));
    public static final ConfigurationProperty<String> HOST_CERT =
        WEBSSO_PROPERTIES.add(new DefaultConfigurationProperty.Text("websso.hostcert"));

    @Override
    public String name() {
        return "caGrid WebSSO";
    }

    @Override
    public String behaviorDescription() {
        return "delegates authentication to a caGrid WebSSO server (use this option for CCTS)";
    }

    @Override
    protected String[] applicationContextResourceNames() {
        List<String> names = new ArrayList<String>();
        names.add("websso-authentication-beans.xml");
        names.addAll(Arrays.asList(super.applicationContextResourceNames()));
        return names.toArray(new String[names.size()]);
    }

    @Override
    protected String getPopulatorBeanName() {
        return "cctsAuthoritiesPopulator";
    }

    @Override
    protected String getTicketValidatorBeanName() {
        return "cctsCasProxyTicketValidator";
    }

    @Override
    protected Properties createContextProperties() {
        Properties properties = super.createContextProperties();
        nullSafeSetProperty(properties, "websso.hostkey.path",   getConfiguration().get(HOST_KEY));
        nullSafeSetProperty(properties, "websso.hostcert.path",  getConfiguration().get(HOST_CERT));
        return properties;
    }

    @Override
    public ConfigurationProperties configurationProperties() {
        return DefaultConfigurationProperties.union(super.configurationProperties(), WEBSSO_PROPERTIES);
    }
}
