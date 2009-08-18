/**
 *
 */
package edu.northwestern.bioinformatics.studycalendar.grid;

import gov.nih.nci.cabig.ccts.ae.domain.AENotificationType;
import gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo;
import gov.nih.nci.cabig.ctms.grid.ae.client.AdverseEventConsumerClient;
import gov.nih.nci.cabig.ctms.grid.ae.common.AdverseEventConsumerI;
import gov.nih.nci.cagrid.common.Utils;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;

import java.io.FileReader;
import java.io.InputStream;
import java.io.Reader;
import java.util.Date;

/**
 * TODO: this is not a unit test.
 *
 * @author <a href="mailto:joshua.phillips@semanticbits.com>Joshua Phillips</a>
 */
public class PSCAdverseEventConsumerTest /* TODO: renable when working // extends DaoTestCase */ {

    private String clientConfigFile;

    private String aeFile;

    private String serviceUrl;

//    private void init() {
//        clientConfigFile = System.getProperty("psc.test.clientConfigFile",
//                "/gov/nih/nci/cabig/ctms/grid/ae/client/client-config.wsdd");
//        aeFile = System.getProperty("psc.test.sampleNotificationFile",
//                "grid/adverse-event-consumer/test/resources/SampleAdverseEventMessage.xml");
//        // serviceUrl = System.getProperty("psc.test.serviceUrl",
//        // "http://localhost:8080/wsrf/services/cagrid/AdverseEventConsumer");
//
//        serviceUrl = System.getProperty("psc.test.serviceUrl",
//                "https://localhost:8080/wsrf/services/cagrid/AdverseEventConsumer");
//
//        String driver = System.getProperty("psc.test.db.driver", "org.postgresql.Driver");
//        String url = System.getProperty("psc.test.db.url", "jdbc:postgresql:psc_test");
//        String usr = System.getProperty("psc.test.db.usr", "psc");
//        String pwd = System.getProperty("psc.test.db.pwd", "psc");
//
//        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_DRIVER_CLASS, driver);
//        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_CONNECTION_URL, url);
//        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_USERNAME, usr);
//        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_PASSWORD, pwd);
//    }
//
//    public PSCAdverseEventConsumerTest() {
//        super();
//        init();
//
//    }
//
//    public PSCAdverseEventConsumerTest(final String name) {
//        super(name);
//        init();
//    }
//
//    @Override
//    protected DatabaseOperation getSetUpOperation() throws Exception {
//        return DatabaseOperation.CLEAN_INSERT;
//    }
//
//    @Override
//    protected DatabaseOperation getTearDownOperation() throws Exception {
//        return DatabaseOperation.NONE;
//    }

    public void testCreateNotificationLocal() throws Exception {
        AENotificationType ae = getNotification();
        DataAuditInfo.setLocal(new DataAuditInfo("test", "127.0.0.1", new Date(), ""));
        AdverseEventConsumerI consumer = new PSCAdverseEventConsumer();
        consumer.register(ae);
        DataAuditInfo.setLocal(null);
        validateNotification(ae);
    }

    private void validateNotification(final AENotificationType ae) {
        // TODO Auto-generated method stub

    }

    private AENotificationType getAe() throws Exception {
        FileReader reader = new FileReader(aeFile);
        InputStream is = getClass().getResourceAsStream(clientConfigFile);
        AENotificationType ae = (AENotificationType) Utils.deserializeObject(reader, AENotificationType.class, is);

        return ae;
    }

    public void testCreateNotificationRemote() throws Exception {
        AENotificationType ae = getAe();

        AdverseEventConsumerClient client = new AdverseEventConsumerClient(serviceUrl);
        client.register(ae);
    }

    private AENotificationType getNotification() throws Exception {
        AENotificationType ae = null;
        // InputStream config = getClass().getResourceAsStream(clientConfigFile);
        Reader reader = new FileReader(aeFile);
        ae = (AENotificationType) Utils.deserializeObject(reader, AENotificationType.class);
        return ae;
    }

//    public static void main(final String[] args) {
//        junit.textui.TestRunner.run(suite());
//    }
//
//    public static Test suite() {
//        TestSuite suite = new TestSuite();
//
//        /*
//           * NOTE: These tests CANNOT be run in succession because it will cause the maximum number of connections to be exceeded.
//           */
//        suite.addTest(new PSCAdverseEventConsumerTest("testCreateNotificationRemote"));
//        // suite.addTest(new PSCAdverseEventConsumerTest("testCreateNotificationLocal"));
//        return suite;
//    }

//    @Override
    protected IDataSet getDataSet() throws Exception {
        //String fileName = "grid/adverse-event-consumer/test/resources/test_data.xml";
//        File testFile = new File(fileName);
//        if (!testFile.exists()) {
//            throw new RuntimeException(fileName + " not found.");
//        }
//
//        return new FlatXmlDataSet(new FileInputStream(testFile));
    	String fileName = "test_data.xml";
        return new FlatXmlDataSet(Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName));
    }

}
