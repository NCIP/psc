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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.dbunit.DBTestCase;
import org.dbunit.PropertiesBasedJdbcDatabaseTester;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import edu.northwestern.bioinformatics.studycalendar.dao.SubjectDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo;
import gov.nih.nci.cagrid.common.Utils;
import gov.nih.nci.ccts.grid.ParticipantType;
import gov.nih.nci.ccts.grid.Registration;
import gov.nih.nci.ccts.grid.client.RegistrationConsumerClient;
import gov.nih.nci.ccts.grid.common.RegistrationConsumer;

/**
 * @author <a href="mailto:joshua.phillips@semanticbits.com>Joshua Phillips</a>
 * 
 */
public class PSCRegistrationConsumerTest extends DBTestCase {

	private String clientConfigFile;

	private String regFile;

	private String serviceUrl;

	private ApplicationContext ctx;

	private SubjectDao subjectDao;

	private StudySubjectAssignmentDao studySubjectAssignmentDao;

	private StudyDao studyDao;

	private SiteDao siteDao;

	private StudySiteDao studySiteDao;

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
		String url = System.getProperty("psc.test.db.url", "jdbc:postgresql://localhost:5432/studycalendar");
		String usr = System.getProperty("psc.test.db.usr", "psc");
		String pwd = System.getProperty("psc.test.db.pwd", "psc");

		System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_DRIVER_CLASS, driver);
		System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_CONNECTION_URL, url);
		System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_USERNAME, usr);
		System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_PASSWORD, pwd);

		ctx = new ClassPathXmlApplicationContext(new String[] { "classpath:applicationContext.xml",
				"classpath:applicationContext-api.xml", "classpath:applicationContext-command.xml",
				"classpath:applicationContext-dao.xml", "classpath:applicationContext-security.xml",
				"classpath:applicationContext-service.xml", "classpath:applicationContext-spring.xml" });

		subjectDao = (SubjectDao) ctx.getBean("subjectDao");
		studyDao = (StudyDao) ctx.getBean("studyDao");
		siteDao = (SiteDao) ctx.getBean("siteDao");
		studySubjectAssignmentDao = (StudySubjectAssignmentDao) ctx.getBean("studySubjectAssignmentDao");
		studySiteDao = (StudySiteDao) ctx.getBean("studySiteDao");
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
			DataAuditInfo.setLocal(new DataAuditInfo("test", "127.0.0.1", new Date(), ""));
			RegistrationConsumer consumer = new PSCRegistrationConsumer();
			consumer.register(reg);
			DataAuditInfo.setLocal(null);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			fail("Error creating registration: " + ex.getMessage());
		}
		validateRegistration(reg);
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

	private void validateRegistration(final Registration registration) {
		// first validate subject
		Subject participantToBeValidated = subjectDao.getByGridId(registration.getSubject().getGridId());
		validateParticipant(participantToBeValidated, registration.getSubject());

		Study study = studyDao.getById(-1);
		Site site = siteDao.getById(-1);
		assertNotNull(site);
		assertNotNull(study);
		StudySubjectAssignment studySubjectAssignment = subjectDao.getAssignment(participantToBeValidated,
				study, site);

		assertNotNull(studySubjectAssignment);

		// TODO: Check if it was correctly populated.
	}

	private void validateParticipant(final Subject participantToBeValidated,
			final ParticipantType registerdParticipant) {
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
		suite.addTest(new PSCRegistrationConsumerTest("testCreateRegistrationLocal"));
		// suite.addTest(new PSCRegistrationConsumerTest("testCreateRegistrationRemote"));
		return suite;
	}

	@Override
	protected IDataSet getDataSet() throws Exception {

		String fileName = "test_data.xml";
		File testFile = new File(fileName);
		if (!testFile.exists()) {
			throw new RuntimeException(fileName + " not found.");
		}

		return new FlatXmlDataSet(new FileInputStream(testFile));
	}

}
