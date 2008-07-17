/**
 *
 */
package edu.northwestern.bioinformatics.studycalendar.grid;

import edu.northwestern.bioinformatics.studycalendar.dao.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import gov.nih.nci.cabig.ctms.audit.DataAuditInfo;
import gov.nih.nci.cagrid.common.Utils;
import gov.nih.nci.ccts.grid.IdentifierType;
import gov.nih.nci.ccts.grid.OrganizationAssignedIdentifierType;
import gov.nih.nci.ccts.grid.ParticipantType;
import gov.nih.nci.ccts.grid.Registration;
import gov.nih.nci.ccts.grid.client.RegistrationConsumerClient;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.commons.lang.StringUtils;
import org.dbunit.DBTestCase;
import org.dbunit.PropertiesBasedJdbcDatabaseTester;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.*;
import java.util.Date;

/**
 * @author <a href="mailto:joshua.phillips@semanticbits.com>Joshua Phillips</a>
 */
public class PSCRegistrationConsumerTest extends DBTestCase {

    private String clientConfigFile;

    private String regFile;

    private String serviceUrl;


    private SubjectDao subjectDao;

    private StudySubjectAssignmentDao studySubjectAssignmentDao;

    private StudyDao studyDao;

    private StudyService studyService;

    private SiteDao siteDao;

    private StudySiteDao studySiteDao;

    private PSCRegistrationConsumer registrationConsumer;


    private ApplicationContext applicationContext;


    private static final String COORDINATING_CENTER_IDENTIFIER_TYPE = "Coordinating Center Identifier";

    // private static Log logger = LogFactory.getLog(PSCRegistrationConsumerTest.class);

    private void init() {

        applicationContext=new ClassPathXmlApplicationContext(new String[]{
                        // "classpath:applicationContext.xml",
                        "classpath:applicationContext-grid.xml"});
        registrationConsumer= (PSCRegistrationConsumer) applicationContext.getBean("registrationConsumer");


        clientConfigFile = System.getProperty("psc.test.clientConfigFile",
                "gov/nih/nci/ccts/grid/client/client-config.wsdd");
        regFile = System.getProperty("psc.test.sampleRegistrationFile",
                "grid/registration-consumer/test/resources/SampleRegistrationMessage.xml");
//        serviceUrl = System.getProperty("psc.test.serviceUrl",
//                "http://10.10.10.2:9012/wsrf/services/cagrid/RegistrationConsumer");
////
        serviceUrl = System.getProperty("psc.test.serviceUrl",
                "https://localhost:8443/psc-wsrf/services/cagrid/RegistrationConsumer");
//        serviceUrl = System.getProperty("psc.test.serviceUrl",
//                "http://cbvapp-d1017.nci.nih.gov:18080/psc-wsrf/services/cagrid/RegistrationConsumer");

//        String url = System.getProperty("psc.test.db.url", "jdbc:postgresql://cbiovdev5004.nci.nih.gov:5455/psc");
//        String usr = System.getProperty("psc.test.db.usr", "pscdev");
//        String pwd = System.getProperty("psc.test.db.pwd", "devpsc1234");

        String driver = System.getProperty("psc.test.db.driver", "org.postgresql.Driver");
//        String url = System.getProperty("psc.test.db.url", "jdbc:postgresql://localhost:5432/psc");
//        //String url = System.getProperty("psc.test.db.url", "jdbc:postgresql://10.10.10.2:5432/psc");
//        String usr = System.getProperty("psc.test.db.usr", "psc");
//        String pwd = System.getProperty("psc.test.db.pwd", "psc");

        String url = System.getProperty("psc.test.db.url", "jdbc:postgresql:psc_test");
        //String url = System.getProperty("psc.test.db.url", "jdbc:postgresql://10.10.10.2:5432/psc");
        String usr = System.getProperty("psc.test.db.usr", "psc");
        String pwd = System.getProperty("psc.test.db.pwd", "psc");


        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_DRIVER_CLASS, driver);
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_CONNECTION_URL, url);
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_USERNAME, usr);
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_PASSWORD, pwd);



        subjectDao = (SubjectDao) applicationContext.getBean("subjectDao");
        studyDao = (StudyDao) applicationContext.getBean("studyDao");
        siteDao = (SiteDao) applicationContext.getBean("siteDao");
        studySubjectAssignmentDao = (StudySubjectAssignmentDao) applicationContext.getBean("studySubjectAssignmentDao");
        studySiteDao = (StudySiteDao) applicationContext.getBean("studySiteDao");
        studyService = (StudyService) applicationContext.getBean("studyService");
    }

    public PSCRegistrationConsumerTest() {
        super();
        init();

    }

    public PSCRegistrationConsumerTest(final String name) {
        super(name);
        init();
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        DataAuditInfo.setLocal(new gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo("test", "localhost", new Date(), "/wsrf/services/cagrid/StudyConsumer"));
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        DataAuditInfo.setLocal(null);

    }

    @Override
    protected DatabaseOperation getSetUpOperation() throws Exception {
        return DatabaseOperation.CLEAN_INSERT;
    }

    @Override
    protected DatabaseOperation getTearDownOperation() throws Exception {
        return DatabaseOperation.NONE;
    }

    public void testCreateRegistrationLocal() throws Exception {
        Registration registration = getRegistration();
        registrationConsumer.register(registration);
//        consumer.commit(registration);
        registrationConsumer.rollback(registration);
//        //consumer.register(registration);

//            consumer.commit(registration);
//            consumer.register(registration);
        // DataAuditInfo.setLocal(null);

//        validateRegistration(registration);
    }

    public void testRollbackRegistrationLocal() {
        Registration registration = getRegistration();
        try {
            // DataAuditInfo.setLocal(new DataAuditInfo("test", "127.0.0.1", new Date(), ""));
            registrationConsumer.register(registration);
//            validateRegistration(registration);
            registrationConsumer.rollback(registration);
            // DataAuditInfo.setLocal(null);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            fail("Error creating registration: " + ex.getMessage());
        }

    }

    public void testCommitRegistrationLocal() {
        Registration registration = getRegistration();
        try {
            // DataAuditInfo.setLocal(new DataAuditInfo("test", "127.0.0.1", new Date(), ""));
            registrationConsumer.register(registration);
//            validateRegistration(registration);
            registrationConsumer.commit(registration);
            // DataAuditInfo.setLocal(null);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            fail("Error creating registration: " + ex.getMessage());
        }

    }

    public void testCreateRegistrationRemote() {
        Registration reg = getRegistration();
        try {
            RegistrationConsumerClient client = new RegistrationConsumerClient(serviceUrl);
            client.register(reg);


        }
        catch (Exception ex) {
            ex.printStackTrace();
            fail("Error making call: " + ex.getMessage());
        }
    }

    public void testRollbackRegistrationRemote() {
        Registration registration = getRegistration();
        try {
            RegistrationConsumerClient client = new RegistrationConsumerClient(serviceUrl);
            client.register(registration);
            client.rollback(registration);

        }
        catch (Exception ex) {
            ex.printStackTrace();
            fail("Error making call: " + ex.getMessage());
        }
    }

    public void testCommitRegistrationRemote() {
        Registration registration = getRegistration();
        try {
            RegistrationConsumerClient client = new RegistrationConsumerClient(serviceUrl);
            // client.register(registration);
            client.rollback(registration);

        }
        catch (Exception ex) {
            ex.printStackTrace();
            fail("Error making call: " + ex.getMessage());
        }
    }

    private Registration getRegistration() {

        Registration reg = null;
        try {
            InputStream config = Thread.currentThread().getContextClassLoader().getResourceAsStream(
                    "gov/nih/nci/ccts/grid/client/client-config.wsdd");
            Reader reader = new FileReader(regFile);
            reg = (Registration) Utils.deserializeObject(reader, Registration.class, config);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            fail("Error deserializing Registration object: " + ex.getMessage());
        }
        return reg;
    }

    private String findIdentifierOfType(final IdentifierType[] idTypes, final String ofType) {
        if (idTypes == null) {
            return null;
        }
        for (IdentifierType idType : idTypes) {
            if (idType instanceof OrganizationAssignedIdentifierType && StringUtils.equals(idType.getType(), ofType)) {
                return idType.getValue();
            }
        }
        return null;
    }

    private StudySite findStudySite(final Study study, final String siteNCICode) {
        for (StudySite studySite : study.getStudySites()) {
            if (StringUtils.equals(studySite.getSite().getAssignedIdentifier(), siteNCICode)) {
                return studySite;
            }
        }
        return null;
    }

    private void validateRegistration(final Registration registration) {
        // first validate subject
//        Subject subjectToBeValidated = subjectDao.findSubjectByPersonId(findIdentifierOfType(registration
//                .getParticipant().getIdentifier(), "MRN"));
//        assertNotNull(subjectToBeValidated);
//        validateParticipant(subjectToBeValidated, registration.getParticipant());
//
//        Study study = studyService.getStudyByAssignedIdentifier(findIdentifierOfType(registration.getStudyRef()
//                .getIdentifier(), COORDINATING_CENTER_IDENTIFIER_TYPE));
//        String siteNCICode = registration.getStudySite().getHealthcareSite(0).getNciInstituteCode();
//        StudySite studySite = findStudySite(study, siteNCICode);
//
//        assertNotNull(studySite);
//        assertNotNull(studySite.getSite());
//        assertNotNull(study);
//        StudySubjectAssignment studySubjectAssignment = subjectDao.getAssignment(subjectToBeValidated, study, studySite
//                .getSite());
//
//        assertNotNull(studySubjectAssignment);

        // TODO: Check if it was correctly populated.
    }

    private void validateParticipant(final Subject participantToBeValidated, final ParticipantType registerdParticipant) {
        assertEquals(registerdParticipant.getBirthDate(), participantToBeValidated.getDateOfBirth());
        assertEquals(registerdParticipant.getAdministrativeGenderCode(), participantToBeValidated.getGender());
        assertEquals(registerdParticipant.getFirstName(), participantToBeValidated.getFirstName());
        assertEquals(registerdParticipant.getLastName(), participantToBeValidated.getLastName());
        assertEquals(registerdParticipant.getIdentifier(0).getValue(), participantToBeValidated.getPersonId());

    }

    public static void main(final String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        /*
           * NOTE: These tests CANNOT be run in succession because it will cause the maximum number of connections to be exceeded.
           */
        //  suite.addTest(new PSCRegistrationConsumerTest("testCreateRegistrationLocal"));
        //  suite.addTest(new PSCRegistrationConsumerTest("testRollbackRegistrationLocal"));
       // suite.addTest(new PSCRegistrationConsumerTest("testCommitRegistrationLocal"));
        //suite.addTest(new PSCRegistrationConsumerTest("testCreateRegistrationRemote"));
      suite.addTest(new PSCRegistrationConsumerTest("testCreateRegistrationRemote"));
        //suite.addTest(new PSCRegistrationConsumerTest("testCommitRegistrationRemote"));
        return suite;
    }

    @Override
    protected IDataSet getDataSet() throws Exception {

        String fileName = "grid/registration-consumer/test/resources/test_data.xml";
        File testFile = new File(fileName);
        if (!testFile.exists()) {
            throw new RuntimeException(fileName + " not found.");
        }

        return new FlatXmlDataSet(new FileInputStream(testFile));
    }

}
