package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationScopeMappings;
import edu.northwestern.bioinformatics.studycalendar.core.osgi.OsgiLayerTools;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.StudyProvider;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import edu.northwestern.bioinformatics.studycalendar.service.WorkflowService;
import edu.northwestern.bioinformatics.studycalendar.service.dataproviders.StudyConsumer;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.StudyWorkflowStatus;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.UserTemplateRelationship;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.WorkflowMessage;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import edu.northwestern.bioinformatics.studycalendar.web.delta.RevisionChanges;
import gov.nih.nci.cabig.ctms.lang.DateTools;
import gov.nih.nci.cabig.ctms.lang.StaticNowFactory;
import org.springframework.web.servlet.ModelAndView;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import static edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.SecurityContextHolderTestHelper.setSecurityContext;
import static edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.SecurityContextHolderTestHelper.setUserAndReturnMembership;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;
import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.notNull;

/**
 * @author Rhett Sutphin
 */
public class DisplayTemplateControllerTest extends ControllerTestCase {
    private static final String STUDY_NAME = "NU-1066";
    private static final Timestamp NOW = DateTools.createTimestamp(2008, Calendar.SEPTEMBER, 18);

    private DisplayTemplateController controller;

    private StudyDao studyDao;
    private DeltaService deltaService;
    private AmendmentService amendmentService;
    private StudyConsumer studyConsumer;

    private Study study;
    private StudySegment seg0a;
    private StudySegment seg0b;
    private Amendment a1;

    private PscUser user;
    private WorkflowService workflowService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        studyDao = registerDaoMockFor(StudyDao.class);
        deltaService = registerMockFor(DeltaService.class);
        amendmentService = registerMockFor(AmendmentService.class);
        studyConsumer = registerMockFor(StudyConsumer.class);
        workflowService = registerMockFor(WorkflowService.class);
        StaticNowFactory nowFactory = new StaticNowFactory();
        OsgiLayerTools osgiTools = registerMockFor(OsgiLayerTools.class);

        study = setId(100, Fixtures.createBasicTemplate());
        study.setAssignedIdentifier(STUDY_NAME);
        study.setGridId("Eleventy-hundred");
        seg0a = study.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(0);
        seg0b = study.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(1);
        int id = 50;
        for (Epoch epoch : study.getPlannedCalendar().getEpochs()) {
            epoch.setId(id++);
            for (StudySegment studySegment : epoch.getStudySegments()) { studySegment.setId(id++); }
        }

        Amendment a2 = setId(2, createAmendments("A0", "A1", "A2"));
        a1 = setId(1, a2.getPreviousAmendment());
        setId(0, a1.getPreviousAmendment());
        study.setAmendment(a2);
        Site site = Fixtures.createSite("Mayo", "MN004");
        study.addSite(site);
        controller = new DisplayTemplateController();
        controller.setStudyDao(studyDao);
        controller.setDeltaService(deltaService);
        controller.setAmendmentService(amendmentService);
        controller.setControllerTools(controllerTools);
        controller.setApplicationSecurityManager(applicationSecurityManager);
        controller.setNowFactory(nowFactory);
        controller.setStudyConsumer(studyConsumer);
        controller.setOsgiLayerTools(osgiTools);
        controller.setWorkflowService(workflowService);

        request.setMethod("GET");
        request.setParameter("study", study.getId().toString());

        // basic test user can see anything
        user = AuthorizationObjectFactory.createPscUser("jo",
            AuthorizationScopeMappings.createSuiteRoleMembership(PscRole.STUDY_QA_MANAGER).forAllSites(),
            AuthorizationScopeMappings.createSuiteRoleMembership(PscRole.STUDY_TEAM_ADMINISTRATOR).forAllSites());
        setSecurityContext(user);

        expect(osgiTools.getServices(StudyProvider.class)).andStubReturn(Collections.<StudyProvider>emptyList());
        StudyWorkflowStatus studyWorkflowStatus = registerMockFor(StudyWorkflowStatus.class);
        expect(workflowService.build((Study) notNull(), (PscUser) notNull())).andReturn(studyWorkflowStatus);
        expect(studyWorkflowStatus.getMessages()).andReturn(Collections.<WorkflowMessage>emptyList());
        nowFactory.setNowTimestamp(NOW);
        expect(studyWorkflowStatus.getMessagesIgnoringRevisionMessages()).andReturn(null).anyTimes();
        expect(studyWorkflowStatus.isRevisionComplete()).andReturn(true).anyTimes();
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

    public void testGetStudyByStudyId() throws Exception {
        request.setParameter("study", study.getId().toString());
        Map<String, Object> actualModel = getAndReturnModel();
        assertSame(study, actualModel.get("study"));
    }

    public void testModelIncludesUserTemplateRelationship() throws Exception {
        UserTemplateRelationship actual = (UserTemplateRelationship) getAndReturnModel().get("relationship");
        assertNotNull("UTR missing", actual);
        assertSame("UTR is for wrong study", study, actual.getStudy());
        assertSame("UTR is for wrong user", user, actual.getUser());
    }

    @SuppressWarnings({ "unchecked" })
    public void testGetStudyByAssignedIdentifier() throws Exception {
        request.setParameter("study", study.getName());

        expect(studyDao.getByAssignedIdentifier(study.getName())).andReturn(study);
        expect(studyConsumer.refresh(study)).andReturn(study);

        replayMocks();
        Map<String, Object> actualModel = controller.handleRequest(request, response).getModel();
        verifyMocks();

        assertSame(study, actualModel.get("study"));
    }

    @SuppressWarnings({ "unchecked" })
    public void testGetStudyByStudyGridId() throws Exception {
        request.setParameter("study", study.getGridId());

        expect(studyDao.getByAssignedIdentifier(study.getGridId())).andReturn(null);
        expect(studyDao.getByGridId(study.getGridId())).andReturn(study);
        expect(studyConsumer.refresh(study)).andReturn(study);

        replayMocks();
        Map<String, Object> actualModel = controller.handleRequest(request, response).getModel();
        verifyMocks();

        assertSame(study, actualModel.get("study"));
    }

    public void testView() throws Exception {
        assertEquals("template/display", doHandle().getViewName());
    }

    public void testNonStudySegmentModel() throws Exception {
        Map<String, Object> actualModel = getAndReturnModel();
        assertSame(study, actualModel.get("study"));
        assertSame(study.getPlannedCalendar(), actualModel.get("plannedCalendar"));
        assertSame(seg0a.getEpoch(), actualModel.get("epoch"));
        assertFalse(actualModel.containsKey("calendar"));
    }

    public void testStudySegmentIsFirstStudySegmentByDefault() throws Exception {
        Map<String, Object> actualModel = getAndReturnModel();
        assertSame(seg0a, ((StudySegmentTemplate) actualModel.get("studySegment")).getBase());
    }

    public void testStudySegmentRespectsSelectedStudySegmentParameter() throws Exception {
        request.addParameter("studySegment", seg0b.getId().toString());
        Map<String, Object> actualModel = getAndReturnModel();
        assertSame(seg0b, ((StudySegmentTemplate) actualModel.get("studySegment")).getBase());
    }

    public void testStudySegmentIgnoresSelectedStudySegmentParameterIfNotInStudy() throws Exception {
        request.addParameter("studySegment", "234");
        Map<String, Object> actualModel = getAndReturnModel();
        assertSame(seg0a, ((StudySegmentTemplate) actualModel.get("studySegment")).getBase());
    }

    public void testDefaultToPublishedAmendment() throws Exception {
        study.setDevelopmentAmendment(new Amendment("dev"));

        Map<String, Object> actualModel = getAndReturnModel();
        assertSame(study, actualModel.get("study"));
        assertSame(study.getAmendment(), actualModel.get("amendment"));
        assertNull("Development revision not selected, so should not be present",
            actualModel.get("developmentRevision"));
    }

    public void testExceptionIfNoPublishedAmendmentAndNoneSelected() throws Exception {
        study.setDevelopmentAmendment(null);
        study.setAmendment(null);

        try {
            getAndReturnModel();
            fail("Exception not thrown");
        } catch (StudyCalendarSystemException e) {
            assertEquals("No default amendment for " + STUDY_NAME, e.getMessage());
        }
    }

    public void testSelectingPreviouslyPublishedAmendment() throws Exception {
        study.setDevelopmentAmendment(new Amendment("dev"));
        request.setParameter("amendment", "1");
        Study amended = study.transientClone();
        expect(amendmentService.getAmendedStudy(study, a1)).andReturn(amended);

        Map<String, Object> actualModel = getAndReturnModel();
        assertSame(amended, actualModel.get("study"));
        assertSame(a1, actualModel.get("amendment"));
        assertNull("Development revision not selected, so should not be present",
            actualModel.get("developmentRevision"));
    }

    public void testSelectingDevelopmentAmendmentIfPresent() throws Exception {
        Study amended = expectSelectDevelopmentAmendment();

        Map<String, Object> actualModel = getAndReturnModel();
        assertSame(amended, actualModel.get("study"));
        assertSame(amended.getDevelopmentAmendment(), actualModel.get("amendment"));
        assertSame(amended.getDevelopmentAmendment(), actualModel.get("developmentRevision"));
        assertNotNull(actualModel.get("revisionChanges"));
        assertTrue(actualModel.get("revisionChanges") instanceof RevisionChanges);
    }

    public void testSelectDevelopmentAmendmentForInitialCreation() throws Exception {
        study.setAmendment(null);
        Study amended = expectSelectDevelopmentAmendment();

        Map<String, Object> actualModel = getAndReturnModel();

        assertSame(amended, actualModel.get("study"));
        assertSame(amended.getDevelopmentAmendment(), actualModel.get("amendment"));
        assertSame(amended.getDevelopmentAmendment(), actualModel.get("developmentRevision"));
        assertNull("Changes should not be included for initial dev", actualModel.get("revisionChanges"));
    }

    public void testChangesIncludedIfInAmendmentDevelopment() throws Exception {
        Amendment dev = setId(4, new Amendment("dev"));
        request.addParameter("amendment", "4");
        study.setDevelopmentAmendment(dev);
        Study amended = study.transientClone();

        expect(deltaService.revise(study, dev)).andReturn(amended);
        Map<String, Object> actualModel = getAndReturnModel();
        assertSame(amended, actualModel.get("study"));
        assertSame(amended.getDevelopmentAmendment(), actualModel.get("developmentRevision"));
    }

    public void testIncludesTodaysDateInApiFormatForPreview() throws Exception {
        assertEquals("2008-09-18", getAndReturnModel().get("todayForApi"));
    }

    public void testIsForbiddenForNoDevelopmentUserToRequestDevelopmentAmendment() throws Exception {
        setUserAndReturnMembership("jo", PscRole.STUDY_TEAM_ADMINISTRATOR).forAllSites();

        expectSelectDevelopmentAmendment();
        assertForbidden();
    }

    public void testAccessIsForbiddenForUserWithoutAccess() throws Exception {
        setUserAndReturnMembership("jo", PscRole.DATA_READER).forSites(createSite("O", "Other one"));

        assertForbidden();
    }

    public void testStudyCalendarTemplateBuilderCanEditDevelopmentAmendment() throws Exception {
        expectSelectDevelopmentAmendment();
        setUserAndReturnMembership("jo", PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER).
            forAllStudies().forAllSites();

        assertTrue((Boolean) getAndReturnModel().get("canEdit"));
    }

    public void testStudyCalendarTemplateBuilderCannotEditReleasedAmendment() throws Exception {
        setUserAndReturnMembership("jo", PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER).
            forAllStudies().forAllSites();

        assertFalse((Boolean) getAndReturnModel().get("canEdit"));
    }

    public void testDataReaderCannotEditDevelopmentAmendment() throws Exception {
        setUserAndReturnMembership("jo", PscRole.DATA_READER).
            forAllStudies().forAllSites();

        assertFalse((Boolean) getAndReturnModel().get("canEdit"));
    }

    ////// HELPERS

    private Study expectSelectDevelopmentAmendment() {
        request.addParameter("amendment", "4");
        Amendment dev = setId(4, new Amendment("dev"));
        study.setDevelopmentAmendment(dev);
        Study amended = study.transientClone();
        expect(deltaService.revise(study, dev)).andReturn(amended);
        return amended;
    }

    @SuppressWarnings({ "unchecked" })
    private Map<String, Object> getAndReturnModel() throws Exception {
        return (Map<String, Object>) doHandle().getModel();
    }

    private ModelAndView doHandle() throws Exception {
        expect(studyDao.getByAssignedIdentifier(study.getId().toString())).andReturn(null);
        expect(studyDao.getById(study.getId())).andReturn(study);
        expect(studyConsumer.refresh(study)).andReturn(study);

        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();
        return mv;
    }

    private void assertForbidden() throws Exception {
        assertNull("Should be no MV", doHandle());
        assertEquals("Wrong code", 403, response.getStatus());
    }
}
