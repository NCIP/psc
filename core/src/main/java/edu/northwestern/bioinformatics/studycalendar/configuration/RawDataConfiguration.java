package edu.northwestern.bioinformatics.studycalendar.configuration;

import java.util.Map;

/**
 * Extension to {@link gov.nih.nci.cabig.ctms.tools.configuration.Configuration} for
 * configurations that enable access to the raw EAV (or similar) underlying the configuration.
 * This should expose all values in the store, instead of just the ones that match up with
 * the configured {@link gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperties}.
 *
 * @author Rhett Sutphin
 */
public interface RawDataConfiguration extends gov.nih.nci.cabig.ctms.tools.configuration.Configuration {
    Map<String, String> getRawData();
}
