package edu.northwestern.bioinformatics.studycalendar.tools;

import junit.framework.TestCase;

import java.util.Properties;

/**
 * @author Saurabh Agrawal
 */
public class PscDataSourceSelfDiscoveringPropertiesFactoryBeanTest extends TestCase {

    private PscDataSourceSelfDiscoveringPropertiesFactoryBean propertiesFactoryBean;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        propertiesFactoryBean = new PscDataSourceSelfDiscoveringPropertiesFactoryBean();
        propertiesFactoryBean.setApplicationDirectoryName("psc");
        propertiesFactoryBean.setDatabaseConfigurationName("datasource");
        //(PscDataSourceSelfDiscoveringPropertiesFactoryBean) getDeployedApplicationContext().getBeansOfType(PscDataSourceSelfDiscoveringPropertiesFactoryBean.class);
        assertNotNull(propertiesFactoryBean);
    }

    public void testGridConfiguration() {
        Properties properties = propertiesFactoryBean.getProperties();


        assertNotNull("registration consumer grid url can not be null", properties.getProperty("grid.registrationconsumer.url"));
        assertEquals("registration consumer grid url value can not be null", "/psc-wsrf/services/cagrid/RegistrationConsumer", properties.getProperty("grid.registrationconsumer.url"));

        assertNotNull("study consumer grid url can not be null", properties.getProperty("grid.studyconsumer.url"));
        assertEquals("study consumer grid url value can not be null", "/psc-wsrf/services/cagrid/StudyConsumer", properties.getProperty("grid.studyconsumer.url"));


    }
}
