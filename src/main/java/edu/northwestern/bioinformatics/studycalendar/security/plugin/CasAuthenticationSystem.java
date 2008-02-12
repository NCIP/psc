package edu.northwestern.bioinformatics.studycalendar.security.plugin;

import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperties;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.providers.AuthenticationProvider;
import org.acegisecurity.ui.AuthenticationEntryPoint;
import org.acegisecurity.ui.cas.CasProcessingFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;

import edu.northwestern.bioinformatics.studycalendar.tools.spring.StringXmlApplicationContext;
import edu.northwestern.bioinformatics.studycalendar.tools.spring.SpringBeanConfigurationTools;

import java.util.Properties;
import java.util.Map;
import java.util.Collection;
import java.util.Arrays;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class CasAuthenticationSystem extends AbstractAuthenticationSystem {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final ConfigurationProperties PROPERTIES
        = new ConfigurationProperties(
            new ClassPathResource("cas-details.properties", CasAuthenticationSystem.class));
    public static final ConfigurationProperty<String> SERVICE_URL
        = PROPERTIES.add(new ConfigurationProperty.Text("cas.serviceUrl"));
    public static final ConfigurationProperty<String> TRUST_STORE
        = PROPERTIES.add(new ConfigurationProperty.Text("cas.trustStore"));
    public static final ConfigurationProperty<String> APPLICATION_URL
        = PROPERTIES.add(new ConfigurationProperty.Text(PSC_URL_CONFIGURATION_PROPERTY_NAME));

    private static final String CAS_FILTER_PATH = "/j_acegi_cas_security_check";

    private ApplicationContext casContext;

    public ConfigurationProperties configurationProperties() {
        return PROPERTIES;
    }

    @Override
    protected Collection<ConfigurationProperty<?>> requiredConfigurationProperties() {
        return Arrays.asList((ConfigurationProperty<?>) SERVICE_URL, APPLICATION_URL);
    }

    @Override
    protected void initBeforeCreate() {
        String xml = createContextXml();
        log.debug("Creating casConfiguration context\n{}", xml);
        StringXmlApplicationContext configParametersContext
            = new StringXmlApplicationContext(xml, getApplicationContext());
        configParametersContext.refresh();
        casContext = new ClassPathXmlApplicationContext(
            new String[] { "cas-authentication-beans.xml" }, getClass(), configParametersContext);
    }

    private String createContextXml() {
        Properties template = new Properties();
        nullSafeSetProperty(template, "cas.server.trustStore",   getConfiguration().get(TRUST_STORE));
        nullSafeSetProperty(template, "cas.server.url.base",     getConfiguration().get(SERVICE_URL));
        nullSafeSetProperty(template, "cas.server.url.validate",
            urlJoin(getConfiguration().get(SERVICE_URL), "proxyValidate"));
        nullSafeSetProperty(template, "cas.local.filterPath",    urlJoin("", CAS_FILTER_PATH));
        nullSafeSetProperty(template, "cas.local.url",
            urlJoin(getConfiguration().get(APPLICATION_URL), CAS_FILTER_PATH));
        nullSafeSetProperty(template, "psc.defaultTarget",       DEFAULT_TARGET_PATH);

        StringBuilder propString = new StringBuilder();
        for (Map.Entry<Object, Object> entry : template.entrySet()) {
            propString.append(String.format("  <prop key=\"%s\">%s</prop>\n", entry.getKey(), entry.getValue()));
        }
        return String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<beans xmlns=\"http://www.springframework.org/schema/beans\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "       xsi:schemaLocation=\"" +
            "http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.0.xsd\"\n" +
            "       >\n<bean id=\"casConfiguration\" class=\"%s\">\n<property name=\"properties\">\n<props>\n%s</props>\n</property>\n</bean>\n</beans>",
            PropertiesFactoryBean.class.getName(), propString);
    }

    private void nullSafeSetProperty(Properties template, String key, String value) {
        template.setProperty(key, value == null ? "" : value);
    }

    private String urlJoin(String left, String right) {
        StringBuilder sb = new StringBuilder(left);
        if (left.endsWith("/") && right.startsWith("/")) {
            sb.deleteCharAt(sb.length() - 1);
        } else if (!left.endsWith("/") && !right.startsWith("/")) {
            sb.append('/');
        }
        sb.append(right);
        return sb.toString();
    }

    @Override
    protected AuthenticationManager createAuthenticationManager() {
        return AuthenticationSystemTools.createProviderManager(getApplicationContext(),
            (AuthenticationProvider) casContext.getBean("casAuthenticationProvider"));
    }

    @Override
    protected AuthenticationEntryPoint createEntryPoint() {
        return (AuthenticationEntryPoint) casContext.getBean("casEntryPoint");
    }

    @Override
    protected Filter createFilter() {
        // filter needs a reference to the authentication manager, so it can't go in the XML
        CasProcessingFilter filter = new CasProcessingFilter();
        filter.setAuthenticationManager(authenticationManager());
        filter.setDefaultTargetUrl(DEFAULT_TARGET_PATH);
        filter.setFilterProcessesUrl(CAS_FILTER_PATH);
        filter.setAuthenticationFailureUrl("/public/loginFailed");
        return SpringBeanConfigurationTools.prepareBean(casContext, filter);
    }
}
