package edu.northwestern.bioinformatics.studycalendar.security;

import junit.framework.TestCase;
import org.acegisecurity.ConfigAttribute;
import org.acegisecurity.ConfigAttributeDefinition;
import org.acegisecurity.intercept.web.FilterSecurityInterceptor;
import org.acegisecurity.intercept.web.PathBasedFilterInvocationDefinitionMap;
import org.osgi.service.cm.ConfigurationException;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import java.util.Dictionary;

/**
 * @author Rhett Sutphin
 */
public class FilterSecurityInterceptorConfigurerTest extends TestCase {
    private FilterSecurityInterceptorConfigurer configurer;
    private FilterSecurityInterceptor filter;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        filter = new FilterSecurityInterceptor();

        configurer = new FilterSecurityInterceptorConfigurer();
        configurer.setFilterSecurityInterceptor(filter);
    }

    public void testSingleRolePath() throws Exception {
        updateWith("/path/foo/**|STUDY_ADMIN");
        ConfigAttributeDefinition actual = actualConfigAttributeDefinitions("/path/foo/**");
        assertEquals("Wrong number of definitions", 1, actual.size());
        assertEquals("Wrong role", "STUDY_ADMIN", ((ConfigAttribute) actual.getConfigAttributes().next()).getAttribute());
    }

    @SuppressWarnings({ "unchecked" })
    public void testMultipleRolePath() throws Exception {
        updateWith("/path/foo/**|STUDY_COORDINATOR SYSTEM_ADMINISTRATOR");
        ConfigAttributeDefinition actual = actualConfigAttributeDefinitions("/path/foo/**");
        assertEquals("Wrong number of definitions", 2, actual.size());

        Iterator<ConfigAttribute> actualAttrs = actual.getConfigAttributes();
        assertEquals("Wrong first role", "STUDY_COORDINATOR", actualAttrs.next().getAttribute());
        assertEquals("Wrong second role", "SYSTEM_ADMINISTRATOR", actualAttrs.next().getAttribute());
    }
    
    public void testMultiplePaths() throws Exception {
        updateWith("/path/foo/**|SYSTEM_ADMINISTRATOR", "/bar/**|SITE_COORDINATOR");
        assertNotNull("Missing definitions for first path", actualConfigAttributeDefinitions("/path/foo/**"));
        assertNotNull("Missing definitions for second path", actualConfigAttributeDefinitions("/bar/**"));
        assertNull("Definitions present for unknown path", actualConfigAttributeDefinitions("/huh/**"));
    }

    public void testNoRolePath() throws Exception {
        updateWith("/path/foo/**");
        ConfigAttributeDefinition actual = actualConfigAttributeDefinitions("/path/foo/**");
        assertEquals("Wrong number of definitions", 0, actual.size());
    }

    @SuppressWarnings({ "unchecked" })
    private void updateWith(String... serializedMap) throws ConfigurationException {
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put(FilterSecurityInterceptorConfigurer.PATH_ROLE_MAP_KEY,
            new Vector(Arrays.asList(serializedMap)));
        configurer.updated(props);
    }

    public ConfigAttributeDefinition actualConfigAttributeDefinitions(String path) {
        return ((PathBasedFilterInvocationDefinitionMap) filter.getObjectDefinitionSource()).lookupAttributes(path);
    }
}
