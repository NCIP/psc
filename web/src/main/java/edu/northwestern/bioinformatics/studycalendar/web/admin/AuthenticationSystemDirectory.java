/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.admin;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarUserException;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.Membrane;
import gov.nih.nci.cabig.ctms.tools.configuration.Configuration;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperties;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author Rhett Sutphin
 */
public class AuthenticationSystemDirectory {
    private BundleContext bundleContext;
    private Membrane membrane;
    private SortedSet<Entry> entries;

    public AuthenticationSystemDirectory(BundleContext bundleContext, Membrane membrane) {
        this.bundleContext = bundleContext;
        this.membrane = membrane;
        this.entries = new TreeSet<Entry>();
        try {
            ServiceReference defaultSystem
                = bundleContext.getServiceReference(AuthenticationSystem.class.getName());
            ServiceReference[] allSystems
                = bundleContext.getServiceReferences(AuthenticationSystem.class.getName(), null);
            for (ServiceReference ref : allSystems) {
                entries.add(new Entry(ref, ref.equals(defaultSystem)));
            }
        } catch (InvalidSyntaxException e) {
            throw new StudyCalendarSystemException("This should not be possible", e);
        }
    }

    public SortedSet<Entry> getEntries() {
        return entries;
    }

    public Entry get(String key) {
        for (Entry entry : entries) {
            if ((key != null && entry.getKey().equals(key)) || (key == null && entry.isDefault())) {
                return entry;
            }
        }
        throw new StudyCalendarValidationException("No such system %s", key);
    }

    public class Entry implements Comparable<Entry> {
        private ServiceReference serviceReference;
        private boolean isDefault;

        public Entry(ServiceReference serviceReference, boolean isDefault) {
            this.serviceReference = serviceReference;
            this.isDefault = isDefault;
        }

        public void retrieveAndValidateWith(Configuration values) throws StudyCalendarUserException {
            AuthenticationSystem system
                = (AuthenticationSystem) membrane.farToNear(bundleContext.getService(getServiceReference()));
            try {
                system.validate(values);
            } finally {
                bundleContext.ungetService(serviceReference);
            }
        }

        public int compareTo(Entry other) {
            if (this.isDefault() && !other.isDefault()) {
                return -1;
            } else if (other.isDefault()) {
                return 1;
            } else {
                return this.getName().toLowerCase().compareTo(other.getName().toLowerCase());
            }
        }

        public ServiceReference getServiceReference() {
            return serviceReference;
        }

        public String getKey() {
            return getServiceReference().getBundle().getSymbolicName();
        }

        public boolean isDefault() {
            return isDefault;
        }

        public String getName() {
            return (String) getServiceReference().getProperty(AuthenticationSystem.ServiceKeys.NAME);
        }

        public String getBehaviorDescription() {
            return (String) getServiceReference().getProperty(AuthenticationSystem.ServiceKeys.BEHAVIOR_DESCRIPTION);
        }

        public ConfigurationProperties getConfigurationProperties() {
            return (ConfigurationProperties) membrane.farToNear(
                getServiceReference().getProperty(AuthenticationSystem.ServiceKeys.CONFIGURATION_PROPERTIES));
        }
    }
}
