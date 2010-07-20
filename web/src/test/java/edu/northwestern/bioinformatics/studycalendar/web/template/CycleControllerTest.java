package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.StudySegmentDao;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.service.TestingTemplateService;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.easymock.classextension.EasyMock;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collection;
import java.util.Map;

/**
 * @author Jalpa Patel
 */
public class CycleControllerTest extends ControllerTestCase {
    private CycleController controller;
    private StudySegment studySegment;
    private StudySegmentDao studySegmentDao;
    private CycleCommand command;
    private BindException errors;

    public void setUp() throws Exception {
        super.setUp();
        studySegmentDao = registerDaoMockFor(StudySegmentDao.class);

        controller = new CycleController();
        controller.setStudySegmentDao(studySegmentDao);
        controller.setControllerTools(controllerTools);
        controller.setTemplateService(new TestingTemplateService());

        Study study = createBasicTemplate();
        study.setId(11);
        assignIds(study);

        study.setDevelopmentAmendment(setId(4, new Amendment()));
        studySegment = study.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(0);


        command = registerMockFor(CycleCommand.class);
        errors = new BindException(command, "command");
        EasyMock.expect(command.getStudySegment()).andStubReturn(studySegment);
    }

    public void testAuthorizedRoles() {
        Collection<ResourceAuthorization> actualAuthorizations = controller.authorizations(null, null);
        assertRolesAllowed(actualAuthorizations, STUDY_CALENDAR_TEMPLATE_BUILDER);
    }

    private ModelAndView doHandle() throws Exception {
        replayMocks();
        ModelAndView mv = controller.handle(command, errors, request, response);
        verifyMocks();
        return mv;
    }

    @SuppressWarnings({"unchecked"})
    private Map<String, Object> doHandleAndReturnModel() throws Exception {
        return (Map<String, Object>) doHandle().getModel();
    }

    public void testHandleAppliesCommand() throws Exception {
        command.apply();
        doHandle();
    }

    public void testHandleDoesNotCallApplyIfThereAreErrors() throws Exception {
        errors.reject("cycleLength", "A");
        // expect apply not called
        doHandle();
    }

    public void testRedirectsToTemplateDisplay() throws Exception {
        command.apply();

        ModelAndView actual = doHandle();
        assertEquals("redirectToCalendarTemplate", actual.getViewName());
    }

    public void testRedirectsToCorrectTemplate() throws Exception {
        Study study = studySegment.getEpoch().getPlannedCalendar().getStudy();
        Integer expectedId = study.getId();
        Integer expectedAmendmentId = study.getDevelopmentAmendment().getId();
        command.apply();

        Map<String, Object> actualModel = doHandleAndReturnModel();
        assertEquals(expectedId, actualModel.get("study"));
        assertEquals(expectedAmendmentId, actualModel.get("amendment"));
    }

    public void testRedirectsToCorrectStudySegment() throws Exception {
        Integer expectedStudySegmentId = studySegment.getId();
        command.apply();

        Map<String, Object> actualModel = doHandleAndReturnModel();
        assertEquals(actualModel.get("studySegment"), expectedStudySegmentId);
    }
}
