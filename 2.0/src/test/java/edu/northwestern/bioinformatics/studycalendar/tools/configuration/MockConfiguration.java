package edu.northwestern.bioinformatics.studycalendar.tools.configuration;

import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperty;
import gov.nih.nci.cabig.ctms.tools.configuration.TransientConfiguration;

import java.util.Map;
import java.util.HashMap;

/**
 * An in-memory-only version of {@link Configuration}, suitable for testing
 *
 * @author Rhett Sutphin
 */
public class MockConfiguration extends TransientConfiguration {
    public MockConfiguration() {
        super(Configuration.PROPERTIES);
    }
}
