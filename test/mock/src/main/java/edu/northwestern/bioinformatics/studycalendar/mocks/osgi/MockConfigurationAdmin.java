/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.mocks.osgi;

import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.Configuration;
import org.osgi.framework.InvalidSyntaxException;

import java.io.IOException;

/**
 * @author Rhett Sutphin
*/
public class MockConfigurationAdmin implements ConfigurationAdmin {
    public Configuration createFactoryConfiguration(String s) throws IOException {
        return new MockConfiguration();
    }

    public Configuration createFactoryConfiguration(String s, String s1) throws IOException {
        return new MockConfiguration();
    }

    public Configuration getConfiguration(String s, String s1) throws IOException {
        return new MockConfiguration();
    }

    public Configuration getConfiguration(String s) throws IOException {
        return new MockConfiguration();
    }

    public Configuration[] listConfigurations(String s) throws IOException, InvalidSyntaxException {
        return new Configuration[] { new MockConfiguration() };
    }
}
