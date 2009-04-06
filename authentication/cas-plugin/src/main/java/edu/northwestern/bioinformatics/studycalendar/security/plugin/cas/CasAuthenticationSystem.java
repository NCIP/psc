package edu.northwestern.bioinformatics.studycalendar.security.plugin.cas;

import edu.northwestern.bioinformatics.studycalendar.security.plugin.AbstractAuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystemTools;
import edu.northwestern.bioinformatics.studycalendar.tools.spring.SpringBeanConfigurationTools;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperties;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperty;
import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.providers.AuthenticationProvider;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.ui.AuthenticationEntryPoint;
import org.acegisecurity.ui.cas.CasProcessingFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;

import javax.servlet.Filter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * @author Rhett Sutphin
 */
public class CasAuthenticationSystem extends AbstractAuthenticationSystem {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final ConfigurationProperties PROPERTIES
        = new ConfigurationProperties(
            new ClassPathResource(absoluteClasspathResourceNameFor("cas-details.properties"), CasAuthenticationSystem.class));
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
    public String name() {
        return "CAS";
    }

    public String behaviorDescription() {
        return "delegates authentication decisions to an enterprise-wide CAS server";
    }

    @Override
    protected Collection<ConfigurationProperty<?>> requiredConfigurationProperties() {
        return Arrays.asList((ConfigurationProperty<?>) SERVICE_URL, APPLICATION_URL);
    }

    @Override
    protected void initBeforeCreate() {
        ApplicationContext configParametersContext
            = AuthenticationSystemTools.createApplicationContextWithPropertiesBean(
                getApplicationContext(), "casConfiguration", createContextProperties());
        casContext = new ClassPathXmlApplicationContext(
            applicationContextResourceNames().toArray(new String[0]),
            getClass(), configParametersContext);
    }

    protected List<String> applicationContextResourceNames() {
        return Arrays.asList(absoluteClasspathResourceNameFor("cas-authentication-beans.xml"));
    }

    private Properties createContextProperties() {
        Properties template = new Properties();
        nullSafeSetProperty(template, "cas.server.trustStore",   getConfiguration().get(TRUST_STORE));
        nullSafeSetProperty(template, "cas.server.url.base",     getConfiguration().get(SERVICE_URL));
        nullSafeSetProperty(template, "cas.server.url.validate",
            urlJoin(getConfiguration().get(SERVICE_URL), "proxyValidate"));
        nullSafeSetProperty(template, "cas.server.url.login",
            urlJoin(getConfiguration().get(SERVICE_URL), "login"));
        nullSafeSetProperty(template, "cas.server.url.logout",
            urlJoin(getConfiguration().get(SERVICE_URL), "logout"));
        nullSafeSetProperty(template, "cas.local.filterPath", urlJoin("", CAS_FILTER_PATH));
        nullSafeSetProperty(template, "cas.local.url",
            urlJoin(getConfiguration().get(APPLICATION_URL), CAS_FILTER_PATH));
        nullSafeSetProperty(template, "psc.defaultTarget",       DEFAULT_TARGET_PATH);
        nullSafeSetProperty(template, "populatorBeanName", getPopulatorBeanName());
        nullSafeSetProperty(template, "ticketValidatorBeanName", getTicketValidatorBeanName());

        return template;
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
        filter.setAuthenticationFailureUrl("/accessDenied.jsp");
        return SpringBeanConfigurationTools.prepareBean(casContext, filter);
    }

    @Override
    public Filter logoutFilter() {
        return (Filter) casContext.getBean("casLogoutFilter");
    }

    public Authentication createUsernamePasswordAuthenticationRequest(String username, String password) {
        return null;
    }

    public Authentication createTokenAuthenticationRequest(String token) {
        return new UsernamePasswordAuthenticationToken(CasProcessingFilter.CAS_STATELESS_IDENTIFIER, token);
    }

    protected String getPopulatorBeanName() {
        return "casAuthoritiesPopulator";
    }

    protected String getTicketValidatorBeanName() {
        return "casProxyTicketValidator";
    }

    private static String absoluteClasspathResourceNameFor(String relative) {
        return "/" + CasAuthenticationSystem.class.getName().
            replaceAll(CasAuthenticationSystem.class.getSimpleName(), "").
            replaceAll("\\.", "/") + relative;
    }
}
