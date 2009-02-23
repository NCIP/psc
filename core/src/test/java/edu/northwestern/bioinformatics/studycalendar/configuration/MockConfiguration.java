package edu.northwestern.bioinformatics.studycalendar.configuration;

import gov.nih.nci.cabig.ctms.tools.configuration.TransientConfiguration;

import edu.northwestern.bioinformatics.studycalendar.configuration.Configuration;

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
