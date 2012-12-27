/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.osgi.mock;

import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;

/**
 * @author Rhett Sutphin
 */
@SuppressWarnings({ "unchecked", "RawUseOfParameterizedType" })
public class EchoService implements ManagedService {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private ServiceRegistration registration;

    public void updated(Dictionary dictionary) throws ConfigurationException {
        if (dictionary != null) {
            log.debug("Updating properties with {}", dictionary);
            registration.setProperties(dictionary);
        }
    }

    public void register(BundleContext bundleContext, String id) {
        Dictionary initialProps = new MapBuilder().
            put(Constants.SERVICE_PID, "psc.mocks.echo." + id).
            put(Constants.SERVICE_DESCRIPTION, "A service which just echos the properties set on it (" + id + ')').
            toDictionary();
        registration = bundleContext.registerService(
            ManagedService.class.getName(), this,
            initialProps
        );
        log.debug("Registered {} with properties {}", id, initialProps);
    }
}
