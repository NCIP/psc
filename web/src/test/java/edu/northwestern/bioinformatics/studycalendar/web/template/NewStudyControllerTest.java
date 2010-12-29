package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.configuration.Configuration;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateSkeletonCreator;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.setId;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_CREATOR;
import static org.easymock.classextension.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
public class NewStudyControllerTest extends ControllerTestCase {
    private static final int ID = 81;
    private static final int AMENDMENT_ID = 4;

    private NewStudyController controller;
    private StudyService studyService;
    private Configuration configuration;
    private NewStudyCommand command;
    private Study study;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        request.setMethod("GET");
        studyService = registerMockFor(StudyService.class);
        command = registerMockFor(NewStudyCommand.class);
        configuration = registerMockFor(Configuration.class);
        expect(configuration.get(Configuration.ENABLE_CREATING_TEMPLATE)).andStubReturn(true);

        controller = new NewStudyController() {
            @Override
            protected Object getCommand(HttpServletRequest request) throws Exception {
                return command;
            }
        };
        controller.setStudyService(studyService);
        controller.setControllerTools(controllerTools);
        controller.setConfiguration(configuration);

        study = setId(ID, new Study());
        study.setDevelopmentAmendment(setId(AMENDMENT_ID, new Amendment("dev")));
    }

    public void testAuthorizedRoles() {
        Collection<ResourceAuthorization> actualAuthorizations = controller.authorizations(null, null);
        assertRolesAllowed(actualAuthorizations, STUDY_CREATOR);
    }

    @SuppressWarnings( { "unchecked" })
    public void testHandle() throws Exception {
        expect(command.create()).andReturn(study);

        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        assertEquals("redirectToCalendarTemplate", mv.getViewName());
        assertEquals(2, mv.getModel().size());
        assertContainsPair(mv.getModel(), "study", ID);
        assertContainsPair(mv.getModel(), "amendment", AMENDMENT_ID);
    }
    
    public void testBindMode() throws Exception {
        request.setParameter("base", "BLANK");
        command.setBase(TemplateSkeletonCreator.BLANK);
        expect(command.create()).andReturn(study);

        replayMocks();
        controller.handleRequest(request, response);
        verifyMocks();
    }

    public void testBadRequestIfTemplateCreationIsDisabled() throws Exception {
        expect(configuration.get(Configuration.ENABLE_CREATING_TEMPLATE)).andReturn(false);

        replayMocks();
        assertNull("Response should have no MV", controller.handleRequest(request, response));
        verifyMocks();

        assertEquals("Wrong error sent", HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        assertEquals("Wrong error message",
            "UI template creation is disabled.  There should be no links to this page visible.",
            response.getErrorMessage());
    }
}
