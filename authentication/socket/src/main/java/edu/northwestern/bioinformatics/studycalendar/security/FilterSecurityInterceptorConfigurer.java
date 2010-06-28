package edu.northwestern.bioinformatics.studycalendar.security;

import org.acegisecurity.ConfigAttributeDefinition;
import org.acegisecurity.SecurityConfig;
import org.acegisecurity.intercept.web.FilterInvocationDefinitionSource;
import org.acegisecurity.intercept.web.FilterSecurityInterceptor;
import org.acegisecurity.intercept.web.PathBasedFilterInvocationDefinitionMap;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Dictionary;
import java.util.Vector;

/**
 * @author Rhett Sutphin
 */
public class FilterSecurityInterceptorConfigurer implements ManagedService {
    private final Logger log = LoggerFactory.getLogger(getClass());

    public static final String SERVICE_PID = "edu.northwestern.bioinformatics.studycalendar.security.filter-security-configurer";
    public static final String PATH_ROLE_MAP_KEY = "pathRoleMap";
    private FilterSecurityInterceptor filter;

    @SuppressWarnings({ "unchecked", "RawUseOfParameterizedType" })
    public void updated(Dictionary dictionary) throws ConfigurationException {
        log.debug("Updating secure paths for {}", filter);
        if (dictionary == null) {
            log.debug("No properties provided; will not update");
        } else {
            Collection<String> serializedMap = (Vector<String>) dictionary.get(PATH_ROLE_MAP_KEY);
            if (serializedMap == null) {
                log.debug("No serialized map present (looked under key {})", PATH_ROLE_MAP_KEY);
            } else {
                filter.setObjectDefinitionSource(createDefinitionSource(serializedMap));
            }
        }
    }

    private FilterInvocationDefinitionSource createDefinitionSource(Collection<String> pathRoleSerializedMap) {
        log.debug("Creating new definition source from {} serialized map entries", pathRoleSerializedMap.size());
        PathBasedFilterInvocationDefinitionMap pathMap = new PathBasedFilterInvocationDefinitionMap();
        for (String pair : pathRoleSerializedMap) {
            String[] pathAndRoles = pair.split("\\|");
            String[] roles;
            if (pathAndRoles.length > 1) {
                roles = pathAndRoles[1].split("\\s+");
            } else {
                roles = new String[0];
            }
            pathMap.addSecureUrl(pathAndRoles[0], createRoleDefinition(roles));
        }
        return pathMap;
    }

    private ConfigAttributeDefinition createRoleDefinition(String[] roles) {
        ConfigAttributeDefinition def = new ConfigAttributeDefinition();
        for (String role: roles) {
            def.addConfigAttribute(new SecurityConfig(role));
        }
        return def;
    }

    ////// CONFIGURATION

    public void setFilterSecurityInterceptor(FilterSecurityInterceptor filter) {
        this.filter = filter;
    }
}
