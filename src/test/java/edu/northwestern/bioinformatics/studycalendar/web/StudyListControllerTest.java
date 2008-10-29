package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import static edu.northwestern.bioinformatics.studycalendar.test.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.SecurityContextHolderTestHelper;
import static org.easymock.classextension.EasyMock.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo;

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

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        controller = new StudyListController();
        studyDao = registerDaoMockFor(StudyDao.class);
        userDao = registerDaoMockFor(UserDao.class);
        templateService = registerMockFor(TemplateService.class);

        controller.setStudyDao(studyDao);
        controller.setUserDao(userDao);
        controller.setTemplateService(templateService);

        user = createUser("jimbo");
        SecurityContextHolderTestHelper.setSecurityContext("jimbo", "password");
        expect(userDao.getByName("jimbo")).andReturn(user);

        complete = setId(COMPLETE_ID, createSingleEpochStudy("Complete", "E1"));
        complete.setAmendment(new Amendment());

        incomplete = setId(INCOMPLETE_ID, createSingleEpochStudy("Incomplete", "E1"));
        incomplete.setAmendment(null);
        incomplete.setDevelopmentAmendment(new Amendment());

        both = setId(BOTH_ID, createSingleEpochStudy("Available but amending", "E1"));
        both.setDevelopmentAmendment(new Amendment());
        both.setAmendment(new Amendment());

        allStudies = Arrays.asList(incomplete, complete, both);
        expect(studyDao.getAll()).andReturn(allStudies).anyTimes();

//        expect(templateService.filterForVisibility(allStudies, null))
//            .andReturn(Collections.<Study>emptyList()).anyTimes();

    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        DataAuditInfo.setLocal(null);
    }

    public void testModelAndViewForStudyAndSubjectCoordinator() throws Exception {
        setUserRoles(user, Role.SUBJECT_COORDINATOR, Role.STUDY_COORDINATOR);

        List<StudyListController.DevelopmentTemplate> inDevelopment = new ArrayList<StudyListController.DevelopmentTemplate>();
        inDevelopment.add(0, (new StudyListController.DevelopmentTemplate(incomplete)));
        inDevelopment.add(1, (new StudyListController.DevelopmentTemplate(both)));
        List<StudyListController.ReleasedTemplate> releasedTemplates = new ArrayList<StudyListController.ReleasedTemplate>();
        releasedTemplates.add(new StudyListController.ReleasedTemplate(complete, true));
        releasedTemplates.add(new StudyListController.ReleasedTemplate(both, true));
        List<StudyListController.ReleasedTemplate> pendingTemplates = new ArrayList<StudyListController.ReleasedTemplate>();
        List<StudyListController.ReleasedTemplate> releasedAndAssignedTemplates = new ArrayList<StudyListController.ReleasedTemplate>();

        expect(templateService.getInDevelopmentTemplates(allStudies, user)).andReturn(inDevelopment);
        expect(templateService.getPendingTemplates(allStudies, user)).andReturn(pendingTemplates);
        expect(templateService.getReleasedAndAssignedTemplates(allStudies, user)).andReturn(releasedAndAssignedTemplates);
        expect(templateService.getReleasedTemplates(allStudies, user)).andReturn(releasedTemplates);

        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        assertDevelopmentTemplateList(Arrays.asList(incomplete, both), mv);
        assertReleasedTemplateList(Arrays.asList(complete, both), new boolean[] { true, true }, mv);
        assertEquals("studyList", mv.getViewName());
    }

    public void testModelForSubjectCoordinatorAndResearchAssociate() throws Exception {
        setUserRoles(user, Role.SUBJECT_COORDINATOR);

        List<StudyListController.DevelopmentTemplate> inDevelopment = new ArrayList<StudyListController.DevelopmentTemplate>();
        List<StudyListController.ReleasedTemplate> releasedTemplates = new ArrayList<StudyListController.ReleasedTemplate>();
        releasedTemplates.add(new StudyListController.ReleasedTemplate(complete, true));
        releasedTemplates.add(new StudyListController.ReleasedTemplate(both, false));
        List<StudyListController.ReleasedTemplate> pendingTemplates = new ArrayList<StudyListController.ReleasedTemplate>();
        List<StudyListController.ReleasedTemplate> releasedAndAssignedTemplates = new ArrayList<StudyListController.ReleasedTemplate>();

        expect(templateService.getInDevelopmentTemplates(allStudies, user)).andReturn(inDevelopment);
        expect(templateService.getPendingTemplates(allStudies, user)).andReturn(pendingTemplates);
        expect(templateService.getReleasedAndAssignedTemplates(allStudies, user)).andReturn(releasedAndAssignedTemplates);
        expect(templateService.getReleasedTemplates(allStudies, user)).andReturn(releasedTemplates);

        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        System.out.println("mv " + mv);
        verifyMocks();

        assertDevelopmentTemplateList(Collections.<Study>emptyList(), mv);
        assertReleasedTemplateList(Arrays.asList(complete, both), new boolean[] { true, false }, mv);
    }
    
    public void testModelForSiteCoordinator() throws Exception {
        setUserRoles(user, Role.SITE_COORDINATOR);

        List<StudyListController.DevelopmentTemplate> inDevelopment = new ArrayList<StudyListController.DevelopmentTemplate>();
        List<StudyListController.ReleasedTemplate> releasedTemplates = new ArrayList<StudyListController.ReleasedTemplate>();
        releasedTemplates.add(new StudyListController.ReleasedTemplate(complete, false));
        releasedTemplates.add(new StudyListController.ReleasedTemplate(both, false));
        List<StudyListController.ReleasedTemplate> pendingTemplates = new ArrayList<StudyListController.ReleasedTemplate>();
        List<StudyListController.ReleasedTemplate> releasedAndAssignedTemplates = new ArrayList<StudyListController.ReleasedTemplate>();

        expect(templateService.getInDevelopmentTemplates(allStudies, user)).andReturn(inDevelopment);
        expect(templateService.getPendingTemplates(allStudies, user)).andReturn(pendingTemplates);
        expect(templateService.getReleasedAndAssignedTemplates(allStudies, user)).andReturn(releasedAndAssignedTemplates);
        expect(templateService.getReleasedTemplates(allStudies, user)).andReturn(releasedTemplates);

        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        assertDevelopmentTemplateList(Collections.<Study>emptyList(), mv);
        assertReleasedTemplateList(Arrays.asList(complete, both), new boolean[] { false, false }, mv);
    }

    @SuppressWarnings({ "unchecked" })
    private void assertDevelopmentTemplateList(List<Study> expected, ModelAndView mv) {
        assertDevelopmentTemplateList(expected,
            (List<StudyListController.DevelopmentTemplate>) mv.getModel().get("inDevelopmentTemplates"));
    }

    private void assertDevelopmentTemplateList(List<Study> expected, List<StudyListController.DevelopmentTemplate> actual) {
        assertEquals("Wrong number of development templates", expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals("Dev template mismatch at index " + i, (int) expected.get(i).getId(), actual.get(i).getId());
        }
    }

    @SuppressWarnings({ "unchecked" })
    private void assertReleasedTemplateList(List<Study> expected, boolean[] expectedAssignable, ModelAndView mv) {
        assertReleasedTemplateList(expected, expectedAssignable, (List<StudyListController.ReleasedTemplate>) mv.getModel().get("releasedTemplates"));
    }

    private void assertReleasedTemplateList(List<Study> expected, boolean[] expectedAssignable, List<StudyListController.ReleasedTemplate> actual) {
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
