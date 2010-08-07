package edu.northwestern.bioinformatics.studycalendar.web.osgi;

import edu.northwestern.bioinformatics.studycalendar.core.osgi.OsgiLayerTools;
import edu.northwestern.bioinformatics.studycalendar.security.CompleteAuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.tools.MapBasedDictionary;
import edu.northwestern.bioinformatics.studycalendar.tools.configuration.DictionaryConfiguration;
import edu.northwestern.bioinformatics.studycalendar.tools.configuration.RawDataConfiguration;
import gov.nih.nci.cabig.ctms.tools.configuration.Configuration;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperty;
import gov.nih.nci.cabig.ctms.web.filters.FilterAdapter;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.userdetails.UserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;

/**
 * Bridge from OSGi-layer {@link CompleteAuthenticationSystem} to a filter that
 * is available in the host webapp.
 *
 * @author Rhett Sutphin
 */
public class InstalledAuthenticationSystem extends FilterAdapter implements InitializingBean {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private OsgiLayerTools osgiLayerTools;
    private RawDataConfiguration storedAuthenticationSystemConfiguration;
    private UserDetailsService userDetailsService;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, final FilterChain filterChain) throws IOException, ServletException {
        final CompleteAuthenticationSystem system = getCompleteAuthenticationSystem();

        system.doFilter(servletRequest, servletResponse, new FilterChain() {
            public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException, ServletException {
                final SecurityContext original = SecurityContextHolder.getContext();
                SecurityContextHolder.setContext(system.getCurrentSecurityContext());
                log.debug("SecurityContext bridged from OSGi layer.  Now: {}", SecurityContextHolder.getContext());

                filterChain.doFilter(servletRequest, servletResponse);

                log.debug("Filter processing complete.  Resetting SecurityContext to {}", original);
                SecurityContextHolder.setContext(original);
            }
        });
    }

    public CompleteAuthenticationSystem getCompleteAuthenticationSystem() {
        return osgiLayerTools.getRequiredService(CompleteAuthenticationSystem.class);
    }

    @SuppressWarnings({ "RawUseOfParameterizedType", "unchecked" })
    public void updateCompleteAuthenticationSystem(Configuration props) {
        DictionaryConfiguration dictProps;
        if (props instanceof DictionaryConfiguration) {
            dictProps = (DictionaryConfiguration) props;
        } else {
            dictProps = new DictionaryConfiguration(props);
        }
        Dictionary<String,String> dict = noNulls(dictProps.getDictionary());
        log.debug("Updating authentication system configuration layer with {}", dict);
        osgiLayerTools.updateConfiguration(dict,
            CompleteAuthenticationSystem.SERVICE_PID);
        if (storedAuthenticationSystemConfiguration != props) {
            log.debug("Updating database-persistent authentication system configuration with {}",
                dict);
            for (ConfigurationProperty property : props.getProperties().getAll()) {
                if (props.isSet(property)) {
                    storedAuthenticationSystemConfiguration.set(property, props.get(property));
                }
            }
        }
    }

    // Builds a new Dictionary that matches the target except that it has no null values.
    private Dictionary<String, String> noNulls(Dictionary<String, String> target) {
        Enumeration<String> srcKeys = target.keys();
        Dictionary<String, String> noNulls = new MapBasedDictionary<String, String>();
        while (srcKeys.hasMoreElements()) {
            String key = srcKeys.nextElement();
            if (target.get(key) == null) {
                log.trace("Skipping null-valued entry with key {}", key);
            } else {
                noNulls.put(key, target.get(key));
            }
        }
        return noNulls;
    }

    public AuthenticationSystem getAuthenticationSystem() {
        return getCompleteAuthenticationSystem().getCurrentAuthenticationSystem();
    }

    ////// CONFIGURATION

    @Required
    public void setOsgiLayerTools(OsgiLayerTools osgiLayerTools) {
        this.osgiLayerTools = osgiLayerTools;
    }

    @Required
    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Required
    public void setStoredAuthenticationSystemConfiguration(
        RawDataConfiguration storedAuthenticationSystemConfiguration
    ) {
        this.storedAuthenticationSystemConfiguration = storedAuthenticationSystemConfiguration;
    }

    public void afterPropertiesSet() throws Exception {
        // sync the OSGi layer from the database on startup
        updateCompleteAuthenticationSystem(storedAuthenticationSystemConfiguration);
    }
}
