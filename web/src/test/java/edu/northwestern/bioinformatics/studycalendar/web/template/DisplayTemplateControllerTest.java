package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.StudyProvider;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createAmendments;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.setId;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.SecurityContextHolderTestHelper;
import edu.northwestern.bioinformatics.studycalendar.core.osgi.OsgiLayerTools;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.service.AuthorizationService;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.service.dataproviders.StudyConsumer;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.DevelopmentTemplate;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.ReleasedTemplate;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import edu.northwestern.bioinformatics.studycalendar.web.delta.RevisionChanges;
import gov.nih.nci.cabig.ctms.lang.DateTools;
import gov.nih.nci.cabig.ctms.lang.StaticNowFactory;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;
import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.reset;
import org.springframework.web.servlet.ModelAndView;

import java.sql.Timestamp;
import java.util.*;
import static java.util.Collections.singletonList;
import static java.util.Collections.EMPTY_LIST;

/**
 * @author Rhett Sutphin
 */
public class DisplayTemplateControllerTest extends ControllerTestCase {
    private static final String STUDY_NAME = "NU-1066";
    private static final Timestamp NOW = DateTools.createTimestamp(2008, Calendar.SEPTEMBER, 18);

    private DisplayTemplateController controller;

    private StudyDao studyDao;
    private DeltaService deltaService;
    private AuthorizationService authorizationService;
    private AmendmentService amendmentService;
    private StaticNowFactory nowFactory;
    private StudyConsumer studyConsumer;
    private TemplateService templateService;

    private Study study;
    private List<Study> studies = new ArrayList<Study>();
    private Site site;
    private StudySite studySite;
    private StudySegment seg1, seg0a, seg0b;
    private Amendment a0, a1, a2;
    private User subjectCoord;
    private OsgiLayerTools osgiTools;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        studyDao = registerDaoMockFor(StudyDao.class);
        deltaService = registerMockFor(DeltaService.class);
        authorizationService = registerMockFor(AuthorizationService.class);
        amendmentService = registerMockFor(AmendmentService.class);
        studyConsumer = registerMockFor(StudyConsumer.class);
        nowFactory = new StaticNowFactory();
        templateService = registerMockFor(TemplateService.class);
        osgiTools = registerMockFor(OsgiLayerTools.class);

        study = setId(100, Fixtures.createBasicTemplate());
        study.setAssignedIdentifier(STUDY_NAME);
        studies.add(study);
        seg0a = study.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(0);
        seg0b = study.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(1);
        seg1 =  study.getPlannedCalendar().getEpochs().get(1).getStudySegments().get(0);
        int id = 50;
        for (Epoch epoch : study.getPlannedCalendar().getEpochs()) {
            epoch.setId(id++);
            for (StudySegment studySegment : epoch.getStudySegments()) { studySegment.setId(id++); }
        }

        a2 = setId(2, createAmendments("A0", "A1", "A2"));
        a1 = setId(1, a2.getPreviousAmendment());
        a0 = setId(0, a1.getPreviousAmendment());
        study.setAmendment(a2);
        site = Fixtures.createSite("Site");
        studySite = Fixtures.createStudySite(study, site);
        controller = new DisplayTemplateController();
        controller.setStudyDao(studyDao);
        controller.setDeltaService(deltaService);
        controller.setAuthorizationService(authorizationService);
        controller.setAmendmentService(amendmentService);
        controller.setControllerTools(controllerTools);
        controller.setApplicationSecurityManager(applicationSecurityManager);
        controller.setNowFactory(nowFactory);
        controller.setStudyConsumer(studyConsumer);
        controller.setTemplateService(templateService);
        controller.setOsgiLayerTools(osgiTools);

        request.setMethod("GET");
        request.addParameter("study", study.getId().toString());

        subjectCoord = Fixtures.createUser("john", Role.SUBJECT_COORDINATOR);
        SecurityContextHolderTestHelper.setSecurityContext(subjectCoord, "asdf");

        List<DevelopmentTemplate> inDevelopment = new ArrayList<DevelopmentTemplate>();
        List<ReleasedTemplate> releasedTemplates = new ArrayList<ReleasedTemplate>();
        releasedTemplates.add(new ReleasedTemplate(study, true));
        List<ReleasedTemplate> pendingTemplates = new ArrayList<ReleasedTemplate>();
        List<ReleasedTemplate> releasedAndAssignedTemplates = new ArrayList<ReleasedTemplate>();

        expect(templateService.getInDevelopmentTemplates(studies, subjectCoord)).andReturn(inDevelopment);
        expect(templateService.getPendingTemplates(studies, subjectCoord)).andReturn(pendingTemplates);
        expect(templateService.getReleasedAndAssignedTemplates(studies, subjectCoord)).andReturn(releasedAndAssignedTemplates);
        expect(templateService.getReleasedTemplates(studies, subjectCoord)).andReturn(releasedTemplates);

        expect(authorizationService.filterStudySitesForVisibility(study.getStudySites(), subjectCoord.getUserRole(Role.SUBJECT_COORDINATOR)))
                .andReturn(singletonList(studySite)).anyTimes();
        nowFactory.setNowTimestamp(NOW);
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

    public void testGetStudyByAssignedIdentifier() throws Exception {
        request.addParameter("study", study.getName());
        Map<String, Object> actualModel = getAndReturnModel();
        assertSame(study, actualModel.get("study"));
    }

    public void testGetStudyByStudyId() throws Exception {
        request.addParameter("study", study.getId().toString());
        Map<String, Object> actualModel = getAndReturnModel();
        assertSame(study, actualModel.get("study"));
    }


    public void testView() throws Exception {
        expectGetStudyProviders();
        expect(studyDao.getByAssignedIdentifier(study.getId().toString())).andReturn(null);
        expect(studyDao.getById(study.getId())).andReturn(study);
        expect(studyConsumer.refresh(study)).andReturn(study);
        List<StudySubjectAssignment> expectedAssignments = Arrays.asList(new StudySubjectAssignment(), new StudySubjectAssignment(), new StudySubjectAssignment());
        expect(authorizationService.filterStudySubjectAssignmentsByStudySite(study.getStudySites(), expectedAssignments)).andReturn(expectedAssignments).anyTimes();
        expect(studyDao.getAssignmentsForStudy(study.getId())).andReturn(expectedAssignments);
        replayMocks();
        assertEquals("template/display", controller.handleRequest(request, response).getViewName());
        verifyMocks();
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

    public void testOnStudyAssignmentsIncludedWhenComplete() throws Exception {
        expectGetStudyProviders();
        reset(studyDao);
        expect(studyDao.getByAssignedIdentifier(study.getId().toString())).andReturn(null);
        expect(studyDao.getById(study.getId())).andReturn(study);
        expect(studyConsumer.refresh(study)).andReturn(study);

        List<StudySubjectAssignment> expectedAssignments = Arrays.asList(new StudySubjectAssignment(), new StudySubjectAssignment(), new StudySubjectAssignment());
        expect(authorizationService.filterStudySubjectAssignmentsByStudySite(study.getStudySites(), expectedAssignments)).andReturn(expectedAssignments).anyTimes();
        expect(studyDao.getAssignmentsForStudy(study.getId())).andReturn(expectedAssignments);
        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();
        Map<String, Object> actualModel = (Map<String, Object>) mv.getModel();
        assertEquals(expectedAssignments, actualModel.get("onStudyAssignments"));
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
        Amendment dev = setId(4, new Amendment("dev"));
        study.setDevelopmentAmendment(dev);
        request.setParameter("amendment", "4");
        Study amended = study.transientClone();

        expect(deltaService.revise(study, dev)).andReturn(amended);
        Map<String, Object> actualModel = getAndReturnModel();
        assertSame(amended, actualModel.get("study"));
        assertSame(dev, actualModel.get("amendment"));
        assertSame(amended.getDevelopmentAmendment(), actualModel.get("developmentRevision"));
        assertNotNull(actualModel.get("revisionChanges"));
        assertTrue(actualModel.get("revisionChanges") instanceof RevisionChanges);
    }

    public void testSelectDevelopmentAmendmentForInitialCreation() throws Exception {
        Amendment dev = setId(4, new Amendment("dev"));
        study.setDevelopmentAmendment(dev);
        study.setAmendment(null);
        request.setParameter("amendment", "4");
        Study amended = study.transientClone();

        expect(deltaService.revise(study, dev)).andReturn(amended);
        expect(studyDao.getByAssignedIdentifier(study.getId().toString())).andReturn(null);
        expect(studyDao.getById(study.getId())).andReturn(study);
        expect(studyConsumer.refresh(study)).andReturn(study);
        expectGetStudyProviders();

        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();
        Map<String, Object> actualModel = (Map<String, Object>) mv.getModel();

        assertSame(amended, actualModel.get("study"));
        assertSame(dev, actualModel.get("amendment"));
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

    @SuppressWarnings({ "unchecked" })
    private Map<String, Object> getAndReturnModel() throws Exception {
        expectGetStudyProviders();
        expect(studyDao.getByAssignedIdentifier(study.getId().toString())).andReturn(null);
        expect(studyDao.getById(study.getId())).andReturn(study);
        expect(studyConsumer.refresh(study)).andReturn(study);
        List<StudySubjectAssignment> expectedAssignments = Arrays.asList(new StudySubjectAssignment(), new StudySubjectAssignment(), new StudySubjectAssignment());
        expect(authorizationService.filterStudySubjectAssignmentsByStudySite(study.getStudySites(), expectedAssignments)).andReturn(expectedAssignments).anyTimes();
        expect(studyDao.getAssignmentsForStudy(study.getId())).andReturn(expectedAssignments);
        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();
        return (Map<String, Object>) mv.getModel();
    }

    private void expectGetStudyProviders() {
        expect(osgiTools.getServices(StudyProvider.class)).andReturn(EMPTY_LIST);
    }
}
