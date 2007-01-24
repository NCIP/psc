/**
 * 
 */
package edu.northwestern.bioinformatics.studycalendar.grid;

import gov.nih.nci.cabig.ctms.grid.ae.beans.AENotificationType;
import gov.nih.nci.cabig.ctms.grid.ae.client.AdverseEventConsumerClient;
import gov.nih.nci.cabig.ctms.grid.ae.common.AdverseEventConsumer;
import gov.nih.nci.cagrid.common.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.Reader;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.dbunit.DBTestCase;
import org.dbunit.PropertiesBasedJdbcDatabaseTester;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;

/**
 * @author <a href="mailto:joshua.phillips@semanticbits.com>Joshua Phillips</a>
 * 
 */
public class PSCAdverseEventConsumerTest extends DBTestCase {

    private String clientConfigFile;

    private String aeFile;

    private String serviceUrl;

    private void init() {
        this.clientConfigFile = System.getProperty("psc.test.clientConfigFile",
                        "/gov/nih/nci/cabig/ctms/grid/ae/client/client-config.wsdd");
        this.aeFile = System.getProperty("psc.test.sampleNotificationFile",
                        "test/resources/SampleAdverseEventMessage.xml");
        this.serviceUrl = System.getProperty("psc.test.serviceUrl",
                        "http://localhost:8080/wsrf/services/cagrid/AdverseEventConsumer");

        String driver = System.getProperty("psc.test.db.driver", "org.postgresql.Driver");
        String url = System.getProperty("psc.test.db.url", "jdbc:postgresql:study_calendar");
        String usr = System.getProperty("psc.test.db.usr", "postgres");
        String pwd = System.getProperty("psc.test.db.pwd", "postgres");

        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_DRIVER_CLASS, driver);
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_CONNECTION_URL, url);
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_USERNAME, usr);
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_PASSWORD, pwd);
    }

    public PSCAdverseEventConsumerTest() {
        super();
        init();

    }

    public PSCAdverseEventConsumerTest(String name) {
        super(name);
        init();
    }

    protected DatabaseOperation getSetUpOperation() throws Exception {
        return DatabaseOperation.CLEAN_INSERT;
    }

    protected DatabaseOperation getTearDownOperation() throws Exception {
        return DatabaseOperation.NONE;
    }

    public void testCreateNotificationLocal() {
        AENotificationType ae = getNotification();
        try {
            AdverseEventConsumer consumer = new PSCAdverseEventConsumer();
            consumer.register(ae);
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Error creating registration: " + ex.getMessage());
        }
        validateNotification(ae);
    }

    private void validateNotification(AENotificationType ae) {
        // TODO Auto-generated method stub

    }

    public void testCreateNotificationRemote() {
        AENotificationType ae = getNotification();
        try {
            AdverseEventConsumerClient client = new AdverseEventConsumerClient(this.serviceUrl);
            client.register(ae);
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Error making call: " + ex.getMessage());
        }
    }

    private AENotificationType getNotification() {
        AENotificationType ae = null;
        try {
            InputStream config = getClass().getResourceAsStream(clientConfigFile);
            Reader reader = new FileReader(aeFile);
            ae = (AENotificationType) Utils.deserializeObject(reader, AENotificationType.class,
                            config);
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Error deserializing AENotificationType object: " + ex.getMessage());
        }
        return ae;
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new PSCAdverseEventConsumerTest("testCreateNotificationLocal"));
        suite.addTest(new PSCAdverseEventConsumerTest("testCreateNotificationRemote"));
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
