package edu.northwestern.bioinformatics.studycalendar.grid;

import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Date;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.test.AbstractTransactionalSpringContextTests;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo;
import org.easymock.classextension.EasyMock;
import static org.easymock.EasyMock.expect;

import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUserDetailsService;


public class PSCStudyConsumerTest extends AbstractTransactionalSpringContextTests {

	public static final Log logger = LogFactory.getLog(PSCStudyConsumerTest.class);
	
	private String regFile;
	
	private PSCStudyConsumer studyConsumer;
		
	private StudyService studyService;

	private String ccIdentifier = "TEST_STUDY";
	
	private StudyGridServiceAuthorizationHelper gridServicesAuthorizationHelper;
	private PscUserDetailsService pscUserDetailsService;
	private PscUser user;
	
	protected void onSetUpInTransaction() throws Exception {
		
		DataAuditInfo.setLocal(new DataAuditInfo("test", "localhost", new Date(), "/wsrf-psc/services/cagrid/StudyConsumer"));
		regFile = System.getProperty("psc.test.sampleStudyFile");
	
		gridServicesAuthorizationHelper=EasyMock.createMock(StudyGridServiceAuthorizationHelper.class);
		pscUserDetailsService=EasyMock.createMock(PscUserDetailsService.class);

		SuiteRoleMembership suiteRoleMembership = new SuiteRoleMembership(SuiteRole.STUDY_CREATOR, null, null);
		suiteRoleMembership.addSite("TEST_SITE");
		Map<SuiteRole,SuiteRoleMembership> expectedMemberships = Collections.singletonMap(SuiteRole.STUDY_CREATOR,
				suiteRoleMembership);

		user = new PscUser(null, expectedMemberships);

	}
	
	public void testCreateStudyLocal() throws Exception {
		logger.info("### Running test create study local method");
		gov.nih.nci.cabig.ccts.domain.Study study = populateStudyDTO();
		
		studyConsumer.setGridServicesAuthorizationHelper(gridServicesAuthorizationHelper);
		studyConsumer.setPscUserDetailsService(pscUserDetailsService);

		expect(gridServicesAuthorizationHelper.getCurrentUsername()).andReturn("John");
		expect(pscUserDetailsService.loadUserByUsername("John")).andReturn(user);

		EasyMock.replay(gridServicesAuthorizationHelper);
		EasyMock.replay(pscUserDetailsService);

		studyConsumer.createStudy(study);
		
		EasyMock.verify(gridServicesAuthorizationHelper);
		EasyMock.verify(pscUserDetailsService);
		

		Study pscStudy = studyService.getStudyByAssignedIdentifier(ccIdentifier);
		assertNotNull("must create study", pscStudy);
		assertNotNull("must create study", pscStudy.getId());
	}
	
	public void testRollbackStudyLocal() throws Exception {
		logger.info("### Running test rollback study local method");
		gov.nih.nci.cabig.ccts.domain.Study study = populateStudyDTO();
		logger.info("Creating local study");
		
		studyConsumer.setGridServicesAuthorizationHelper(gridServicesAuthorizationHelper);
		studyConsumer.setPscUserDetailsService(pscUserDetailsService);

		expect(gridServicesAuthorizationHelper.getCurrentUsername()).andReturn("John");
		expect(pscUserDetailsService.loadUserByUsername("John")).andReturn(user);

		EasyMock.replay(gridServicesAuthorizationHelper);
		EasyMock.replay(pscUserDetailsService);
		
		studyConsumer.createStudy(study);
		
		EasyMock.verify(gridServicesAuthorizationHelper);
		EasyMock.verify(pscUserDetailsService);
		
		Study pscStudy = studyService.getStudyByAssignedIdentifier(ccIdentifier);
		assertNotNull("must create study", pscStudy);
		assertNotNull("must create study", pscStudy.getId());
		
		logger.info("Calling Rollback study method");
		
		studyConsumer.rollback(study);
		pscStudy = studyService.getStudyByAssignedIdentifier(ccIdentifier);
		assertNull("Study rollback error : ", pscStudy);
	}


	private gov.nih.nci.cabig.ccts.domain.Study populateStudyDTO() throws Exception {
		gov.nih.nci.cabig.ccts.domain.Study studyDTO = null;
		try {
			InputStream config = Thread.currentThread().getContextClassLoader().getResourceAsStream(
					"gov/nih/nci/ccts/grid/studyconsumer/client/client-config.wsdd");
			Reader reader = null;
			if (regFile != null){
				reader = new FileReader(regFile);
			}else{
				reader = new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("SampleStudyMessage.xml"));
			}
			studyDTO = (gov.nih.nci.cabig.ccts.domain.Study) gov.nih.nci.cagrid.common.Utils.deserializeObject(reader, gov.nih.nci.cabig.ccts.domain.Study.class, config);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return studyDTO;
	}


	protected String[] getConfigLocations() {

		String[] configs = {"classpath:applicationContext-studyConsumer-grid.xml"};
		return configs;
	}

	protected void onTearDownAfterTransaction() throws Exception {
		DataAuditInfo.setLocal(null);
		
	}

	@Required
	public void setStudyConsumer(PSCStudyConsumer studyConsumer) {
		this.studyConsumer = studyConsumer;
	}

	@Required
	public void setStudyService(StudyService studyService) {
		this.studyService = studyService;
	}

}
