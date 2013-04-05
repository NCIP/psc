/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.security.plugin.local;

import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.PluginActivator;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceFactory;

import java.util.Dictionary;

/**
 * @author Rhett Sutphin
 */
public class Activator extends PluginActivator {
    @Override
    protected Class<? extends AuthenticationSystem> authenticationSystemClass() {
        return LocalAuthenticationSystem.class;
    }

    @Override
    protected Dictionary<String, Object> serviceProperties(ServiceFactory factory, Bundle bundle) {
        Dictionary<String, Object> props = super.serviceProperties(factory, bundle);
        // This is the default out of the built-in plugins
        props.put(Constants.SERVICE_RANKING, 16);
        return props;
    }
}