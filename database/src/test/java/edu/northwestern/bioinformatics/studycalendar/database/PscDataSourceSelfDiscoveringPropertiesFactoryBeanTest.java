/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.database;

import junit.framework.TestCase;

import java.io.File;
import java.util.Properties;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Saurabh Agrawal
 * @author Rhett Sutphin
 */
public class PscDataSourceSelfDiscoveringPropertiesFactoryBeanTest extends TestCase {

    private PscDataSourceSelfDiscoveringPropertiesFactoryBean bean;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        bean = new PscDataSourceSelfDiscoveringPropertiesFactoryBean();
        bean.setApplicationDirectoryName("psc");
        assertNotNull(bean);
    }

    public void testGridConfiguration() {
        bean.setDatabaseConfigurationName("datasource");
        Properties properties = bean.getProperties();

        assertNotNull("registration consumer grid url can not be null", properties.getProperty("grid.registrationconsumer.url"));
        assertEquals("registration consumer grid url value can not be null", "/wsrf-psc/services/cagrid/RegistrationConsumer", properties.getProperty("grid.registrationconsumer.url"));

        assertNotNull("study consumer grid url can not be null", properties.getProperty("grid.studyconsumer.url"));
        assertEquals("study consumer grid url value can not be null", "/wsrf-psc/services/cagrid/StudyConsumer", properties.getProperty("grid.studyconsumer.url"));

        assertNotNull("roll back time should not be null", properties.getProperty("grid.rollback.timeout"));
        assertEquals("rollback timeout should not be null", "1", properties.getProperty("grid.rollback.timeout"));
    }
    
    public void testDatabaseConfigurationNameTakenFromSystemPropertyIfSet() throws Exception {
        String expected = "hsqldb";
        System.setProperty("psc.config.datasource", expected);

        assertEquals("Config name not taken from system prop", expected, bean.getDatabaseConfigurationName());
    }

    public void testCsmDatasourcePropertiesDefaultToPscDatasourceProperties() {
        Properties properties = bean.getProperties();

        properties.clear();

        String url = "hsqldb", driver = "org.hsqldb.driver", username = "foo", password = "bar", dialect = "spanish";

        properties.setProperty("datasource.url", url);
        properties.setProperty("datasource.driver", driver);
        properties.setProperty("datasource.username", username);
        properties.setProperty("datasource.password", password);
        properties.setProperty("datasource.dialect", dialect);

        bean.computeProperties();

        assertEquals("Config name not taken from psc datasource prop", url, properties.getProperty("csm.datasource.url"));
        assertEquals("Config name not taken from psc datasource prop", driver, properties.getProperty("csm.datasource.driver"));
        assertEquals("Config name not taken from psc datasource prop", username, properties.getProperty("csm.datasource.username"));
        assertEquals("Config name not taken from psc datasource prop", password, properties.getProperty("csm.datasource.password"));
        assertEquals("Config name not taken from psc datasource prop", password, properties.getProperty("csm.datasource.password"));
        assertEquals("Config name not taken from psc datasource prop", dialect, properties.getProperty("csm.datasource.dialect"));
    }

    public void testCsmDatasourcePropertiesOverrideDefaults() {
        Properties properties = bean.getProperties();

        String url = "hsqldb", driver = "org.hsqldb.driver", username = "foo", password = "bar", dialect = "spanish";

        properties.setProperty("csm.datasource.url", url);
        properties.setProperty("csm.datasource.driver", driver);
        properties.setProperty("csm.datasource.username", username);
        properties.setProperty("csm.datasource.password", password);
        properties.setProperty("csm.datasource.dialect", dialect);

        bean.computeProperties();

        assertEquals("Config name not taken from csm datasource prop", url, properties.getProperty("csm.datasource.url"));
        assertEquals("Config name not taken from csm datasource prop", driver, properties.getProperty("csm.datasource.driver"));
        assertEquals("Config name not taken from csm datasource prop", username, properties.getProperty("csm.datasource.username"));
        assertEquals("Config name not taken from csm datasource prop", password, properties.getProperty("csm.datasource.password"));
        assertEquals("Config name not taken from csm datasource prop", dialect, properties.getProperty("csm.datasource.dialect"));
    }

    public void testCsmContextNameDefaultsCorrectly() {
        Properties properties = bean.getProperties();
        assertEquals("Config name not taken from csm datasource prop", "study_calendar", properties.getProperty("csm.application.context"));
    }

    public void testCsmContextNameIsOverrideable() {
        Properties properties = bean.getProperties();

        String context = "CTMS_SUITE";

        properties.setProperty("csm.application.context", context);

        bean.computeProperties();

        assertEquals("Config name not taken from csm datasource prop", context, properties.getProperty("csm.application.context"));
    }

    public void testSearchDirectoriesStartWithSystemPropertyPathIfSpecified() throws Exception {
        System.setProperty("psc.config.path", "/foo/baz");
        assertThat(bean.searchDirectories().get(0), is(new File("/foo/baz")));
    }
}
