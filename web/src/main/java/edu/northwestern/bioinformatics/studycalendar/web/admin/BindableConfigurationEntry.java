/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.admin;

import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperty;
import gov.nih.nci.cabig.ctms.tools.configuration.Configuration;

/**
 * @author Rhett Sutphin
*/
public class BindableConfigurationEntry<V> {
    private final Configuration configuration;
    private final ConfigurationProperty<V> property;

    public BindableConfigurationEntry(Configuration configuration, ConfigurationProperty<V> property) {
        this.configuration = configuration;
        this.property = property;
    }

    public ConfigurationProperty<V> getProperty() {
        return property;
    }

    public V getValue() {
        return configuration.get(property);
    }

    public void setValue(V value) {
        configuration.set(property, value);
    }

    public V getDefault() {
        return property.getDefault();
    }

    public String toString() {
        return new StringBuilder("[").append(getProperty()).append("=").append(getValue())
            .append("; default=").append(getDefault()).append(']').toString();
    }
}
