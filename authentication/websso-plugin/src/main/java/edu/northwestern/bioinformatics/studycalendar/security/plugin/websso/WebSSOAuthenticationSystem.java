/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.security.plugin.websso;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.cas.CasAuthenticationSystem;
import gov.nih.nci.cabig.ctms.tools.configuration.Configuration;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperties;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperty;
import gov.nih.nci.cabig.ctms.tools.configuration.DefaultConfigurationProperties;
import gov.nih.nci.cabig.ctms.tools.configuration.DefaultConfigurationProperty;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
        return "delegates authentication to a caGrid WebSSO server (use this option for CCTS); requires additional container configuration";
    }

    @Override
    protected Collection<ConfigurationProperty<?>> requiredConfigurationProperties() {
        Collection<ConfigurationProperty<?>> reqd = new ArrayList<ConfigurationProperty<?>>(super.requiredConfigurationProperties());
        reqd.add(HOST_KEY);
        reqd.add(HOST_CERT);
        return reqd;
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
    protected String getDirectAuthenticationProviderBeanName() {
        return "webssoDirectAuthenticationProvider";
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

    @Override
    public void validate(Configuration config) throws StudyCalendarValidationException {
        super.validate(config);

        validateIsReadableFilename(config, HOST_KEY);
        validateIsReadableFilename(config, HOST_CERT);
    }

    private void validateIsReadableFilename(Configuration config, ConfigurationProperty<String> property) {
        String value = config.get(property);
        File f = new File(value);
        if (!f.canRead()) {
            throw new StudyCalendarValidationException("%s '%s' is not readable", property.getName(), value);
        }
    }
}
