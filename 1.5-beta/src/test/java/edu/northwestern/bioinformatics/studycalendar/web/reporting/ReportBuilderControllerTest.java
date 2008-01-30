package edu.northwestern.bioinformatics.studycalendar.web.reporting;


import edu.northwestern.bioinformatics.studycalendar.dao.SubjectDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.reporting.ReportRowDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import static org.easymock.EasyMock.expect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Jaron Sampson
 */
public class ReportBuilderControllerTest extends ControllerTestCase {
	private ReportBuilderController controller;
	private SiteService siteService;
	private SiteDao siteDao;
	private StudyDao studyDao;
	private SubjectDao subjectDao;
	private ReportRowDao reportRowDao;

	protected void setUp() throws Exception {
		super.setUp();

		siteDao = registerDaoMockFor(SiteDao.class);
		studyDao = registerDaoMockFor(StudyDao.class);
		subjectDao = registerDaoMockFor(SubjectDao.class);
		reportRowDao = registerMockFor(ReportRowDao.class);
		siteService = registerMockFor(SiteService.class);
		
		
		controller = new ReportBuilderController();
		controller.setSiteDao(siteDao);
		controller.setStudyDao(studyDao);
		controller.setSubjectDao(subjectDao);
		controller.setReportRowDao(reportRowDao);
		controller.setSiteService(siteService);
	}

	public void testRefData() throws Exception {
        List<Site> sites = new ArrayList<Site>();
        expect(siteService.getSitesForUser(ApplicationSecurityManager.getUser())).andReturn(sites);

		replayMocks();

        Map map = controller.referenceData(request);

        verifyMocks();
        assertSame("Site lists not the same.", sites, map.get("sites"));
	}
	
}
