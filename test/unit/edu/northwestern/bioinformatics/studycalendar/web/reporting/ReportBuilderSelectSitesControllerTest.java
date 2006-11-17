package edu.northwestern.bioinformatics.studycalendar.web.reporting;

import java.util.ArrayList;
import java.util.List;
import org.easymock.classextension.EasyMock;
import org.springframework.web.servlet.ModelAndView;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;

/**
 * @author Jaron Sampson
 *
 */
public class ReportBuilderSelectSitesControllerTest extends ControllerTestCase {
	private ReportBuilderSelectSitesController controller;
    private StudyDao studyDao;
    private TemplateService templateService;


	protected void setUp() throws Exception {
		super.setUp();

		templateService = registerMockFor(TemplateService.class);
		studyDao = registerDaoMockFor(StudyDao.class);
		
		controller = new ReportBuilderSelectSitesController();
		
		controller.setTemplateService(templateService);
		controller.setStudyDao(studyDao);

	}
	
	public void testHandleRequest() throws Exception {
		List<Study> studies = new ArrayList<Study>();
		EasyMock.expect(studyDao.getAll()).andReturn(studies);
		//TODO: Fix this so that it the ASM.getUser request doesn't leave a null here - jsampson
//		EasyMock.expect(templateService.checkOwnership(null, studies)).andReturn(studies);
		
		replayMocks();
		
		ModelAndView actual = controller.handleRequest(request, response);
		
		verifyMocks();
		
		assertSame("Studies list not present", studies, actual.getModel().get("studies"));
		assertEquals("Wrong view name", "reporting/ajax/studiesBySites", actual.getViewName());
	}

}
