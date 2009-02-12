package edu.northwestern.bioinformatics.studycalendar.tools;

import junit.framework.TestCase;

import java.util.Properties;

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
}
