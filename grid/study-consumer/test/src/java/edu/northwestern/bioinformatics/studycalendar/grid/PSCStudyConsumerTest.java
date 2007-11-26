/**
 *
 */
package edu.northwestern.bioinformatics.studycalendar.grid;

import gov.nih.nci.cagrid.common.Utils;
import gov.nih.nci.ccts.grid.Study;
import gov.nih.nci.ccts.grid.client.StudyConsumerClient;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.dbunit.DBTestCase;
import org.dbunit.PropertiesBasedJdbcDatabaseTester;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.Reader;

/**
 * @author <a href="mailto:joshua.phillips@semanticbits.com>Joshua Phillips</a>
 */
public class PSCStudyConsumerTest extends DBTestCase {

	private String clientConfigFile;

	private String regFile;

	private String configLoction;

	private String serviceUrl;

	private void init() {

		clientConfigFile = System.getProperty("psc.test.clientConfigFile",
				"gov/nih/nci/ccts/grid/client/client-config.wsdd");

		regFile = System.getProperty("psc.test.sampleStudyFile",
				"grid/study-consumer/test/resources/SampleStudyMessage.xml");

		// serviceUrl = System.getProperty("psc.test.serviceUrl",
		// "http://localhost:8080/wsrf/services/cagrid/AdverseEventConsumer");

		serviceUrl = System.getProperty("psc.test.serviceUrl",
				"http://10.10.10.2:9017/wsrf/services/cagrid/AdverseEventConsumer");

		String driver = System.getProperty("psc.test.db.driver", "org.postgresql.Driver");
		String url = System.getProperty("psc.test.db.url", "jdbc:postgresql:psc");
		String usr = System.getProperty("psc.test.db.usr", "psc");
		String pwd = System.getProperty("psc.test.db.pwd", "psc");

		System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_DRIVER_CLASS, driver);
		System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_CONNECTION_URL, url);
		System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_USERNAME, usr);
		System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_PASSWORD, pwd);
	}

	public PSCStudyConsumerTest() {
		super();
		init();

	}

	public PSCStudyConsumerTest(final String name) {
		super(name);
		init();
	}

	@Override
	protected DatabaseOperation getSetUpOperation() throws Exception {
		return DatabaseOperation.CLEAN_INSERT;
	}

	@Override
	protected DatabaseOperation getTearDownOperation() throws Exception {
		return DatabaseOperation.NONE;
	}

	public void testCreateStudyLocal() {
		try {
			PSCStudyConsumer studyClient = new PSCStudyConsumer();
			Study study = populateStudyDTO();
			studyClient.createStudy(study);
		}
		catch (Exception e) {
			e.printStackTrace();

		}

	}

	public void testCommitStudyLocal() {
		try {
			PSCStudyConsumer studyClient = new PSCStudyConsumer();
			Study study = populateStudyDTO();
			studyClient.createStudy(study);
			studyClient.commit(study);
		}
		catch (Exception e) {
			e.printStackTrace();

		}

	}

	public void testRollbackStudyLocal() {
		try {
			PSCStudyConsumer studyClient = new PSCStudyConsumer();
			Study study = populateStudyDTO();
			studyClient.createStudy(study);
			studyClient.rollback(study);
		}
		catch (Exception e) {
			e.printStackTrace();

		}

	}

	public static void main(final String[] args) {
		junit.textui.TestRunner.run(suite());
	}

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
			studyClient.createStudy(study);
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
			Reader reader = new FileReader(regFile);
			studyDTO = (Study) Utils.deserializeObject(reader, Study.class, config);
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
		return studyDTO;
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();

		/*
		 * NOTE: These tests CANNOT be run in succession because it will cause the maximum number of connections to be exceeded.
		 */
		// suite.addTest(new PSCStudyConsumerTest("testCreateNotificationRemote"));
		suite.addTest(new PSCStudyConsumerTest("testCommitStudyLocal"));
		// suite.addTest(new PSCStudyConsumerTest("testRollbackStudyLocal"));

		// suite.addTest(new PSCStudyConsumerTest("testCreateStudyLocal"));
		return suite;
	}

	@Override
	protected IDataSet getDataSet() throws Exception {
		String fileName = "grid/study-consumer/test/resources/test_data.xml";
		File testFile = new File(fileName);
		if (!testFile.exists()) {
			throw new RuntimeException(fileName + " not found.");
		}

		return new FlatXmlDataSet(new FileInputStream(testFile));
	}

}
