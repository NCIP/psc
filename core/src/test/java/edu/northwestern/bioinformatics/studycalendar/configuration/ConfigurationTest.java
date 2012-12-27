/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.configuration;

import edu.northwestern.bioinformatics.studycalendar.core.DaoTestCase;
import static edu.northwestern.bioinformatics.studycalendar.configuration.Configuration.*;
import edu.northwestern.bioinformatics.studycalendar.configuration.Configuration;

import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowCallbackHandler;
import gov.nih.nci.cabig.ctms.tools.configuration.ConfigurationProperty;

/**
 * @author Rhett Sutphin
 */
public class ConfigurationTest extends DaoTestCase {
    private Configuration configuration
        = (Configuration) getApplicationContext().getBean("configuration");

    public void testGetListProperty() throws Exception {
        List<String> actual = configuration.get(MAIL_EXCEPTIONS_TO);
        assertEquals("Wrong number of elements in list", 2, actual.size());
        assertEquals("r-sutphin@northwestern.edu", actual.get(0));
        assertEquals("joe@example.com", actual.get(1));
    }
    
    public void testGetIntegerProperty() throws Exception {
        assertEquals((Integer) 28, configuration.get(SMTP_PORT));
    }

    public void testGetBooleanProperty() throws Exception {
        assertEquals(Boolean.FALSE, configuration.get(SHOW_DEBUG_INFORMATION));
    }

    public void testGetBooleanPropertyForCreatingSubject() throws Exception {
        assertEquals(Boolean.TRUE, configuration.get(ENABLE_ASSIGNING_SUBJECT));
    }

    public void testGetBooleanPropertyForCreatingTemplate() throws Exception {
        assertEquals(Boolean.TRUE, configuration.get(ENABLE_CREATING_TEMPLATE));
    }

    public void testGetDefaultWhenUnconfigured() throws Exception {
        String actual = configuration.get(DEPLOYMENT_NAME);
        assertEquals("Study Calendar", actual);
    }
    
    public void testSetStringProperty() throws Exception {
        configuration.set(DEPLOYMENT_NAME, "Test Deployment");

        interruptSession();

        assertStoredValue("Test Deployment", DEPLOYMENT_NAME);
    }

    public void testSetListProperty() throws Exception {
        configuration.set(MAIL_EXCEPTIONS_TO, Arrays.asList("a", "b", "c"));

        interruptSession();

        assertStoredValue("a, b, c", MAIL_EXCEPTIONS_TO);
    }
    
    public void testMap() throws Exception {
        Map<String, Object> map = configuration.getMap();
        assertEquals(28, map.get("smtpPort"));
        assertEquals(false, map.get("showDebugInformation"));
        assertEquals("Study Calendar", map.get("deploymentName"));
    }
    
    public void testMapReturnsNullForMissing() throws Exception {
        assertNull(configuration.getMap().get("bogus"));
    }

    private <V> void assertStoredValue(final String expected, ConfigurationProperty<V> property) {
        final int[] count = new int[1];
        getJdbcTemplate().query("SELECT value FROM configuration WHERE prop=?",
            new Object[] { property.getKey() }, new RowCallbackHandler() {
            public void processRow(ResultSet rs) throws SQLException {
                assertEquals(expected, rs.getString("value"));
                count[0]++;
            }
        } );
        assertEquals("Wrong number of values found", 1, count[0]);
    }
}
