/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.osgi.configuration;

import edu.northwestern.bioinformatics.studycalendar.tools.MapBasedDictionary;
import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.Membrane;
import org.apache.felix.cm.PersistenceManager;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ManagedService;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.osgi.OsgiLayerIntegratedTestHelper.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author Rhett Sutphin
 */
@RunWith(JUnit4.class)
public class PscFelixPersistenceManagerIntegratedTest {
    private static String MOCK_SERVICES_BUNDLE = "edu.northwestern.bioinformatics.psc-osgi-layer-mock";
    private static String MOCK_MANAGED_SERVICE_PID = "psc.mocks.echo.B";
    private static final String PM_BUNDLE = "edu.northwestern.bioinformatics.psc-osgi-layer-felix-persistence-manager";

    @Test
    public void pluginLayerProvidesAPersistenceManagerImplementation() throws Exception {
        waitForService(PersistenceManager.class.getName());

        assertThat(
            getBundleContext().getServiceReference(PersistenceManager.class.getName()),
            is(not(nullValue())));
    }

    @Test
    public void itStoresConfigurationDataToTheDatabase() throws Exception {
        if (skipDueToHsqldb()) return;

        startBundle(MOCK_SERVICES_BUNDLE);
        waitForService(MOCK_SERVICES_BUNDLE, ManagedService.class.getName());
        waitForService(PersistenceManager.class.getName());

        Bundle mockServicesBundle = findBundle(MOCK_SERVICES_BUNDLE);
        Configuration config = getConfigurationAdmin().
            getConfiguration(MOCK_MANAGED_SERVICE_PID, mockServicesBundle.getLocation());
        config.update(new MapBasedDictionary<String, String>(Collections.singletonMap("size.hat", "8.75")));

        JdbcTemplate template = getJdbcTemplate();

        List<String> inDb = template.queryForList(
            "SELECT pv.value FROM osgi_cm_property_values pv INNER JOIN osgi_cm_properties p ON pv.property_id=p.id WHERE p.service_pid=? AND p.name=?",
            String.class,
            MOCK_MANAGED_SERVICE_PID, "size.hat"
        );

        assertThat(inDb, Matchers.hasItem("8.75"));
    }

    private boolean skipDueToHsqldb() {
        String dbName = getJdbcTemplate().execute(new ConnectionCallback<String>() {
            public String doInConnection(Connection con) throws SQLException, DataAccessException {
                return con.getMetaData().getDatabaseProductName();
            }
        });
        if (dbName.toLowerCase().contains("hsql")) {
            System.err.println("Skipping data-backed integration test on assumed in-memory " + dbName + " instance.");
            return true;
        } else {
            return false;
        }
    }

    private JdbcTemplate getJdbcTemplate() {
        return (JdbcTemplate) getApplicationContext().getBean("jdbcTemplate");
    }

    private ConfigurationAdmin getConfigurationAdmin() throws IOException {
        ServiceReference reference =
            getBundleContext().getServiceReference(ConfigurationAdmin.class.getName());
        return (ConfigurationAdmin) getMembrane().farToNear(getBundleContext().getService(reference));
    }

    private Membrane getMembrane() {
        return (Membrane) getApplicationContext().getBean("membrane");
    }
}
