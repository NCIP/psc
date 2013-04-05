/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.WorkflowService;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.StudyWorkflowStatus;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.WorkflowMessage;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import org.springframework.context.ApplicationContext;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;

import java.util.Collections;

import static org.easymock.EasyMock.isNull;
import static org.easymock.classextension.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
public class EditControllerTest extends ControllerTestCase {
    private EditController controller;
    private EditTemplateCommand command;
    private WorkflowService workflowService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        StudyDao studyDao = registerDaoMockFor(StudyDao.class);
        EpochDao epochDao = registerDaoMockFor(EpochDao.class);
        StudySegmentDao studySegmentDao = registerDaoMockFor(StudySegmentDao.class);
        ApplicationContext applicationContext = registerMockFor(ApplicationContext.class);
        workflowService = registerMockFor(WorkflowService.class);
        ActivityDao activityDao = registerDaoMockFor(ActivityDao.class);
        PopulationDao populationDao = registerDaoMockFor(PopulationDao.class);
        PeriodDao periodDao = registerDaoMockFor(PeriodDao.class);
        command = registerMockFor(EditTemplateCommand.class);

        controller = new EditController();
        controller.setStudySegmentDao(studySegmentDao);
        controller.setEpochDao(epochDao);
        controller.setStudyDao(studyDao);
        controller.setApplicationContext(applicationContext);
        controller.setPopulationDao(populationDao);
        controller.setActivityDao(activityDao);
        controller.setPeriodDao(periodDao);
        controller.setControllerTools(controllerTools);
        controller.setCommandBeanName("mockCommandBean");
        controller.setWorkflowService(workflowService);
        controller.setApplicationSecurityManager(applicationSecurityManager);
        expect(applicationContext.getBean("mockCommandBean")).andReturn(command).anyTimes();
    }

    @SuppressWarnings({ "unchecked" })
    public void testHandle() throws Exception {
        StudyWorkflowStatus studyWorkflowStatus = registerMockFor(StudyWorkflowStatus.class);
        expect(workflowService.build((Study) isNull(), (PscUser) isNull())).andReturn(studyWorkflowStatus);
        expect(studyWorkflowStatus.getMessagesIgnoringRevisionMessages()).andReturn(null).anyTimes();
        expect(studyWorkflowStatus.isRevisionComplete()).andReturn(true).anyTimes();

        expect(studyWorkflowStatus.getMessages()).andReturn(Collections.<WorkflowMessage>emptyList());

        expect(command.apply()).andReturn(true);
        expect(command.getModel()).andReturn(new ModelMap("foo", 95));
        expect(command.getRelativeViewName()).andReturn("pony");

        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        assertEquals("template/ajax/pony", mv.getViewName());
        assertContainsPair(mv.getModel(), "foo", 95);
    }
    
    public void testHandleGetIsError() throws Exception {
        request.setMethod("GET");
        replayMocks();

        assertNull(controller.handleRequest(request, response));
        verifyMocks();

        assertEquals("Wrong HTTP status code", HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        assertEquals("Wrong error message", "POST is the only valid method for this URL", response.getErrorMessage());
    }
}
