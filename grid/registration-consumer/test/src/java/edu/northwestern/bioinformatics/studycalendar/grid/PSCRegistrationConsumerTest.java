/**
 * 
 */
package edu.northwestern.bioinformatics.studycalendar.grid;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.Reader;
import java.util.Date;

import org.dbunit.DBTestCase;
import org.dbunit.PropertiesBasedJdbcDatabaseTester;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.springframework.context.ApplicationContext;

import edu.northwestern.bioinformatics.studycalendar.domain.auditing.DataAuditInfo;

import gov.nih.nci.cabig.ctms.client.RegistrationConsumerClient;
import gov.nih.nci.cabig.ctms.common.RegistrationConsumer;
import gov.nih.nci.cabig.ctms.grid.RegistrationType;
import gov.nih.nci.cagrid.common.Utils;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author <a href="mailto:joshua.phillips@semanticbits.com>Joshua Phillips</a>
 * 
 */
public class PSCRegistrationConsumerTest extends DBTestCase {

    private String clientConfigFile;

    private String regFile;

    private String serviceUrl;
    
    private void init() {
        this.clientConfigFile = System.getProperty("psc.test.clientConfigFile",
                        "/gov/nih/nci/cabig/ctms/client/client-config.wsdd");
        this.regFile = System.getProperty("psc.test.sampleRegistrationFile",
                        "test/resources/SampleRegistrationMessage.xml");
        this.serviceUrl = System.getProperty("psc.test.serviceUrl",
                        "http://localhost:8080/wsrf/services/cagrid/RegistrationConsumer");

        String driver = System.getProperty("psc.test.db.driver", "org.postgresql.Driver");
        String url = System.getProperty("psc.test.db.url", "jdbc:postgresql:study_calendar");
        String usr = System.getProperty("psc.test.db.usr", "postgres");
        String pwd = System.getProperty("psc.test.db.pwd", "postgres");

        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_DRIVER_CLASS, driver);
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_CONNECTION_URL, url);
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_USERNAME, usr);
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_PASSWORD, pwd);
        
        
    }

    public PSCRegistrationConsumerTest() {
        super();
        init();

    }

    public PSCRegistrationConsumerTest(String name) {
        super(name);
        init();
    }
    
    public void setUp() throws Exception{
        super.setUp();
        
    }
    
    public void tearDown() throws Exception{
        super.tearDown();
    }

    protected DatabaseOperation getSetUpOperation() throws Exception {
        return DatabaseOperation.CLEAN_INSERT;
    }

    protected DatabaseOperation getTearDownOperation() throws Exception {
        return DatabaseOperation.DELETE;
    }

    public void testCreateRegistrationLocal() {
        RegistrationType reg = getRegistration();
        try {
            DataAuditInfo.setLocal(new DataAuditInfo("test", "127.0.0.1", new Date(), ""));
            RegistrationConsumer consumer = new PSCRegistrationConsumer();
            consumer.register(reg);
            DataAuditInfo.setLocal(null);
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Error creating registration: " + ex.getMessage());
        }
        validateRegistration(reg);
    }

    public void testCreateRegistrationRemote() {
        RegistrationType reg = getRegistration();
        try {
            RegistrationConsumerClient client = new RegistrationConsumerClient(this.serviceUrl);
            client.register(reg);
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Error making call: " + ex.getMessage());
        }
    }

    private RegistrationType getRegistration() {
        RegistrationType reg = null;
        try {
            InputStream config = getClass().getResourceAsStream(clientConfigFile);
            Reader reader = new FileReader(regFile);
            reg = (RegistrationType) Utils
                            .deserializeObject(reader, RegistrationType.class, config);
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Error deserializing RegistrationType object: " + ex.getMessage());
        }
        return reg;
    }

    private void validateRegistration(RegistrationType reg) {
        // TODO: Check if it was correctly populated.
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new PSCRegistrationConsumerTest("testCreateRegistrationLocal"));
//        suite.addTest(new PSCRegistrationConsumerTest("testCreateRegistrationRemote"));
        return suite;
    }

    @Override
    protected IDataSet getDataSet() throws Exception {
        String fileName = "test/resources/test_data.xml";
        File testFile = new File(fileName);
        if (!testFile.exists()) {
            throw new RuntimeException(fileName + " not found.");
        }

        return new FlatXmlDataSet(new FileInputStream(testFile));
    }

}
