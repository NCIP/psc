package edu.northwestern.bioinformatics.studycalendar.utils.configuration;

import edu.northwestern.bioinformatics.studycalendar.testing.DaoTestCase;
import static edu.northwestern.bioinformatics.studycalendar.utils.configuration.Configuration.*;

import java.util.List;
import java.util.Arrays;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowCallbackHandler;

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

    public void testGetDefaultWhenUnconfigured() throws Exception {
        String actual = configuration.get(DEPLOYMENT_NAME);
        assertEquals("Study Calendar", actual);
    }
    
    public void testGetDefaultExplicitly() throws Exception {
        assertEquals(25, (int) configuration.getDefault(SMTP_PORT));
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

    private <V> void assertStoredValue(final String expected, Property<V> property) {
        final int[] count = new int[1];
        getJdbcTemplate().query("SELECT value FROM configuration WHERE key=?",
            new Object[] { property.getKey() }, new RowCallbackHandler() {
            public void processRow(ResultSet rs) throws SQLException {
                assertEquals(expected, rs.getString("value"));
                count[0]++;
            }
        } );
        assertEquals("Wrong number of values found", 1, count[0]);
    }
}
