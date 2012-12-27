/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dataproviders.commands;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public abstract class ProviderCommand<T> extends BusyBoxCommand {
    private BundleContext bundleContext;

    public ProviderCommand(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    protected abstract Class<T> providerClass();

    @SuppressWarnings({ "unchecked" })
    protected Map<String, T> getProviders(PrintStream err) {
        Map<String, T> providers = new LinkedHashMap<String, T>();
        ServiceReference[] providerRefs = getProviderReferences();
        if (providerRefs != null) {
            for (ServiceReference ref : providerRefs) {
                T provider = (T) bundleContext.getService(ref);
                providers.put(ref.getBundle().getSymbolicName(), provider);
            }
        }
        if (providers.isEmpty()) {
            err.println("No " + getName() + " providers active");
        }
        return providers;
    }

    private ServiceReference[] getProviderReferences() {
        try {
            return bundleContext.getAllServiceReferences(providerClass().getName(), null);
        } catch (InvalidSyntaxException e) {
            throw new Error("This shouldn't be possible", e);
        }
    }
}
