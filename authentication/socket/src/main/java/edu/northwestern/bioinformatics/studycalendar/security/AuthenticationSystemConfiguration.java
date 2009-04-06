package edu.northwestern.bioinformatics.studycalendar.security;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystemLoadingFailure;
import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.Membrane;
import gov.nih.nci.cabig.ctms.tools.configuration.Configuration;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationEvent;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationListener;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperties;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperty;
import gov.nih.nci.cabig.ctms.tools.configuration.DefaultConfigurationMap;
import gov.nih.nci.cabig.ctms.tools.configuration.DefaultConfigurationProperties;
import gov.nih.nci.cabig.ctms.tools.configuration.DefaultConfigurationProperty;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.ui.savedrequest.SavedRequest;
import org.acegisecurity.userdetails.UserDetailsService;
import org.acegisecurity.util.PortResolverImpl;
import org.apache.commons.lang.StringUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class AuthenticationSystemConfiguration implements Configuration, ConfigurationListener {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private Configuration delegate;
    private DataSource dataSource;
    private UserDetailsService userDetailsService;
    private BundleContext bundleContext;
    private ConfigurationProperties currentProperties;
    private AuthenticationSystem currentSystem, newSystem;
    private ServiceReference currentSystemReference, newSystemReference;
    private boolean propertiesReady;
    private boolean systemReady;

    public static final DefaultConfigurationProperties UNIVERSAL_PROPERTIES
        = new DefaultConfigurationProperties(new ClassPathResource(
            "authentication-system-universal.properties", AuthenticationSystemConfiguration.class));
    public static final ConfigurationProperty<String> AUTHENTICATION_SYSTEM
        = UNIVERSAL_PROPERTIES.add(new DefaultConfigurationProperty.Text("authenticationSystem"));
    private static final String SERVICE_NAME = AuthenticationSystem.class.getName();

    public synchronized ConfigurationProperties getProperties() {
        initProperties();
        return currentProperties;
    }

    public synchronized AuthenticationSystem getAuthenticationSystem() {
        initSystem();
        return currentSystem;
    }

    @Deprecated
    public boolean isCustomAuthenticationSystem() {
        return false;
    }

    private synchronized void signalRebuildNeeded() {
        propertiesReady = false;
        systemReady = false;
    }

    // determines system class & instantiates it to update in the properties.
    // Does not initialize the system.
    private synchronized void initProperties() {
        if (propertiesReady) return;
        systemReady = false;

        newSystemReference = retrieveAuthenticationSystemReference();
        newSystem = acquireAuthenticationSystem();
        log.debug("Successfully retrieved plugin instance {} of class {}",
            newSystem, newSystem.getClass().getName());
        log.debug("Retrieved authentication system has these configuration properties: {}",
            newSystem.configurationProperties().getAll());
        currentProperties = DefaultConfigurationProperties.union(
            UNIVERSAL_PROPERTIES, newSystem.configurationProperties());
        propertiesReady = true;
    }

    // Initializes the system using the parameters determined by initProperties
    private synchronized void initSystem() {
        if (systemReady) return;
        initProperties();
        newSystem.initialize(this);
        log.debug("Successfully initialized new authentication system {}.  Replacing.", newSystem);
        // no errors, so:
        if (currentSystemReference != null) getBundleContext().ungetService(currentSystemReference);
        currentSystemReference = newSystemReference;
        currentSystem = newSystem;
        systemReady = true;
    }

    private ServiceReference retrieveAuthenticationSystemReference() {
        String desired = get(AUTHENTICATION_SYSTEM);
        ServiceReference ref = null;
        if (!StringUtils.isBlank(desired)) {
            ref = findServiceFromBundle(desired);
            if (ref == null) {
                log.error("The configured authentication system \"{}\" is no longer available.  " +
                    "Will use the default system instead.  This may be the cause of any " +
                    "subsequent AuthenticationLoadingFailure.", desired);
            }
        } else {
            log.debug("No specific authentication selected.  Will use the OSGi-selected implementation.");
        }
        if (ref == null) {
            // Get the default, either because nothing is explicitly configured or because
            // the explicitly configured plugin is not available.
            ref = getBundleContext().getServiceReference(SERVICE_NAME);
        }
        if (ref == null) {
            // Still null?  No auth systems available.
            throw new AuthenticationSystemLoadingFailure(
                "No authentication system plugins available from the OSGi layer.  Plugins must be both installed and activated to be used.");
        }
        return ref;
    }

    // TODO: might be able to do this with an OSGi Filter instead
    private ServiceReference findServiceFromBundle(String desiredBundle) {
        log.debug("Searching for selected authentication system bundle \"{}\"", desiredBundle);
        ServiceReference[] refs;
        try {
            refs = getBundleContext().getServiceReferences(SERVICE_NAME, null);
        } catch (InvalidSyntaxException e) {
            throw new StudyCalendarSystemException("Unexpected exception when retrieving list of authentication systems", e);
        }
        for (ServiceReference serviceReference : refs) {
            if (serviceReference.getBundle().getSymbolicName().equals(desiredBundle)) {
                log.debug("Found desired service \"{}\" in bundle {}",
                    desiredBundle, serviceReference.getBundle());
                return serviceReference;
            }
        }
        return null;
    }

    private AuthenticationSystem acquireAuthenticationSystem() {
        log.debug("Thread classloader: {} parent: {}", Thread.currentThread().getContextClassLoader(),
            Thread.currentThread().getContextClassLoader().getParent());
        log.debug("Bundle classloader: {} parent: {}", getBundleContext().getClass().getClassLoader(),
            getBundleContext().getClass().getClassLoader().getParent());
        Membrane osgiMembrane = Membrane.get(
            Thread.currentThread().getContextClassLoader(),
            "edu.northwestern.bioinformatics.studycalendar",
            "edu.northwestern.bioinformatics.studycalendar.security.plugin",
            "org.acegisecurity",
            "org.acegisecurity.providers",
            "org.acegisecurity.ui",
            "org.acegisecurity.ui.savedrequest",
            "org.acegisecurity.userdetails",
            "gov.nih.nci.cabig.ctms.tools.configuration",
            "javax.servlet",
            "javax.servlet.http"
        );
        osgiMembrane.registerProxyConstructorParameters(
            UsernamePasswordAuthenticationToken.class.getName(),
            new Object[] { "unused", "unused" }
        );
        osgiMembrane.registerProxyConstructorParameters(
            AuthenticationException.class.getName(),
            new Object[] { "unused" }
        );
        osgiMembrane.registerProxyConstructorParameters(
            SavedRequest.class.getName(),
            new Object[] { new ProxiableHttpServletRequest(), new PortResolverImpl() }
        );
        Object rawSystem = getBundleContext().getService(newSystemReference);
        log.info("System instance classloader: {} parent: {}", rawSystem.getClass().getClassLoader(),
            rawSystem.getClass().getClassLoader().getParent());
        return (AuthenticationSystem) osgiMembrane.farToNear(rawSystem);
    }

    public void configurationUpdated(ConfigurationEvent update) {
        signalRebuildNeeded();
    }

    ////// Delegating implementation of Configuration

    public <V> V get(ConfigurationProperty<V> property) {
        return delegate.get(property);
    }

    public <V> void set(ConfigurationProperty<V> property, V value) {
        delegate.set(property, value);
    }

    public boolean isSet(ConfigurationProperty<?> property) {
        return delegate.isSet(property);
    }

    public <V> void reset(ConfigurationProperty<V> property) {
        delegate.reset(property);
    }

    public Map<String, Object> getMap() {
        return new DefaultConfigurationMap(this);
    }

    public void addConfigurationListener(ConfigurationListener listener) {
        delegate.addConfigurationListener(listener);
    }

    ////// CONFIGURATION

    public void setDelegate(Configuration delegate) {
        this.delegate = delegate;
        delegate.addConfigurationListener(this);
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    private BundleContext getBundleContext() {
        if (bundleContext == null) {
            throw new StudyCalendarSystemException(
                "No bundle context available.  Authentication system cannot be configured.");
        }
        return bundleContext;
    }

    ////// PROXIABLE CONSTRUCTOR PARAMS

    private static class ProxiableHttpServletRequest implements HttpServletRequest {
        public String getAuthType() {
            throw new UnsupportedOperationException("getAuthType not implemented");
            // return null;
        }

        public Cookie[] getCookies() {
            throw new UnsupportedOperationException("getCookies not implemented");
            // return new javax.servlet.http.Cookie[0];
        }

        public long getDateHeader(String s) {
            throw new UnsupportedOperationException("getDateHeader not implemented");
            // return 0;
        }

        public String getHeader(String s) {
            throw new UnsupportedOperationException("getHeader not implemented");
            // return null;
        }

        public Enumeration getHeaders(String s) {
            throw new UnsupportedOperationException("getHeaders not implemented");
            // return null;
        }

        public Enumeration getHeaderNames() {
            throw new UnsupportedOperationException("getHeaderNames not implemented");
            // return null;
        }

        public int getIntHeader(String s) {
            throw new UnsupportedOperationException("getIntHeader not implemented");
            // return 0;
        }

        public String getMethod() {
            throw new UnsupportedOperationException("getMethod not implemented");
            // return null;
        }

        public String getPathInfo() {
            throw new UnsupportedOperationException("getPathInfo not implemented");
            // return null;
        }

        public String getPathTranslated() {
            throw new UnsupportedOperationException("getPathTranslated not implemented");
            // return null;
        }

        public String getContextPath() {
            throw new UnsupportedOperationException("getContextPath not implemented");
            // return null;
        }

        public String getQueryString() {
            throw new UnsupportedOperationException("getQueryString not implemented");
            // return null;
        }

        public String getRemoteUser() {
            throw new UnsupportedOperationException("getRemoteUser not implemented");
            // return null;
        }

        public boolean isUserInRole(String s) {
            throw new UnsupportedOperationException("isUserInRole not implemented");
            // return false;
        }

        public Principal getUserPrincipal() {
            throw new UnsupportedOperationException("getUserPrincipal not implemented");
            // return null;
        }

        public String getRequestedSessionId() {
            throw new UnsupportedOperationException("getRequestedSessionId not implemented");
            // return null;
        }

        public String getRequestURI() {
            throw new UnsupportedOperationException("getRequestURI not implemented");
            // return null;
        }

        public StringBuffer getRequestURL() {
            throw new UnsupportedOperationException("getRequestURL not implemented");
            // return null;
        }

        public String getServletPath() {
            throw new UnsupportedOperationException("getServletPath not implemented");
            // return null;
        }

        public HttpSession getSession(boolean b) {
            throw new UnsupportedOperationException("getSession not implemented");
            // return null;
        }

        public HttpSession getSession() {
            throw new UnsupportedOperationException("getSession not implemented");
            // return null;
        }

        public boolean isRequestedSessionIdValid() {
            throw new UnsupportedOperationException("isRequestedSessionIdValid not implemented");
            // return false;
        }

        public boolean isRequestedSessionIdFromCookie() {
            throw new UnsupportedOperationException("isRequestedSessionIdFromCookie not implemented");
            // return false;
        }

        public boolean isRequestedSessionIdFromURL() {
            throw new UnsupportedOperationException("isRequestedSessionIdFromURL not implemented");
            // return false;
        }

        public boolean isRequestedSessionIdFromUrl() {
            throw new UnsupportedOperationException("isRequestedSessionIdFromUrl not implemented");
            // return false;
        }

        public Object getAttribute(String s) {
            throw new UnsupportedOperationException("getAttribute not implemented");
            // return null;
        }

        public Enumeration getAttributeNames() {
            throw new UnsupportedOperationException("getAttributeNames not implemented");
            // return null;
        }

        public String getCharacterEncoding() {
            throw new UnsupportedOperationException("getCharacterEncoding not implemented");
            // return null;
        }

        public void setCharacterEncoding(String s) throws UnsupportedEncodingException {
            throw new UnsupportedOperationException("setCharacterEncoding not implemented");

        }

        public int getContentLength() {
            throw new UnsupportedOperationException("getContentLength not implemented");
            // return 0;
        }

        public String getContentType() {
            throw new UnsupportedOperationException("getContentType not implemented");
            // return null;
        }

        public ServletInputStream getInputStream() throws IOException {
            throw new UnsupportedOperationException("getInputStream not implemented");
            // return null;
        }

        public String getParameter(String s) {
            throw new UnsupportedOperationException("getParameter not implemented");
            // return null;
        }

        public Enumeration getParameterNames() {
            throw new UnsupportedOperationException("getParameterNames not implemented");
            // return null;
        }

        public String[] getParameterValues(String s) {
            throw new UnsupportedOperationException("getParameterValues not implemented");
            // return new java.lang.String[0];
        }

        public Map getParameterMap() {
            throw new UnsupportedOperationException("getParameterMap not implemented");
            // return null;
        }

        public String getProtocol() {
            throw new UnsupportedOperationException("getProtocol not implemented");
            // return null;
        }

        public String getScheme() {
            throw new UnsupportedOperationException("getScheme not implemented");
            // return null;
        }

        public String getServerName() {
            throw new UnsupportedOperationException("getServerName not implemented");
            // return null;
        }

        public int getServerPort() {
            throw new UnsupportedOperationException("getServerPort not implemented");
            // return 0;
        }

        public BufferedReader getReader() throws IOException {
            throw new UnsupportedOperationException("getReader not implemented");
            // return null;
        }

        public String getRemoteAddr() {
            throw new UnsupportedOperationException("getRemoteAddr not implemented");
            // return null;
        }

        public String getRemoteHost() {
            throw new UnsupportedOperationException("getRemoteHost not implemented");
            // return null;
        }

        public void setAttribute(String s, Object o) {
            throw new UnsupportedOperationException("setAttribute not implemented");

        }

        public void removeAttribute(String s) {
            throw new UnsupportedOperationException("removeAttribute not implemented");

        }

        public Locale getLocale() {
            throw new UnsupportedOperationException("getLocale not implemented");
            // return null;
        }

        public Enumeration getLocales() {
            throw new UnsupportedOperationException("getLocales not implemented");
            // return null;
        }

        public boolean isSecure() {
            throw new UnsupportedOperationException("isSecure not implemented");
            // return false;
        }

        public RequestDispatcher getRequestDispatcher(String s) {
            throw new UnsupportedOperationException("getRequestDispatcher not implemented");
            // return null;
        }

        public String getRealPath(String s) {
            throw new UnsupportedOperationException("getRealPath not implemented");
            // return null;
        }

        public int getRemotePort() {
            throw new UnsupportedOperationException("getRemotePort not implemented");
            // return 0;
        }

        public String getLocalName() {
            throw new UnsupportedOperationException("getLocalName not implemented");
            // return null;
        }

        public String getLocalAddr() {
            throw new UnsupportedOperationException("getLocalAddr not implemented");
            // return null;
        }

        public int getLocalPort() {
            throw new UnsupportedOperationException("getLocalPort not implemented");
            // return 0;
        }
    }
}
