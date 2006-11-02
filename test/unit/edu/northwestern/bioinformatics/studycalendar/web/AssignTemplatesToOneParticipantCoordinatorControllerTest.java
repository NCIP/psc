package edu.northwestern.bioinformatics.studycalendar.web;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.easymock.classextension.EasyMock;
import static org.easymock.classextension.EasyMock.*;
import static org.easymock.EasyMock.expect;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarAuthorizationManager;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;
import gov.nih.nci.security.authorization.domainobjects.User;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Arrays;



/**
 * @author Yufang Wang
 */
 public class AssignTemplatesToOneParticipantCoordinatorControllerTest extends ControllerTestCase {
 	private AssignTemplatesToOneParticipantCoordinatorController controller = new AssignTemplatesToOneParticipantCoordinatorController();
 	private SiteDao siteDao;
 	private SiteService siteService;
 	private TemplateService templateService;
 	private StudyCalendarAuthorizationManager authorizationManager;
 	
 	protected void setUp() throws Exception {
 		super.setUp();
 		siteDao = registerMockFor(SiteDao.class);
 		controller.setSiteDao(siteDao);
 		siteService = registerMockFor(SiteService.class);
 		controller.setSiteService(siteService);
 		templateService = registerMockFor(TemplateService.class);
 		controller.setTemplateService(templateService);
 	}
 	
 	public void off_testReferenceData() throws Exception {
 		Map<String, Object> refdata = controller.referenceData(request);
 		Site site = (Site)refdata.get("site");
 		assertEquals("Wrong site name", "Here it is", site.getName());
 		User participantcoordinator = (User)refdata.get("participantcoordinator");
 		assertEquals("Wrong participant coordinator userId", "16", participantcoordinator.getUserId());
 		//List<Study> assignedTemplates = (List<Study>)refdata.get("assignedTemplates");
 		//List<Study> availableTemplates = (List<Study>)refdata.get("availableTemplates");
 	}
 	
 	public void off_testOnSubmit() throws Exception {
 		AssignTemplatesToOneParticipantCoordinatorCommand mockCommand = registerMockFor(AssignTemplatesToOneParticipantCoordinatorCommand.class);

 		replayMocks();

 		ModelAndView mv = controller.handleRequest(request, response);
 		verifyMocks();

 		assertEquals("Wrong View", "assignTemplatesToOneParticipantCoordinator", mv.getViewName());
 	}
 }