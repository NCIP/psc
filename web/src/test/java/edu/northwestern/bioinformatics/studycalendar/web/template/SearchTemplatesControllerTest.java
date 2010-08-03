package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.SecurityContextHolderTestHelper;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.StudyWorkflowStatus;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.TemplateAvailability;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.WorkflowMessageFactory;
import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collections;
import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createBasicTemplate;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.easymock.classextension.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
public class SearchTemplatesControllerTest extends ControllerTestCase {
    private SearchTemplatesController controller;

    private StudyService studyService;
    private TemplateService mockTemplateService;
    private PscUser user;
    private Study a, d, p, r;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mockTemplateService = registerMockFor(TemplateService.class);
        studyService = registerMockFor(StudyService.class);
        user = AuthorizationObjectFactory.createPscUser("jo", new PscRole[0]);
        SecurityContextHolderTestHelper.setSecurityContext(user, "dc");

        controller = new SearchTemplatesController();
        controller.setApplicationSecurityManager(applicationSecurityManager);
        controller.setStudyService(studyService);
        controller.setControllerTools(controllerTools);

        a = createBasicTemplate("A");
        d = createBasicTemplate("D");
        p = createBasicTemplate("P");
        r = createBasicTemplate("R");

        request.setMethod("GET");
    }

    public void testAuthorizations() throws Exception {
        assertRolesAllowed(controller.authorizations("GET", null), PscRole.valuesWithStudyAccess());
    }

    public void testErrorIfNotGET() throws Exception {
        request.setMethod("POST");
        request.addParameter("searchText", "whatever");
        doHandle();
        assertEquals("Wrong status", 400, response.getStatus());
    }

    public void testErrorWithoutParameter() throws Exception {
        doHandle();
        assertEquals("Wrong status", 400, response.getStatus());
        assertEquals("Wrong message", "searchText parameter is required", response.getErrorMessage());
    }

    @SuppressWarnings({ "unchecked" })
    public void testSuccessModel() throws Exception {
        request.addParameter("searchText", "baz");
        expect(studyService.searchVisibleStudies(user, "baz")).andReturn(
            new MapBuilder<TemplateAvailability, List<StudyWorkflowStatus>>().
                put(TemplateAvailability.IN_DEVELOPMENT, singletonList(createResultEntry(user, d))).
                put(TemplateAvailability.PENDING, asList(
                    createResultEntry(user, p),
                    createResultEntry(user, r))).
                put(TemplateAvailability.AVAILABLE, asList(
                    createResultEntry(user, a),
                    createResultEntry(user, r))).
                toMap());

        ModelAndView actual = doHandle();
        assertEquals("Wrong view", "template/ajax/templates", actual.getViewName());

        List<StudyWorkflowStatus> actualDev =
            (List<StudyWorkflowStatus>) actual.getModel().get("inDevelopmentTemplates");
        assertEquals("Wrong number of development templates", 1, actualDev.size());
        assertEquals("Wrong development template", "D", actualDev.get(0).getStudy().getAssignedIdentifier());

        List<StudyWorkflowStatus> actualRel =
            (List<StudyWorkflowStatus>) actual.getModel().get("releasedTemplates");
        assertEquals("Wrong number of released templates: " + actualRel, 3, actualRel.size());
        assertEquals("Wrong 1st released template", "A", actualRel.get(0).getStudy().getAssignedIdentifier());
        assertEquals("Wrong 2nd released template", "P", actualRel.get(1).getStudy().getAssignedIdentifier());
        assertEquals("Wrong 3rd released template", "R", actualRel.get(2).getStudy().getAssignedIdentifier());
    }

    @SuppressWarnings({ "unchecked" })
    public void testReturnsAllVisibleWithBlankSearch() throws Exception {
        request.addParameter("searchText", " ");
        expect(studyService.searchVisibleStudies(user, null)).andReturn(
            new MapBuilder<TemplateAvailability, List<StudyWorkflowStatus>>().
                put(TemplateAvailability.IN_DEVELOPMENT, Collections.<StudyWorkflowStatus>emptyList()).
                put(TemplateAvailability.PENDING, Collections.<StudyWorkflowStatus>emptyList()).
                put(TemplateAvailability.AVAILABLE, asList(
                    createResultEntry(user, a))).
                toMap());

        doHandle();
        // verify mocks is sufficient
    }

    ////// HELPERS

    private StudyWorkflowStatus createResultEntry(PscUser user, Study s) {
        return new StudyWorkflowStatus(s, user, new WorkflowMessageFactory(), Fixtures.getTestingDeltaService());
    }

    private ModelAndView doHandle() throws Exception {
        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();
        return mv;
    }
}
