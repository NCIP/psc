package edu.northwestern.bioinformatics.studycalendar.web.reporting;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.easymock.classextension.EasyMock;
import org.springframework.web.servlet.ModelAndView;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
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
		sites.add(Fixtures.createNamedInstance("Boston", Site.class));
		Site westCoastSite = Fixtures.createNamedInstance("Seattle", Site.class);
		
		EasyMock.expect(siteDao.getById(1)).andReturn(sites.get(0));
		EasyMock.expect(siteDao.getById(2)).andReturn(sites.get(1));
		
		List<Study> studies = new ArrayList<Study>();
		String[] armNames = {"Arm1", "Arm2"};
		studies.add(Fixtures.createSingleEpochStudy("Frank", "Treatment", armNames));
		studies.add(Fixtures.createSingleEpochStudy("Norbert", "Treatment", armNames));
		studies.add(Fixtures.createSingleEpochStudy("Lucy", "Treatment", armNames));
		studies.add(Fixtures.createSingleEpochStudy("Tom", "Treatment", armNames));
		
		List<StudySite> studySites = new ArrayList<StudySite>();
		studySites.add(Fixtures.createStudySite(studies.get(0), sites.get(0)));
		studySites.add(Fixtures.createStudySite(studies.get(1), sites.get(0)));
		studySites.add(Fixtures.createStudySite(studies.get(2), westCoastSite));
		studySites.add(Fixtures.createStudySite(studies.get(3), sites.get(1)));
		
		
		replayMocks();
		String[] siteIds = {"1","2"};
		request.addParameter("sites", siteIds);
		ModelAndView actual = controller.handleRequest(request, response);
		
		verifyMocks();
		
		Set<Study> studiesInModel = (Set<Study>) actual.getModel().get("studies");
		for(Study study : studiesInModel) {
			assertNotSame("Found a study from wrong site.", studies.get(2), study);
		}
		assertEquals("Wrong view name", "reporting/ajax/studiesBySites", actual.getViewName());
	}

}
