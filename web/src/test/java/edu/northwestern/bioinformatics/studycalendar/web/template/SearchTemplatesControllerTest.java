package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.SecurityContextHolderTestHelper;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.TemplateWorkflowStatus;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.UserTemplateRelationship;
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

    private TemplateService mockTemplateService;
    private PscUser user;
    private Study a, d, p, r;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mockTemplateService = registerMockFor(TemplateService.class);
        user = AuthorizationObjectFactory.createPscUser("jo", new PscRole[0]);
        SecurityContextHolderTestHelper.setSecurityContext(user, "dc");

        controller = new SearchTemplatesController();
        controller.setApplicationSecurityManager(applicationSecurityManager);
        controller.setTemplateService(mockTemplateService);
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
        expect(mockTemplateService.searchVisibleTemplates(user, "baz")).andReturn(
            new MapBuilder<TemplateWorkflowStatus, List<UserTemplateRelationship>>().
                put(TemplateWorkflowStatus.IN_DEVELOPMENT, singletonList(new UserTemplateRelationship(user, d))).
                put(TemplateWorkflowStatus.PENDING, asList(
                    new UserTemplateRelationship(user, p),
                    new UserTemplateRelationship(user, r))).
                put(TemplateWorkflowStatus.AVAILABLE, asList(
                    new UserTemplateRelationship(user, a),
                    new UserTemplateRelationship(user, r))).
                toMap());

        ModelAndView actual = doHandle();
        assertEquals("Wrong view", "template/ajax/templates", actual.getViewName());

        List<UserTemplateRelationship> actualDev =
            (List<UserTemplateRelationship>) actual.getModel().get("inDevelopmentTemplates");
        assertEquals("Wrong number of development templates", 1, actualDev.size());
        assertEquals("Wrong development template", "D", actualDev.get(0).getStudy().getAssignedIdentifier());

        List<UserTemplateRelationship> actualRel =
            (List<UserTemplateRelationship>) actual.getModel().get("releasedTemplates");
        assertEquals("Wrong number of released templates: " + actualRel, 3, actualRel.size());
        assertEquals("Wrong 1st released template", "A", actualRel.get(0).getStudy().getAssignedIdentifier());
        assertEquals("Wrong 2nd released template", "P", actualRel.get(1).getStudy().getAssignedIdentifier());
        assertEquals("Wrong 3rd released template", "R", actualRel.get(2).getStudy().getAssignedIdentifier());
    }

    @SuppressWarnings({ "unchecked" })
    public void testReturnsAllVisibleWithBlankSearch() throws Exception {
        request.addParameter("searchText", " ");
        expect(mockTemplateService.searchVisibleTemplates(user, null)).andReturn(
            new MapBuilder<TemplateWorkflowStatus, List<UserTemplateRelationship>>().
                put(TemplateWorkflowStatus.IN_DEVELOPMENT, Collections.<UserTemplateRelationship>emptyList()).
                put(TemplateWorkflowStatus.PENDING, Collections.<UserTemplateRelationship>emptyList()).
                put(TemplateWorkflowStatus.AVAILABLE, asList(
                    new UserTemplateRelationship(user, a))).
                toMap());

        doHandle();
        // verify mocks is sufficient
    }

    ////// HELPERS

    private ModelAndView doHandle() throws Exception {
        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();
        return mv;
    }
}
