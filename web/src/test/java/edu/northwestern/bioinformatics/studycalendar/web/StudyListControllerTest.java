package edu.northwestern.bioinformatics.studycalendar.web;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.SecurityContextHolderTestHelper;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.service.StudySiteService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.DevelopmentTemplate;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.ReleasedTemplate;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;
import static org.easymock.EasyMock.notNull;
import static org.easymock.classextension.EasyMock.expect;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;
import static java.util.Arrays.asList;

/**
 * @author Rhett Sutphin
 */
public class StudyListControllerTest extends ControllerTestCase {
    private static final int COMPLETE_ID = 14;
    private static final int INCOMPLETE_ID = 37;
    private static final Integer BOTH_ID = 44;

    private StudyListController controller;

    private StudyDao studyDao;
    private UserDao userDao;
    private TemplateService templateService;

    private User user;
    private Study complete;
    private Study incomplete;
    private Study both;
    private List<Study> allStudies;
    private StudySiteService studySiteService;
    private List<List<StudySite>> allStudySites;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        controller = new StudyListController();
        studyDao = registerDaoMockFor(StudyDao.class);
        userDao = registerDaoMockFor(UserDao.class);
        templateService = registerMockFor(TemplateService.class);
        studySiteService = registerMockFor(StudySiteService.class);

        controller.setStudyDao(studyDao);
        controller.setUserDao(userDao);
        controller.setTemplateService(templateService);
        controller.setStudySiteService(studySiteService);
        controller.setApplicationSecurityManager(applicationSecurityManager);

        user = createUser("jimbo");
        SecurityContextHolderTestHelper.setSecurityContext(user, "password");

        complete = setId(COMPLETE_ID, createSingleEpochStudy("Complete", "E1"));
        complete.setAmendment(new Amendment());

        incomplete = setId(INCOMPLETE_ID, createSingleEpochStudy("Incomplete", "E1"));
        incomplete.setAmendment(null);
        incomplete.setDevelopmentAmendment(new Amendment());

        both = setId(BOTH_ID, createSingleEpochStudy("Available but amending", "E1"));
        both.setDevelopmentAmendment(new Amendment());
        both.setAmendment(new Amendment());

        allStudies = asList(incomplete, complete, both);
        allStudySites = asList(
                asList(createStudySite(incomplete, null)),
                asList(createStudySite(complete, null)),
                asList(createStudySite(both, null))
        );
        expect(studyDao.getAll()).andReturn(allStudies).anyTimes();
        expect(studySiteService.refreshStudySitesForStudies((List<Study>) notNull())).andReturn(allStudySites).anyTimes();

//        expect(templateService.filterForVisibility(allStudies, null))
//            .andReturn(Collections.<Study>emptyList()).anyTimes();

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

    public void testModelAndViewForStudyAndSubjectCoordinator() throws Exception {
        setUserRoles(user, Role.SUBJECT_COORDINATOR, Role.STUDY_COORDINATOR);

        List<DevelopmentTemplate> inDevelopment = new ArrayList<DevelopmentTemplate>();
        inDevelopment.add(0, (new DevelopmentTemplate(incomplete)));
        inDevelopment.add(1, (new DevelopmentTemplate(both)));
        List<ReleasedTemplate> releasedTemplates = new ArrayList<ReleasedTemplate>();
        releasedTemplates.add(new ReleasedTemplate(complete, true));
        releasedTemplates.add(new ReleasedTemplate(both, true));
        List<ReleasedTemplate> pendingTemplates = new ArrayList<ReleasedTemplate>();
        List<ReleasedTemplate> releasedAndAssignedTemplates = new ArrayList<ReleasedTemplate>();

        expect(templateService.getInDevelopmentTemplates(allStudies, user)).andReturn(inDevelopment);
        expect(templateService.getPendingTemplates(allStudies, user)).andReturn(pendingTemplates);
        expect(templateService.getReleasedAndAssignedTemplates(allStudies, user)).andReturn(releasedAndAssignedTemplates);
        expect(templateService.getReleasedTemplates(allStudies, user)).andReturn(releasedTemplates);

        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        assertDevelopmentTemplateList(asList(incomplete, both), mv);
        assertReleasedTemplateList(asList(complete, both), new boolean[] { true, true }, mv);
        assertEquals("studyList", mv.getViewName());
    }

    public void testModelForSubjectCoordinatorAndResearchAssociate() throws Exception {
        setUserRoles(user, Role.SUBJECT_COORDINATOR);

        List<DevelopmentTemplate> inDevelopment = new ArrayList<DevelopmentTemplate>();
        List<ReleasedTemplate> releasedTemplates = new ArrayList<ReleasedTemplate>();
        releasedTemplates.add(new ReleasedTemplate(complete, true));
        releasedTemplates.add(new ReleasedTemplate(both, false));
        List<ReleasedTemplate> pendingTemplates = new ArrayList<ReleasedTemplate>();
        List<ReleasedTemplate> releasedAndAssignedTemplates = new ArrayList<ReleasedTemplate>();

        expect(templateService.getInDevelopmentTemplates(allStudies, user)).andReturn(inDevelopment);
        expect(templateService.getPendingTemplates(allStudies, user)).andReturn(pendingTemplates);
        expect(templateService.getReleasedAndAssignedTemplates(allStudies, user)).andReturn(releasedAndAssignedTemplates);
        expect(templateService.getReleasedTemplates(allStudies, user)).andReturn(releasedTemplates);

        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        System.out.println("mv " + mv);
        verifyMocks();

        assertDevelopmentTemplateList(Collections.<Study>emptyList(), mv);
        assertReleasedTemplateList(asList(complete, both), new boolean[] { true, false }, mv);
    }
    
    public void testModelForSiteCoordinator() throws Exception {
        setUserRoles(user, Role.SITE_COORDINATOR);

        List<DevelopmentTemplate> inDevelopment = new ArrayList<DevelopmentTemplate>();
        List<ReleasedTemplate> releasedTemplates = new ArrayList<ReleasedTemplate>();
        releasedTemplates.add(new ReleasedTemplate(complete, false));
        releasedTemplates.add(new ReleasedTemplate(both, false));
        List<ReleasedTemplate> pendingTemplates = new ArrayList<ReleasedTemplate>();
        List<ReleasedTemplate> releasedAndAssignedTemplates = new ArrayList<ReleasedTemplate>();

        expect(templateService.getInDevelopmentTemplates(allStudies, user)).andReturn(inDevelopment);
        expect(templateService.getPendingTemplates(allStudies, user)).andReturn(pendingTemplates);
        expect(templateService.getReleasedAndAssignedTemplates(allStudies, user)).andReturn(releasedAndAssignedTemplates);
        expect(templateService.getReleasedTemplates(allStudies, user)).andReturn(releasedTemplates);

        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        assertDevelopmentTemplateList(Collections.<Study>emptyList(), mv);
        assertReleasedTemplateList(asList(complete, both), new boolean[] { false, false }, mv);
    }

    @SuppressWarnings({ "unchecked" })
    private void assertDevelopmentTemplateList(List<Study> expected, ModelAndView mv) {
        assertDevelopmentTemplateList(expected,
            (List<DevelopmentTemplate>) mv.getModel().get("inDevelopmentTemplates"));
    }

    private void assertDevelopmentTemplateList(List<Study> expected, List<DevelopmentTemplate> actual) {
        assertEquals("Wrong number of development templates", expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals("Dev template mismatch at index " + i, (int) expected.get(i).getId(), actual.get(i).getId());
        }
    }

    @SuppressWarnings({ "unchecked" })
    private void assertReleasedTemplateList(List<Study> expected, boolean[] expectedAssignable, ModelAndView mv) {
        assertReleasedTemplateList(expected, expectedAssignable, (List<ReleasedTemplate>) mv.getModel().get("releasedTemplates"));
    }

    private void assertReleasedTemplateList(List<Study> expected, boolean[] expectedAssignable, List<ReleasedTemplate> actual) {
        assertEquals("Wrong number of released templates", expected.size(), actual.size());
        assertEquals("Test setup failure", expected.size(), expectedAssignable.length);
        for (int i = 0; i < expected.size(); i++) {
            assertEquals("Released template mismatch at index " + i,
                (int) expected.get(i).getId(), actual.get(i).getId());
            assertEquals("Released template assignable flag mismatch at " + i,
                expectedAssignable[i], actual.get(i).getCanAssignSubjects());
        }
    }
}
