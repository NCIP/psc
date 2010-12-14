package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.SecurityContextHolderTestHelper;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.ChangeAction;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Revision;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Occurred;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationScopeMappings;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.StudyWorkflowStatus;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.TemplateAvailability;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.WorkflowMessageFactory;
import edu.nwu.bioinformatics.commons.DateUtils;
import gov.nih.nci.cabig.ctms.lang.DateTools;
import gov.nih.nci.cabig.ctms.lang.StaticNowFactory;
import gov.nih.nci.cabig.ctms.suite.authorization.ProvisioningSession;
import gov.nih.nci.cabig.ctms.suite.authorization.ProvisioningSessionFactory;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import gov.nih.nci.security.authorization.domainobjects.User;
import org.easymock.classextension.EasyMock;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory.createPscUser;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationScopeMappings.createSuiteRoleMembership;
import static org.easymock.EasyMock.*;

// TODO: this test is a mockful mess.  It needs more stubs so that individual cases are clearer.
public class StudyServiceTest extends StudyCalendarTestCase {
    private static final Timestamp NOW = DateTools.createTimestamp(2001, Calendar.FEBRUARY, 4);

    private StudyService service;
    private StudyDao studyDao;
    private SiteDao siteDao;
    private ActivityDao activityDao;
    private Study study;
    StudySubjectAssignment subjectAssignment;
    ScheduledCalendar calendar;
    StaticNowFactory staticNowFactory;
    private DeltaService deltaService;
    private ScheduledActivityDao scheduledActivityDao;
    private NotificationService notificationService;
    private ProvisioningSession pSession;
    private ProvisioningSessionFactory psFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        pSession = registerMockFor(ProvisioningSession.class);
        psFactory = registerMockFor(ProvisioningSessionFactory.class);

        studyDao = registerMockFor(StudyDao.class);
        siteDao = registerMockFor(SiteDao.class);
        deltaService = registerMockFor(DeltaService.class);
        scheduledActivityDao=registerDaoMockFor(ScheduledActivityDao.class);
        activityDao = registerMockFor(ActivityDao.class);
        notificationService=registerMockFor(NotificationService.class);
        staticNowFactory = new StaticNowFactory();
        staticNowFactory.setNowTimestamp(NOW);

        WorkflowService ws = new WorkflowService();
        ws.setApplicationSecurityManager(applicationSecurityManager);
        ws.setWorkflowMessageFactory(new WorkflowMessageFactory());
        ws.setDeltaService(Fixtures.getTestingDeltaService());

        service = new StudyService();
        service.setStudyDao(studyDao);
        service.setSiteDao(siteDao);
        service.setActivityDao(activityDao);
        service.setDeltaService(deltaService);
        service.setNowFactory(staticNowFactory);
        service.setScheduledActivityDao(scheduledActivityDao);
        service.setNotificationService(notificationService);
        service.setApplicationSecurityManager(applicationSecurityManager);
        service.setWorkflowService(ws);
        service.setProvisioningSessionFactory(psFactory);

        study = setId(1 , new Study());

        calendar = new ScheduledCalendar();

        subjectAssignment = new StudySubjectAssignment();
        subjectAssignment.setSubject(createSubject("John", "Doe"));
        subjectAssignment.setScheduledCalendar(calendar);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        applicationSecurityManager.removeUserSession();
    }

    public void testScheduleReconsentAfterScheduledActivityOnOccurredEvent() throws Exception{
        staticNowFactory.setNowTimestamp(DateTools.createTimestamp(2005, Calendar.JULY, 2));

        ScheduledStudySegment studySegment0 = new ScheduledStudySegment();
        studySegment0.addEvent(Fixtures.createScheduledActivityWithStudy("AAA", 2005, Calendar.JULY, 1));
        studySegment0.addEvent(Fixtures.createScheduledActivityWithStudy("BBB", 2005, Calendar.JULY, 2,
                new Occurred(null, DateUtils.createDate(2005, Calendar.JULY, 3))));
        studySegment0.addEvent(Fixtures.createScheduledActivityWithStudy("CCC", 2005, Calendar.JULY, 4));
        studySegment0.addEvent(Fixtures.createScheduledActivityWithStudy("DDD", 2005, Calendar.JULY, 8));
        calendar.addStudySegment(studySegment0);

        ScheduledStudySegment studySegment1 = new ScheduledStudySegment();
        studySegment1.addEvent(Fixtures.createScheduledActivityWithStudy("EEE", 2005, Calendar.AUGUST, 1,
                new Occurred(null, DateUtils.createDate(2005, Calendar.AUGUST, 2))));
        studySegment1.addEvent(edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createScheduledActivityWithStudy("FFF", 2005, Calendar.AUGUST, 3));
        studySegment1.addEvent(Fixtures.createScheduledActivityWithStudy("GGG", 2005, Calendar.AUGUST, 8));
        calendar.addStudySegment(studySegment1);

        List<StudySubjectAssignment> assignments = Collections.singletonList(subjectAssignment);
        expect(studyDao.getAssignmentsForStudy(study.getId())).andReturn(assignments);

        Activity reconsent = setId(1, createNamedInstance("Reconsent", Activity.class));
        expect(activityDao.getByName("Reconsent")).andReturn(reconsent);

        studyDao.save(study);
        scheduledActivityDao.save(isA(ScheduledActivity.class));

        replayMocks();
        service.scheduleReconsent(study, staticNowFactory.getNow(), "Reconsent Details");
        verifyMocks();

        List<ScheduledActivity> list = studySegment0.getActivitiesByDate().get(DateTools.createTimestamp(2005, Calendar.JULY, 4));
        
        assertEquals("Wrong number of events on July 4th", 2, list.size());
        assertEquals("Reconsent Details should be details", "Reconsent Details", list.get(0).getDetails());
        assertEquals("Reconsent should be activity name", "Reconsent", list.get(0).getActivity().getName());
    }

    public void testScheduleReconsentForSecondArmOnSameDayAsScheduledActivity() throws Exception{
        staticNowFactory.setNowTimestamp(DateTools.createTimestamp(2005, Calendar.AUGUST, 3));

        ScheduledStudySegment studySegment0 = new ScheduledStudySegment();
        studySegment0.addEvent(Fixtures.createScheduledActivityWithStudy("AAA", 2005, Calendar.JULY, 1,
                new Occurred(null, DateUtils.createDate(2005, Calendar.JULY, 2))));
        studySegment0.addEvent(Fixtures.createScheduledActivityWithStudy("BBB", 2005, Calendar.JULY, 3));
        studySegment0.addEvent(Fixtures.createScheduledActivityWithStudy("CCC", 2005, Calendar.JULY, 8));
        calendar.addStudySegment(studySegment0);

        ScheduledStudySegment studySegment1 = new ScheduledStudySegment();
        studySegment1.addEvent(Fixtures.createScheduledActivityWithStudy("DDD", 2005, Calendar.AUGUST, 1,
                new Occurred(null, DateUtils.createDate(2005, Calendar.AUGUST, 2))));
        studySegment1.addEvent(Fixtures.createScheduledActivityWithStudy("EEE", 2005, Calendar.AUGUST, 3));
        studySegment1.addEvent(Fixtures.createScheduledActivityWithStudy("FFF", 2005, Calendar.AUGUST, 8));
        calendar.addStudySegment(studySegment1);

        List<StudySubjectAssignment> assignments = Collections.singletonList(subjectAssignment);
        expect(studyDao.getAssignmentsForStudy(study.getId())).andReturn(assignments);

        Activity reconsent = setId(1, createNamedInstance("Reconsent", Activity.class));
        expect(activityDao.getByName("Reconsent")).andReturn(reconsent);
        scheduledActivityDao.save(isA(ScheduledActivity.class));
        studyDao.save(study);

        replayMocks();
        service.scheduleReconsent(study, staticNowFactory.getNow(), "Reconsent Details");
        verifyMocks();

        List<ScheduledActivity> list = studySegment1.getActivitiesByDate().get(DateTools.createTimestamp(2005, Calendar.AUGUST, 3));

        assertEquals("Wrong number of events on August 8th", 2, list.size());
        assertEquals("Reconsent Details should be details", "Reconsent Details", list.get(0).getDetails());
        assertEquals("Reconsent should be activity name", "Reconsent", list.get(0).getActivity().getName());
    }

    public void testSendMailForScheduleReconsent() throws Exception {
        staticNowFactory.setNowTimestamp(DateTools.createTimestamp(2005, Calendar.JULY, 2));
        study.setAssignedIdentifier("testStudy");
        ScheduledStudySegment studySegment = new ScheduledStudySegment();
        studySegment.addEvent(Fixtures.createScheduledActivity("AAA", 2005, Calendar.JULY, 4));
        expect(activityDao.getByName("Reconsent")).andReturn(setId(1, createNamedInstance("Reconsent", Activity.class)));
        scheduledActivityDao.save(isA(ScheduledActivity.class));
        studyDao.save(study);
        calendar.addStudySegment(studySegment);

        User SSCM = AuthorizationObjectFactory.createCsmUser(1, "testUser");
        SSCM.setEmailId("testUser@email.com");
        subjectAssignment.setStudySubjectCalendarManager(SSCM);

        expect(studyDao.getAssignmentsForStudy(study.getId())).andReturn(Arrays.asList(subjectAssignment));
        String subject = "Subjects on testStudy need to be reconsented";
        String message = "A reconsent activity with details Reconsent Details has been added to the schedule of each subject on testStudy." +
                " Check your dashboard for upcoming subjects that need to be reconsented.";
        notificationService.sendNotificationMailToUsers(subject, message, Arrays.asList(SSCM.getEmailId()));

        replayMocks();
        service.scheduleReconsent(study, staticNowFactory.getNow(), "Reconsent Details");
        verifyMocks();
    }

    public void testSave() {
        Study expected = createNamedInstance("Study A", Study.class);
        Amendment amend0 = Fixtures.createAmendments("Amendment A");
        Amendment amend1 = Fixtures.createAmendments("Amendment B");
        amend1.setPreviousAmendment(amend0);
        expected.setAmendment(amend1);

        studyDao.save(expected);
        deltaService.saveRevision(amend1);
        deltaService.saveRevision(amend0);
        replayMocks();

        service.save(expected);
        verifyMocks();
    }

    public void testDefaultManagingSitesSetFromUser() throws Exception {
        List<Site> sites = Arrays.asList(createSite("A", "A"), createSite("B", "B"));
        PscUser principal = createUserAndSetCsmId();
        SuiteRoleMembership mem = createSuiteRoleMembership(PscRole.STUDY_CREATOR).forSites(sites);
        principal.getMemberships().put(SuiteRole.STUDY_CREATOR, mem);

        createAndExpectSession(principal);
        expectCreateAndGetMembership(SuiteRole.STUDY_CREATOR, true, null);
        expectCreateAndGetMembership(SuiteRole.STUDY_CALENDAR_TEMPLATE_BUILDER, true, null);

        Study expected = createNamedInstance("A", Study.class);

        studyDao.save(expected);
        expect(siteDao.reassociate(sites)).andReturn(sites);
        replayMocks();

        service.save(expected);
        verifyMocks();

        assertEquals("Wrong number of managing sites", 2, expected.getManagingSites().size());
        Iterator<Site> siteIterator = expected.getManagingSites().iterator();
        assertEquals("Wrong 1st managing site", "A", siteIterator.next().getAssignedIdentifier());
        assertEquals("Wrong 2nd managing site", "B", siteIterator.next().getAssignedIdentifier());
    }

    public void testDefaultManagingSitesForAllSitesUser() throws Exception {
        PscUser principal = createUserAndSetCsmId();
        SuiteRoleMembership mem = createSuiteRoleMembership(PscRole.STUDY_CREATOR).forAllSites();
        principal.getMemberships().put(SuiteRole.STUDY_CREATOR, mem);

        createAndExpectSession(principal);
        expectCreateAndGetMembership(SuiteRole.STUDY_CREATOR, true, null);
        expectCreateAndGetMembership(SuiteRole.STUDY_CALENDAR_TEMPLATE_BUILDER, true, null);

        Study expected = createNamedInstance("A", Study.class);

        studyDao.save(expected);
        replayMocks();

        service.save(expected);
        verifyMocks();

        assertEquals("Wrong number of managing sites", 0, expected.getManagingSites().size());
    }

    public void testNoManagingSitesSetFromNonBuilderUser() throws Exception {
        PscUser principal = createUserAndSetCsmId();
        SuiteRoleMembership mem = createSuiteRoleMembership(PscRole.STUDY_QA_MANAGER).
                    forSites(createSite("A", "A"), createSite("B", "B"));
        principal.getMemberships().put(SuiteRole.STUDY_QA_MANAGER, mem);

        createAndExpectSession(principal);
        expectCreateAndGetMembership(SuiteRole.STUDY_CREATOR, true, null);
        expectCreateAndGetMembership(SuiteRole.STUDY_CALENDAR_TEMPLATE_BUILDER, true, null);


        Study expected = createNamedInstance("A", Study.class);

        studyDao.save(expected);
        replayMocks();

        service.save(expected);
        verifyMocks();

        assertEquals("Wrong number of managing sites", 0, expected.getManagingSites().size());
    }

    public void testNoManagingSitesSetWithNoUser() throws Exception {
        applicationSecurityManager.removeUserSession();

        Study expected = createNamedInstance("A", Study.class);

        studyDao.save(expected);
        replayMocks();

        service.save(expected);
        verifyMocks();

        assertEquals("Wrong number of managing sites", 0, expected.getManagingSites().size());
    }

    public void testManagingSitesNotChangedForUpdates() throws Exception {
        SecurityContextHolderTestHelper.setSecurityContext(
            createPscUser("sherry",
                createSuiteRoleMembership(PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER).
                    forSites(createSite("A", "A"), createSite("B", "B"))),
            "secret"
        );

        Study expected = setId(4, createNamedInstance("A", Study.class));
        expected.addManagingSite(createSite("C"));

        studyDao.save(expected);
        replayMocks();

        service.save(expected);
        verifyMocks();

        assertEquals("Wrong number of managing sites", 1, expected.getManagingSites().size());
        Iterator<Site> siteIterator = expected.getManagingSites().iterator();
        assertEquals("Wrong 1st managing site", "C", siteIterator.next().getName());
    }

    public void testApplyDefaultStudyAccess() throws Exception {
        Site site1 = createSite("Site1", "Site1");
        Study expected = createNamedInstance("A", Study.class);
        expected.setAssignedIdentifier("StudyA");
        PscUser principal = createUserAndSetCsmId();
        expectCreateAndGetMembership(SuiteRole.STUDY_CREATOR, false, site1);
        SuiteRoleMembership SCTB = expectCreateAndGetMembership(SuiteRole.STUDY_CALENDAR_TEMPLATE_BUILDER, false, site1);
        createAndExpectSession(principal);
        pSession.replaceRole(SCTB);
        studyDao.save(expected);
        assertEquals("Membership made for specified study", 0, SCTB.getStudyIdentifiers().size());
        assertFalse("Membership made for specified study",
            SCTB.getStudyIdentifiers().contains(expected.getAssignedIdentifier()));

        replayMocks();
        service.save(expected);
        verifyMocks();

        assertEquals("Membership not made for specified study", 1, SCTB.getStudyIdentifiers().size());
        assertTrue("Membership not made for specified study",
            SCTB.getStudyIdentifiers().contains(expected.getAssignedIdentifier()));
    }

    public void testApplyDefaultStudyAccessWhenUserHasNoAccessToSite() throws Exception {
        Site site1 = createSite("Site1", "Site1");
        Site site2 = createSite("Site2", "Site2");
        Study expected = createNamedInstance("A", Study.class);
        expected.setAssignedIdentifier("StudyA");
        PscUser principal = createUserAndSetCsmId();
        expectCreateAndGetMembership(SuiteRole.STUDY_CREATOR, false, site1);
        SuiteRoleMembership SCTB = expectCreateAndGetMembership(SuiteRole.STUDY_CALENDAR_TEMPLATE_BUILDER, false, site2);
        createAndExpectSession(principal);
        studyDao.save(expected);

        replayMocks();
        service.save(expected);
        verifyMocks();

        assertEquals("Membership made for specified study", 0, SCTB.getStudyIdentifiers().size());
        assertFalse("Membership made for specified study",
            SCTB.getStudyIdentifiers().contains(expected.getAssignedIdentifier()));
    }

    public void testApplyDefaultStudyAccessWhenUserIsNotBuilderAndCreator() throws Exception {
        Site site1 = createSite("Site1", "Site1");
        Study expected = createNamedInstance("A", Study.class);
        expected.setAssignedIdentifier("StudyA");
        PscUser principal = createUserAndSetCsmId();
        expectCreateAndGetMembership(SuiteRole.STUDY_CREATOR, true, site1);
        SuiteRoleMembership SCTB = expectCreateAndGetMembership(SuiteRole.STUDY_CALENDAR_TEMPLATE_BUILDER, false, site1);
        createAndExpectSession(principal);
        studyDao.save(expected);

        replayMocks();
        service.save(expected);
        verifyMocks();

        assertEquals("Membership made for specified study", 0, SCTB.getStudyIdentifiers().size());
        assertFalse("Membership made for specified study",
            SCTB.getStudyIdentifiers().contains(expected.getAssignedIdentifier()));
    }

    public void testApplyDefaultStudyAccessWhenBuilderUserHasAllStudies() throws Exception {
        Site site1 = createSite("Site1", "Site1");
        Study expected = createNamedInstance("A", Study.class);
        expected.setAssignedIdentifier("StudyA");
        PscUser principal = createUserAndSetCsmId();
        expectCreateAndGetMembership(SuiteRole.STUDY_CREATOR, false, site1);
        SuiteRoleMembership SCTB = createSuiteRoleMembership(PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER).forSites(site1).forAllStudies();
        createAndExpectSession(principal);
        expect(pSession.getProvisionableRoleMembership(SuiteRole.STUDY_CALENDAR_TEMPLATE_BUILDER)).andReturn(SCTB);
        studyDao.save(expected);

        replayMocks();
        service.save(expected);
        verifyMocks();
    }

    public void testApplyDefaultStudyAccessWhenCreatorHasAllSites() throws Exception {
        Site site1 = createSite("Site1", "Site1");
        Study expected = createNamedInstance("A", Study.class);
        expected.setAssignedIdentifier("StudyA");
        PscUser principal = createUserAndSetCsmId();
        SuiteRoleMembership SC = createSuiteRoleMembership(PscRole.STUDY_CREATOR).forAllSites();
        SuiteRoleMembership SCTB = expectCreateAndGetMembership(SuiteRole.STUDY_CALENDAR_TEMPLATE_BUILDER, false, site1);
        createAndExpectSession(principal);
        expect(pSession.getProvisionableRoleMembership(SuiteRole.STUDY_CREATOR)).andReturn(SC);
        studyDao.save(expected);
        pSession.replaceRole(SCTB);

        assertEquals("Membership made for specified study", 0, SCTB.getStudyIdentifiers().size());
        assertFalse("Membership made for specified study",
            SCTB.getStudyIdentifiers().contains(expected.getAssignedIdentifier()));
        replayMocks();
        service.save(expected);
        verifyMocks();
        assertEquals("Membership not made for specified study", 1, SCTB.getStudyIdentifiers().size());
        assertTrue("Membership not made for specified study",
            SCTB.getStudyIdentifiers().contains(expected.getAssignedIdentifier()));
    }

    public void testApplyDefaultStudyAccessWhenBuilderHasAllSites() throws Exception {
        Site site1 = createSite("Site1", "Site1");
        Study expected = createNamedInstance("A", Study.class);
        expected.setAssignedIdentifier("StudyA");
        PscUser principal = createUserAndSetCsmId();
        SuiteRoleMembership SCTB = createSuiteRoleMembership(PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER).forAllSites();
        expectCreateAndGetMembership(SuiteRole.STUDY_CREATOR, false, site1);
        createAndExpectSession(principal);
        expect(pSession.getProvisionableRoleMembership(SuiteRole.STUDY_CALENDAR_TEMPLATE_BUILDER)).andReturn(SCTB);
        studyDao.save(expected);
        pSession.replaceRole(SCTB);

        assertEquals("Membership made for specified study", 0, SCTB.getStudyIdentifiers().size());
        assertFalse("Membership made for specified study",
            SCTB.getStudyIdentifiers().contains(expected.getAssignedIdentifier()));
        replayMocks();
        service.save(expected);
        verifyMocks();
        assertEquals("Membership not made for specified study", 1, SCTB.getStudyIdentifiers().size());
        assertTrue("Membership not made for specified study",
            SCTB.getStudyIdentifiers().contains(expected.getAssignedIdentifier()));
    }

    //Helper Methods
    private SuiteRoleMembership expectCreateAndGetMembership(SuiteRole expectedRole, Boolean isNull, Site site){
        SuiteRoleMembership srm =
            AuthorizationScopeMappings.createSuiteRoleMembership(PscRole.valueOf(expectedRole)).forSites(site);
        if (isNull) {
            expect(pSession.getProvisionableRoleMembership(expectedRole)).andReturn(null);
        } else {
            expect(pSession.getProvisionableRoleMembership(expectedRole)).andReturn(srm);
        }
        return srm;
    }

    private PscUser createUserAndSetCsmId() {
        PscUser principal = createPscUser("sherry");
        principal.getCsmUser().setUserId(111L);
        return principal;
    }

    private void createAndExpectSession(PscUser principal) {
        SecurityContextHolderTestHelper.setSecurityContext(principal,"secret");
        expect(psFactory.createSession(principal.getCsmUser().getUserId())).andStubReturn(pSession);
    }

    public void testGetNewStudyName() {
        Study study1 = createNamedInstance("[ABC 1000]", Study.class);
        Study study2 = createNamedInstance("[ABC temp]", Study.class);
        Study study3 = createNamedInstance("[ABC 1001]", Study.class);

        List<Study> studies = new ArrayList<Study>();
        studies.add(study1);
        studies.add(study2);
        studies.add(study3);

        expect(service.getStudyDao().searchStudiesByAssignedIdentifier("[ABC %]")).andReturn(studies);

        replayMocks();
        String studyName = service.getNewStudyName();
        verifyMocks();

        assertNotNull("New study name is null", studyName);
        assertEquals("Expected new study name is not the same", "[ABC 1002]", studyName);
    }

    public void testCreateInDesignStudyFromExamplePlanTree() throws Exception {
        Study example = Fixtures.createBasicTemplate();
        PlannedCalendar expectedPC = example.getPlannedCalendar();
        // copy the list since the one in the PC is destroyed in the tested method
        List<Epoch> expectedEpochs = new ArrayList<Epoch>(expectedPC.getEpochs());
        example.setAssignedIdentifier("nerf-herder");

        studyDao.save(example);
        deltaService.saveRevision((Revision) EasyMock.notNull());
        replayMocks();
        service.createInDesignStudyFromExamplePlanTree(example);
        verifyMocks();

        assertNotNull("Development amendment not added", example.getDevelopmentAmendment());
        assertEquals("Development amendment has wrong name", Amendment.INITIAL_TEMPLATE_AMENDMENT_NAME,
            example.getDevelopmentAmendment().getName());
        assertEquals("Wrong number of deltas in the new dev amendment",
            1, example.getDevelopmentAmendment().getDeltas().size());
        Delta<?> actualDelta = example.getDevelopmentAmendment().getDeltas().get(0);
        assertEquals("Wrong kind of delta in the new dev amendment",
            expectedPC, actualDelta.getNode());
        assertEquals("Wrong number of changes in created delta", expectedEpochs.size(),
            actualDelta.getChanges().size());
        for (int i = 0; i < expectedEpochs.size(); i++) {
            assertEquals(i + " change is not an add",
                ChangeAction.ADD, actualDelta.getChanges().get(i).getAction());
            assertSame(i + " change is not an add of the correct epoch",
                expectedEpochs.get(i), ((Add) actualDelta.getChanges().get(i)).getChild());
        }
    }

    public void testGetVisibleTemplates() throws Exception {
        Site nu = setId(18, createSite("NU", "IL090"));
        Study readyAndInDev = assignIds(createBasicTemplate("R"));
        StudySite nuR = setId(81, readyAndInDev.addSite(nu));
        nuR.approveAmendment(readyAndInDev.getAmendment(), new Date());
        readyAndInDev.setDevelopmentAmendment(new Amendment());

        Study pending = assignIds(createBasicTemplate("P"), 2);
        Study inDev = assignIds(createInDevelopmentBasicTemplate("D"), 5);

        PscUser user = AuthorizationObjectFactory.createPscUser("jo",
            createSuiteRoleMembership(PscRole.STUDY_QA_MANAGER).forAllSites(),
            createSuiteRoleMembership(PscRole.STUDY_SUBJECT_CALENDAR_MANAGER).forAllSites().forAllStudies());

        expect(studyDao.searchVisibleStudies(user.getVisibleStudyParameters(), null)).
            andReturn(Arrays.asList(pending, readyAndInDev, inDev));

        replayMocks();
        Map<TemplateAvailability, List<StudyWorkflowStatus>> actual
            = service.getVisibleStudies(user);
        verifyMocks();

        System.out.println(actual);

        List<StudyWorkflowStatus> actualPending = actual.get(TemplateAvailability.PENDING);
        assertEquals("Wrong number of pending templates", 1, actualPending.size());
        assertEquals("Wrong pending template", "P", actualPending.get(0).getStudy().getAssignedIdentifier());

        List<StudyWorkflowStatus> actualAvailable = actual.get(TemplateAvailability.AVAILABLE);
        assertEquals("Wrong number of available templates", 1, actualAvailable.size());
        assertEquals("Wrong available template", "R", actualAvailable.get(0).getStudy().getAssignedIdentifier());

        List<StudyWorkflowStatus> actualDev = actual.get(TemplateAvailability.IN_DEVELOPMENT);
        assertEquals("Wrong number of dev templates", 2, actualDev.size());
        assertEquals("Wrong 1st dev template", "D", actualDev.get(0).getStudy().getAssignedIdentifier());
        assertEquals("Wrong 2nd dev template", "R", actualDev.get(1).getStudy().getAssignedIdentifier());
    }

    public void testSearchVisibleTemplates() throws Exception {
        Study inDev = createInDevelopmentBasicTemplate("D");

        PscUser user = AuthorizationObjectFactory.createPscUser("jo",
            createSuiteRoleMembership(PscRole.STUDY_QA_MANAGER).forAllSites(),
            createSuiteRoleMembership(PscRole.STUDY_SUBJECT_CALENDAR_MANAGER).forAllSites().forAllStudies());

        expect(studyDao.searchVisibleStudies(user.getVisibleStudyParameters(), "d")).
            andReturn(Arrays.asList(inDev));

        replayMocks();
        Map<TemplateAvailability, List<StudyWorkflowStatus>> actual
            = service.searchVisibleStudies(user, "d");
        verifyMocks();

        System.out.println(actual);

        assertEquals("Wrong number of pending templates", 0, actual.get(TemplateAvailability.PENDING).size());
        assertEquals("Wrong number of available templates", 0, actual.get(TemplateAvailability.AVAILABLE).size());

        List<StudyWorkflowStatus> actualDev = actual.get(TemplateAvailability.IN_DEVELOPMENT);
        assertEquals("Wrong number of dev templates", 1, actualDev.size());
        assertEquals("Wrong 1st dev template", "D", actualDev.get(0).getStudy().getAssignedIdentifier());
    }
}
