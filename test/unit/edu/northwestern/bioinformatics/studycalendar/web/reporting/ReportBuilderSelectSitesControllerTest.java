package edu.northwestern.bioinformatics.studycalendar.web.reporting;

import java.util.ArrayList;
import java.util.List;
import org.easymock.classextension.EasyMock;
import org.springframework.web.servlet.ModelAndView;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;

/**
 * @author Jaron Sampson
 *
 */
public class ReportBuilderSelectSitesControllerTest extends ControllerTestCase {
	private ReportBuilderSelectSitesController controller;
    private SiteDao siteDao;


	protected void setUp() throws Exception {
		super.setUp();

		siteDao = registerDaoMockFor(SiteDao.class);
		
		controller = new ReportBuilderSelectSitesController();
		
		controller.setSiteDao(siteDao);

	}
	
	public void testHandleRequest() throws Exception {
		List<Site> sites = new ArrayList<Site>();
		sites.add(Fixtures.createNamedInstance("New York", Site.class));
		sites.add(Fixtures.createNamedInstance("Chicago", Site.class));
		sites.add(Fixtures.createNamedInstance("Boston", Site.class));
		
		EasyMock.expect(siteDao.getById(1)).andReturn(sites.get(0));
		EasyMock.expect(siteDao.getById(2)).andReturn(sites.get(1));;
		EasyMock.expect(siteDao.getById(3)).andReturn(sites.get(2));;
		
		replayMocks();
		String[] siteIds = {"1","2","3"};
		request.addParameter("sites", siteIds);
		ModelAndView actual = controller.handleRequest(request, response);
		
		verifyMocks();
		
		assertEquals("Wrong view name", "reporting/ajax/studiesBySites", actual.getViewName());
	}

}
