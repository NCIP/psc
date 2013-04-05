/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.configuration;

import edu.northwestern.bioinformatics.studycalendar.core.DaoTestCase;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperty;
import gov.nih.nci.cabig.ctms.tools.configuration.DefaultConfigurationProperty;

import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class StoredAuthenticationSystemConfigurationTest extends DaoTestCase {
    private StoredAuthenticationSystemConfiguration configuration
        = (StoredAuthenticationSystemConfiguration) getApplicationContext().getBean("storedAuthenticationSystemConfiguration");
    private static final ConfigurationProperty<String> AUTHENTICATION_SYSTEM
        = new DefaultConfigurationProperty.Text("authenticationSystem");

    public void testConfigurationReadsFromAuthSystemConfTable() throws Exception {
        assertEquals("edu.northwestern.bioinformatics.psc.etc-system",
            configuration.get(AUTHENTICATION_SYSTEM));
    }

    public void testExposesRawData() throws Exception {
        Map<String, String> rawData = configuration.getRawData();
        assertEquals("Wrong number of records", 2, rawData.size());
        assertEquals("Missing system",
            "edu.northwestern.bioinformatics.psc.etc-system", rawData.get("authenticationSystem"));
        assertEquals("Missing unmapped item",
            "something", rawData.get("unmapped"));
    }
}
