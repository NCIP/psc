/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.configuration;

import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationEntry;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperties;
import gov.nih.nci.cabig.ctms.tools.configuration.DatabaseBackedConfiguration;
import gov.nih.nci.cabig.ctms.tools.configuration.DefaultConfigurationProperties;
import org.springframework.orm.hibernate3.HibernateTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.northwestern.bioinformatics.studycalendar.tools.configuration.RawDataConfiguration;

/**
 * @author Rhett Sutphin
 */
public class StoredAuthenticationSystemConfiguration extends DatabaseBackedConfiguration implements RawDataConfiguration {
    private HibernateTemplate hibernateTemplate;

    public ConfigurationProperties getProperties() {
        return DefaultConfigurationProperties.empty();
    }

    @Override
    protected Class<? extends ConfigurationEntry> getConfigurationEntryClass() {
        return AuthenticationSystemConfigurationEntry.class;
    }

    @SuppressWarnings({ "unchecked" })
    public Map<String, String> getRawData() {
        List<ConfigurationEntry> entries = hibernateTemplate.find(
            String.format("from %s", getConfigurationEntryClass().getName()));
        Map<String, String> result = new LinkedHashMap<String, String>();
        for (ConfigurationEntry entry : entries) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    @Override
    // intercept since the superclass getter is private
    public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
        super.setHibernateTemplate(hibernateTemplate);
        this.hibernateTemplate = hibernateTemplate;
    }
}
