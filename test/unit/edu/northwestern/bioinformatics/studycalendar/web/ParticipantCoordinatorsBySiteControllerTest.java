package edu.northwestern.bioinformatics.studycalendar.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.easymock.classextension.EasyMock;
import org.springframework.web.servlet.ModelAndView;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarAuthorizationManager;
import gov.nih.nci.security.authorization.domainobjects.User;

public class ParticipantCoordinatorsBySiteControllerTest extends ControllerTestCase {

	private ParticipantCoordinatorsBySiteController controller;
	private TemplateService templateService;
	private SiteDao siteDao;
	private StudyDao studyDao;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		templateService = registerMockFor(TemplateService.class);
		siteDao = registerDaoMockFor(SiteDao.class);
		studyDao = registerDaoMockFor(StudyDao.class);
		
		controller = new ParticipantCoordinatorsBySiteController();
		
		controller.setSiteDao(siteDao);
		controller.setTemplateService(templateService);
		controller.setStudyDao(studyDao);
		
	}
	
	public void testHandleRequest() throws Exception {
		Site site = Fixtures.createNamedInstance("gumby", Site.class);
		Study study = new Study();
		List<User> assignedUsers = new ArrayList<User>();
		List<User> availableUsers = new ArrayList<User>();
		Map<String, List<User>> map = new HashMap<String, List<User>>();
		map.put(StudyCalendarAuthorizationManager.ASSIGNED_USERS, assignedUsers);
		map.put(StudyCalendarAuthorizationManager.AVAILABLE_USERS, availableUsers);
		request.addParameter("site", "-1");
		request.addParameter("study", "-2");
		
		EasyMock.expect(studyDao.getById(-2)).andReturn(study);
		EasyMock.expect(siteDao.getById(-1)).andReturn(site);
		EasyMock.expect(templateService.getParticipantCoordinators(study, site)).andReturn(map);
		
		replayMocks();
		
		ModelAndView actual = controller.handleRequest(request, response);
		
		verifyMocks();
		
		assertSame("Assigned users not present", assignedUsers, actual.getModel().get("assigned"));
		assertSame("Available users not present", availableUsers, actual.getModel().get("available"));
		assertSame("Site not present", site, actual.getModel().get("site"));
		assertEquals("Wrong view name", "admin/ajax/participantCoordinatorsBySite", actual.getViewName());
	}

}
