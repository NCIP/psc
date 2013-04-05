/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

/**
 *
 */
package edu.northwestern.bioinformatics.studycalendar.grid;

import gov.nih.nci.cabig.ccts.domain.Study;
import gov.nih.nci.cabig.ctms.audit.DataAuditInfo;
import gov.nih.nci.ccts.grid.studyconsumer.client.StudyConsumerClient;
import gov.nih.nci.ccts.grid.studyconsumer.stubs.types.InvalidStudyException;
import gov.nih.nci.ccts.grid.studyconsumer.stubs.types.StudyCreationException;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbunit.DBTestCase;
import org.dbunit.PropertiesBasedJdbcDatabaseTester;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import edu.northwestern.bioinformatics.studycalendar.core.DaoTestCase;

import java.io.*;
import java.util.Date;

/**
 * @author <a href="mailto:joshua.phillips@semanticbits.com>Joshua Phillips</a>
 */
public class PSCStudyConsumerTest extends DaoTestCase {


    private String clientConfigFile;

    private String regFile;

    private String configLoction;

    private String serviceUrl;
    private static final Log logger = LogFactory.getLog(PSCStudyConsumerTest.class);

    private PSCStudyConsumer pscStudyConsumer;

    private ApplicationContext applicationContext;

//    private void init() {

//        applicationContext = new ClassPathXmlApplicationContext(new String[]{
//                // "classpath:applicationContext.xml",
//                "classpath:applicationContext-studyConsumer-grid.xml"});
//        pscStudyConsumer = (PSCStudyConsumer) applicationContext.getBean("studyConsumer");
//
//        clientConfigFile = System.getProperty("psc.test.clientConfigFile",
//                "gov/nih/nci/ccts/grid/client/client-config.wsdd");
//
//        regFile = System.getProperty("psc.test.sampleStudyFile",
//                "grid/study-consumer/test/resources/SampleStudyMessage.xml");
//
//        serviceUrl = System.getProperty("psc.test.serviceUrl",
//                "http://localhost:8080/wsrf/services/cagrid/StudyConsumer");

//        serviceUrl = System.getProperty("psc.test.serviceUrl",
//                "http://10.10.10.2:9012/wsrf/services/cagrid/StudyConsumer");

//        serviceUrl = System.getProperty("psc.test.serviceUrl",
//                "http://cbvapp-d1017.nci.nih.gov:18080/psc-wsrf/services/cagrid/StudyConsumer");


//        String driver = System.getProperty("psc.test.db.driver", "org.postgresql.Driver");

//          String url = System.getProperty("psc.test.db.url", "jdbc:postgresql://cbiovdev5004.nci.nih.gov:5455/psc");
//        String usr = System.getProperty("psc.test.db.usr", "pscdev");
//        String pwd = System.getProperty("psc.test.db.pwd", "devpsc1234");

//        String url = System.getProperty("psc.test.db.url", "jdbc:postgresql:psc-test");
//        //  String url = System.getProperty("psc.test.db.url", "jdbc:postgresql://10.10.10.2:5432/psc");
//        String usr = System.getProperty("psc.test.db.usr", "psc");
//        String pwd = System.getProperty("psc.test.db.pwd", "psc");
//
//        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_DRIVER_CLASS, driver);
//        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_CONNECTION_URL, url);
//        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_USERNAME, usr);
//        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_PASSWORD, pwd);
//    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        regFile = System.getProperty("psc.test.sampleRegistrationFile");
        //DataAuditInfo.setLocal(new gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo("test", "localhost", new Date(), "/wsrf/services/cagrid/StudyConsumer"));
    }
//
//    @Override
//    public void tearDown() throws Exception {
//        super.tearDown();
//        DataAuditInfo.setLocal(null);
//
//    }

//    public PSCStudyConsumerTest() {
//        super();
//        init();
//
//    }
//
//    public PSCStudyConsumerTest(final String name) {
//        super(name);
//        init();
//    }

    @Override
    protected DatabaseOperation getSetUpOperation() throws Exception {
        //return DatabaseOperation.INSERT;
        return DatabaseOperation.CLEAN_INSERT;
    }

    @Override
    protected DatabaseOperation getTearDownOperation() throws Exception {
        return DatabaseOperation.NONE;
        //return DatabaseOperation.NONE;
    }

    public void testCreateStudyLocal() throws Exception {
        logger.info("running test create study local method");


        Study study = populateStudyDTO();
        pscStudyConsumer.createStudy(study);

        pscStudyConsumer.rollback(study);

//        studyClient.createStudy(study);
//        studyClient.commit(study);
//
//        studyClient.rollback(study);

        DataAuditInfo.setLocal(null);
    }

    public void testCommitStudyLocal() throws Exception {
        try {
            PSCStudyConsumer studyClient = new PSCStudyConsumer();

            Study study = populateStudyDTO();
            studyClient.createStudy(study);
            studyClient.commit(study);

//            //now check for audit info
//
//            assertTrue(auditHistoryRepository.checkIfEntityWasCreatedMinutesBeforeSpecificDate(study.getClass(),study.get));
        }
        catch (Exception e) {

            e.printStackTrace();

        }

    }

    public void testRollbackStudyLocal() throws StudyCreationException, InvalidStudyException {
        try {
            PSCStudyConsumer studyClient = new PSCStudyConsumer();
            Study study = populateStudyDTO();
            DataAuditInfo.setLocal(new gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo("test", "127.0.0.1", new Date(), ""));

            studyClient.createStudy(study);

            studyClient.rollback(study);
            DataAuditInfo.setLocal(null);

        }
        catch (Exception e) {
            e.printStackTrace();

        }

    }

//    public static void main(final String[] args) {
//        junit.textui.TestRunner.run(suite());
//    }

    public void testCommitRemote() throws Exception {
        try {
            StudyConsumerClient studyClient = new StudyConsumerClient(serviceUrl);
            Study study = populateStudyDTO();
            studyClient.commit(study);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void testRollbackRemote() throws Exception {
        try {
            StudyConsumerClient studyClient = new StudyConsumerClient(serviceUrl);
            Study study = populateStudyDTO();
            studyClient.createStudy(study);
            studyClient.rollback(study);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void testCreateStudyRemote() throws Exception {
        try {
            StudyConsumerClient studyClient = new StudyConsumerClient(serviceUrl);
            Study study = populateStudyDTO();
            //studyClient.rollback(study);
            studyClient.createStudy(study);
            //studyClient.commit(study);
            // studyClient.commit(study);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public Study populateStudyDTO() throws Exception {
        Study studyDTO = null;
        try {
            InputStream config = Thread.currentThread().getContextClassLoader().getResourceAsStream(
                    "gov/nih/nci/ccts/grid/client/client-config.wsdd");
            Reader reader = null;
            if (regFile != null){
            	reader = new FileReader(regFile);
            }else{
            	reader = new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(
                    "SampleStudyMessage.xml"));
            }
            studyDTO = (Study) gov.nih.nci.cagrid.common.Utils.deserializeObject(reader, Study.class, config);
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw e;
        }
        return studyDTO;
    }

//    public static Test suite() {
//        TestSuite suite = new TestSuite();

        /*
           * NOTE: These tests CANNOT be run in succession because it will cause the maximum number of connections to be exceeded.
           */
        // suite.addTest(new PSCStudyConsumerTest("testCreateNotificationRemote"));
        //suite.addTest(new PSCStudyConsumerTest("testCommitStudyLocal"));
        // suite.addTest(new PSCStudyConsumerTest("testRollbackStudyLocal"));
        //suite.addTest(new PSCStudyConsumerTest("testCreateStudyRemote"));

//        suite.addTest(new PSCStudyConsumerTest("testCreateStudyLocal"));
//        return suite;
//    }

    @Override
    protected IDataSet getDataSet() throws Exception {
//        String fileName = "grid/study-consumer/test/resources/test_data.xml";
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
