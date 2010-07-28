package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.SecurityContextHolderTestHelper;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.StudySiteService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.TemplateAvailability;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.UserTemplateRelationship;
import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;
import static java.util.Arrays.*;
import static org.easymock.classextension.EasyMock.*;

/**
 * @author Rhett Sutphin
 */
public class StudyListControllerTest extends ControllerTestCase {
    private static final int COMPLETE_ID = 14;
    private static final int INCOMPLETE_ID = 37;
    private static final Integer BOTH_ID = 44;

    private StudyListController controller;

    private TemplateService templateService;

    private PscUser user;
    private Study completeStudy, incompleteStudy, bothStudy;
    private UserTemplateRelationship complete, incomplete, both;
    private List<Study> allStudies;
    private StudySiteService studySiteService;
    private List<List<StudySite>> allStudySites;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        controller = new StudyListController();
        StudyDao studyDao = registerDaoMockFor(StudyDao.class);
        templateService = registerMockFor(TemplateService.class);
        studySiteService = registerMockFor(StudySiteService.class);

        controller.setStudyDao(studyDao);
        controller.setTemplateService(templateService);
        controller.setStudySiteService(studySiteService);
        controller.setApplicationSecurityManager(applicationSecurityManager);

        user = AuthorizationObjectFactory.createPscUser("jimbo", new PscRole[0]);
        SecurityContextHolderTestHelper.setSecurityContext(user, "password");

        completeStudy = setId(COMPLETE_ID, createSingleEpochStudy("Complete", "E1"));
        completeStudy.setAmendment(new Amendment());
        complete = new UserTemplateRelationship(user, completeStudy);

        incompleteStudy = setId(INCOMPLETE_ID, createSingleEpochStudy("Incomplete", "E1"));
        incompleteStudy.setAmendment(null);
        incompleteStudy.setDevelopmentAmendment(new Amendment());
        incomplete = new UserTemplateRelationship(user, incompleteStudy);

        bothStudy = setId(BOTH_ID, createSingleEpochStudy("Available but amending", "E1"));
        bothStudy.setDevelopmentAmendment(new Amendment());
        bothStudy.setAmendment(new Amendment());
        both = new UserTemplateRelationship(user, bothStudy);

        allStudies = asList(incompleteStudy, bothStudy, completeStudy);
        allStudySites = asList(
                asList(createStudySite(incompleteStudy, null)),
                asList(createStudySite(completeStudy, null)),
                asList(createStudySite(bothStudy, null))
        );
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        DataAuditInfo.setLocal(null);
    }

    public void testAuthorizedRoles() {
        Collection<ResourceAuthorization> actualAuthorizations = controller.authorizations(null, null);
        assertRolesAllowed(actualAuthorizations,
                DATA_IMPORTER,
                STUDY_QA_MANAGER, STUDY_TEAM_ADMINISTRATOR,
                STUDY_SITE_PARTICIPATION_ADMINISTRATOR,
                STUDY_CREATOR,
                STUDY_CALENDAR_TEMPLATE_BUILDER,
                STUDY_SUBJECT_CALENDAR_MANAGER,
                DATA_READER);
    }

    public void testModelAndView() throws Exception {
        expect(templateService.getVisibleTemplates(user)).andReturn(
            new MapBuilder<TemplateAvailability, List<UserTemplateRelationship>>().
                put(TemplateAvailability.IN_DEVELOPMENT, Arrays.asList(incomplete, both)).
                put(TemplateAvailability.PENDING, Collections.singletonList(complete)).
                put(TemplateAvailability.AVAILABLE, Collections.singletonList(both)).
                toMap());
        expect(studySiteService.refreshStudySitesForStudies(allStudies)).andReturn(allStudySites);

        replayMocks();
        ModelAndView actual = controller.handleRequest(request, response);
        verifyMocks();

        assertEquals("Wrong in development templates",
            Arrays.asList(incomplete, both), actual.getModel().get("inDevelopmentTemplates"));
        assertEquals("Wrong pending templates",
            Arrays.asList(complete), actual.getModel().get("pendingTemplates"));
        assertEquals("Wrong available templates",
            Arrays.asList(both), actual.getModel().get("availableTemplates"));

        assertEquals("Wrong view", "studyList", actual.getViewName());
    }
}
