/**
 *
 */
package edu.northwestern.bioinformatics.studycalendar.grid;

import edu.northwestern.bioinformatics.studycalendar.dao.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import gov.nih.nci.cagrid.common.Utils;
import gov.nih.nci.ccts.grid.IdentifierType;
import gov.nih.nci.ccts.grid.OrganizationAssignedIdentifierType;
import gov.nih.nci.ccts.grid.ParticipantType;
import gov.nih.nci.ccts.grid.Registration;
import gov.nih.nci.ccts.grid.client.RegistrationConsumerClient;
import gov.nih.nci.ccts.grid.common.RegistrationConsumer;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbunit.DBTestCase;
import org.dbunit.PropertiesBasedJdbcDatabaseTester;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.*;

/**
 * @author <a href="mailto:joshua.phillips@semanticbits.com>Joshua Phillips</a>
 */
public class PSCRegistrationConsumerTest extends DBTestCase {

    private String clientConfigFile;

    private String regFile;

    private String serviceUrl;

    private ApplicationContext ctx;

    private SubjectDao subjectDao;

    private StudySubjectAssignmentDao studySubjectAssignmentDao;

    private StudyDao studyDao;

    private StudyService studyService;

    private SiteDao siteDao;

    private StudySiteDao studySiteDao;

    private static final String COORDINATING_CENTER_IDENTIFIER_TYPE = "Coordinating Center Identifier";

    private static Log logger = LogFactory.getLog(PSCRegistrationConsumerTest.class);

    private void init() {
        clientConfigFile = System.getProperty("psc.test.clientConfigFile",
                "gov/nih/nci/ccts/grid/client/client-config.wsdd");
        regFile = System.getProperty("psc.test.sampleRegistrationFile",
                "grid/registration-consumer/test/resources/SampleRegistrationMessage.xml");
        // serviceUrl = System.getProperty("psc.test.serviceUrl",
        // "http://localhost:8080/wsrf/services/cagrid/RegistrationConsumer");

        serviceUrl = System.getProperty("psc.test.serviceUrl",
                "http://localhost:8080/wsrf/services/cagrid/RegistrationConsumer");

        String driver = System.getProperty("psc.test.db.driver", "org.postgresql.Driver");
        String url = System.getProperty("psc.test.db.url", "jdbc:postgresql://localhost:5432/psc");
        String usr = System.getProperty("psc.test.db.usr", "psc");
        String pwd = System.getProperty("psc.test.db.pwd", "psc");

        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_DRIVER_CLASS, driver);
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_CONNECTION_URL, url);
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_USERNAME, usr);
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_PASSWORD, pwd);

        ctx = new ClassPathXmlApplicationContext(new String[]{
                // "classpath:applicationContext.xml",
                "classpath:applicationContext-api.xml", "classpath:applicationContext-command.xml",
                "classpath:applicationContext-dao.xml", "classpath:applicationContext-security.xml",
                "classpath:applicationContext-service.xml", "classpath:applicationContext-db.xml",
                "classpath:applicationContext-spring.xml"});

        subjectDao = (SubjectDao) ctx.getBean("subjectDao");
        studyDao = (StudyDao) ctx.getBean("studyDao");
        siteDao = (SiteDao) ctx.getBean("siteDao");
        studySubjectAssignmentDao = (StudySubjectAssignmentDao) ctx.getBean("studySubjectAssignmentDao");
        studySiteDao = (StudySiteDao) ctx.getBean("studySiteDao");
        studyService = (StudyService) ctx.getBean("studyService");
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

    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected DatabaseOperation getSetUpOperation() throws Exception {
        return DatabaseOperation.CLEAN_INSERT;
    }

    @Override
    protected DatabaseOperation getTearDownOperation() throws Exception {
        return DatabaseOperation.NONE;
    }

    public void testCreateRegistrationLocal() {
        Registration reg = getRegistration();
        try {
            // DataAuditInfo.setLocal(new DataAuditInfo("test", "127.0.0.1", new Date(), ""));
            RegistrationConsumer consumer = new PSCRegistrationConsumer();
            consumer.register(reg);
            // DataAuditInfo.setLocal(null);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            fail("Error creating registration: " + ex.getMessage());
        }
        validateRegistration(reg);
    }

    public void testRollbackRegistrationLocal() {
        Registration registration = getRegistration();
        try {
            // DataAuditInfo.setLocal(new DataAuditInfo("test", "127.0.0.1", new Date(), ""));
            RegistrationConsumer consumer = new PSCRegistrationConsumer();
            consumer.register(registration);
//            validateRegistration(registration);
            consumer.rollback(registration);
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
            RegistrationConsumer consumer = new PSCRegistrationConsumer();
            consumer.register(registration);
//            validateRegistration(registration);
            consumer.commit(registration);
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
        Subject subjectToBeValidated = subjectDao.findSubjectByPersonId(findIdentifierOfType(registration
                .getParticipant().getIdentifier(), "MRN"));
        assertNotNull(subjectToBeValidated);
        validateParticipant(subjectToBeValidated, registration.getParticipant());

        Study study = studyService.getStudyNyAssignedIdentifier(findIdentifierOfType(registration.getStudyRef()
                .getIdentifier(), COORDINATING_CENTER_IDENTIFIER_TYPE));
        String siteNCICode = registration.getStudySite().getHealthcareSite(0).getNciInstituteCode();
        StudySite studySite = findStudySite(study, siteNCICode);

        assertNotNull(studySite);
        assertNotNull(studySite.getSite());
        assertNotNull(study);
        StudySubjectAssignment studySubjectAssignment = subjectDao.getAssignment(subjectToBeValidated, study, studySite
                .getSite());

        assertNotNull(studySubjectAssignment);

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
        //suite.addTest(new PSCRegistrationConsumerTest("testCreateRegistrationLocal"));
        suite.addTest(new PSCRegistrationConsumerTest("testRollbackRegistrationLocal"));
        //suite.addTest(new PSCRegistrationConsumerTest("testCommitRegistrationLocal"));
        // suite.addTest(new PSCRegistrationConsumerTest("testCreateRegistrationRemote"));
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
