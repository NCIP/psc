package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.PscUserBuilder;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.LegacyModeSwitch;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.VisibleSiteParameters;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.VisibleStudyParameters;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.UserStudySubjectAssignmentRelationship;
import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
import gov.nih.nci.cabig.ctms.lang.DateTools;
import gov.nih.nci.cabig.ctms.suite.authorization.CsmHelper;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembershipLoader;
import gov.nih.nci.security.AuthorizationManager;
import gov.nih.nci.security.authorization.domainobjects.Group;
import gov.nih.nci.security.authorization.domainobjects.User;
import gov.nih.nci.security.dao.UserSearchCriteria;
import gov.nih.nci.security.exceptions.CSObjectNotFoundException;
import org.acegisecurity.LockedException;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.easymock.classextension.EasyMock;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import static edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.AuthorizationScopeMappings.createSuiteRoleMembership;
import static org.easymock.EasyMock.*;

public class PscUserServiceTest extends StudyCalendarTestCase {
    private PscUserService service;

    private edu.northwestern.bioinformatics.studycalendar.domain.User legacyUser;
    private User csmUser;
    private Site whatCheer, northLiberty, solon;
    private Study eg1701;

    private UserService userService;
    private SiteDao siteDao;
    private StudyDao studyDao;
    private StudySiteDao studySiteDao;
    private StudySubjectAssignmentDao assignmentDao;
    private SuiteRoleMembershipLoader suiteRoleMembershipLoader;
    private AuthorizationManager csmAuthorizationManager;
    private LegacyModeSwitch aSwitch;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        userService = registerMockFor(UserService.class);
        csmAuthorizationManager = registerMockFor(AuthorizationManager.class);
        suiteRoleMembershipLoader = registerMockFor(SuiteRoleMembershipLoader.class);
        aSwitch = new LegacyModeSwitch();

        siteDao = registerDaoMockFor(SiteDao.class);
        studyDao = registerDaoMockFor(StudyDao.class);
        studySiteDao = registerDaoMockFor(StudySiteDao.class);
        assignmentDao = registerDaoMockFor(StudySubjectAssignmentDao.class);

        CsmHelper csmHelper = registerMockFor(CsmHelper.class);
        csmAuthorizationManager = registerMockFor(AuthorizationManager.class);

        PlatformTransactionManager transactionManager = registerMockFor(PlatformTransactionManager.class);
        DefaultTransactionStatus status = new DefaultTransactionStatus(null, true, true, true, true, null);
        expect(transactionManager.getTransaction((TransactionDefinition) notNull())).
            andStubReturn(status);
        transactionManager.rollback(status);
        expectLastCall().asStub();

        service = new PscUserService();
        service.setUserService(userService);
        service.setTransactionManager(transactionManager);
        service.setCsmAuthorizationManager(csmAuthorizationManager);
        service.setSuiteRoleMembershipLoader(suiteRoleMembershipLoader);
        service.setLegacyModeSwitch(aSwitch);
        service.setCsmHelper(csmHelper);
        service.setStudyDao(studyDao);
        service.setSiteDao(siteDao);
        service.setStudySiteDao(studySiteDao);
        service.setStudySubjectAssignmentDao(assignmentDao);

        csmUser = AuthorizationObjectFactory.createCsmUser(5, "John");
        legacyUser = Fixtures.createUser(1, "John", 1L, true);

        for (SuiteRole role : SuiteRole.values()) {
            Group g = new Group();
            g.setGroupId((long) role.ordinal());
            g.setGroupName(role.getCsmName());
            expect(csmHelper.getRoleCsmGroup(role)).andStubReturn(g);
        }

        whatCheer = setId(987, createSite("What Cheer", "IA987"));
        northLiberty = setId(720, createSite("North Liberty", "IA720"));
        solon = setId(846, createSite("Solon", "IA846"));

        eg1701 = createBasicTemplate("EG 1701");
    }

    public void testLoadKnownUser() throws Exception {
        expect(csmAuthorizationManager.getUser("John")).andReturn(csmUser);
        expect(userService.getUserByName("John")).andReturn(legacyUser);
        Map<SuiteRole,SuiteRoleMembership> expectedMemberships = Collections.singletonMap(SuiteRole.SYSTEM_ADMINISTRATOR,
            new SuiteRoleMembership(SuiteRole.SYSTEM_ADMINISTRATOR, null, null));
        expect(suiteRoleMembershipLoader.getRoleMemberships(csmUser.getUserId())).andReturn(
            expectedMemberships);
        replayMocks();

        PscUser actual = service.loadUserByUsername("John");
        assertNotNull(actual);
        assertSame("Wrong user", "John", actual.getUsername());
        assertSame("Wrong memberships", expectedMemberships, actual.getMemberships());
        assertSame("Wrong legacy user", legacyUser, actual.getLegacyUser());
    }

    public void testNullCsmUserThrowsException() throws Exception {
        expect(csmAuthorizationManager.getUser("John")).andReturn(null);
        replayMocks();

        try {
            service.loadUserByUsername(csmUser.getLoginName());
            fail("Exception not thrown");
        } catch (UsernameNotFoundException unfe) {
            // good
        }
        verifyMocks();
    }

    public void testDeactivatedCsmUserThrowsException() throws Exception {
        aSwitch.setOn(false);
        csmUser.setEndDate(DateTools.createDate(2006, Calendar.MAY, 3));
        expect(csmAuthorizationManager.getUser("John")).andReturn(csmUser);
        expect(suiteRoleMembershipLoader.getRoleMemberships(csmUser.getUserId())).andReturn(
            Collections.<SuiteRole, SuiteRoleMembership>emptyMap());
        replayMocks();

        try {
            service.loadUserByUsername(csmUser.getLoginName());
            fail("Exception not thrown");
        } catch (LockedException le) {
             // good
        }
        verifyMocks();
    }

    public void testGetKnownUserForProvisioning() throws Exception {
        aSwitch.setOn(false);
        expect(csmAuthorizationManager.getUser("John")).andReturn(csmUser);
        Map<SuiteRole,SuiteRoleMembership> expectedMemberships =
            Collections.singletonMap(SuiteRole.SYSTEM_ADMINISTRATOR,
                new SuiteRoleMembership(SuiteRole.SYSTEM_ADMINISTRATOR, null, null));
        expect(suiteRoleMembershipLoader.getProvisioningRoleMemberships(csmUser.getUserId())).
            andReturn(expectedMemberships);
        replayMocks();

        PscUser actual = service.getProvisionableUser("John");
        assertNotNull(actual);
        assertSame("Wrong user", "John", actual.getUsername());
        assertSame("Wrong memberships", expectedMemberships, actual.getMemberships());
    }

    public void testGetNullCsmUserForProvisioningReturnsNull() throws Exception {
        aSwitch.setOn(false);
        expect(csmAuthorizationManager.getUser("John")).andReturn(null);
        replayMocks();

        assertNull(service.getProvisionableUser(csmUser.getLoginName()));
        verifyMocks();
    }

    public void testDeactivatedCsmUserIsReturnedForProvisioning() throws Exception {
        aSwitch.setOn(false);
        csmUser.setEndDate(DateTools.createDate(2006, Calendar.MAY, 3));
        expect(csmAuthorizationManager.getUser("John")).andReturn(csmUser);
        expect(suiteRoleMembershipLoader.getProvisioningRoleMemberships(csmUser.getUserId())).
            andReturn(Collections.<SuiteRole, SuiteRoleMembership>emptyMap());
        replayMocks();

        PscUser actual = service.getProvisionableUser("John");
        assertNotNull(actual);
        assertSame("Wrong user", "John", actual.getUsername());
        verifyMocks();
    }

    public void testGetCsmUsersForRole() throws Exception {
        expect(csmAuthorizationManager.getUsers(Integer.toString(SuiteRole.DATA_READER.ordinal()))).
            andReturn(Collections.singleton(AuthorizationObjectFactory.createCsmUser("jimbo")));

        replayMocks();
        Collection<User> actual =
            service.getCsmUsers(PscRole.DATA_READER);
        verifyMocks();

        assertEquals("Wrong number of users returned", 1, actual.size());
        assertEquals("Wrong user returned", "jimbo", actual.iterator().next().getLoginName());
    }

    public void testGetCsmUsersForRoleWhenAuthorizationManagerFails() throws Exception {
        expect(csmAuthorizationManager.getUsers(Integer.toString(SuiteRole.DATA_READER.ordinal()))).
            andThrow(new CSObjectNotFoundException("Nope"));

        replayMocks();
        Collection<gov.nih.nci.security.authorization.domainobjects.User> actual =
            service.getCsmUsers(PscRole.DATA_READER);
        verifyMocks();

        assertTrue("Should have return no users", actual.isEmpty());
    }

    public void testAllUsersAreReturnedInNaturalOrder() throws Exception {
        expect(csmAuthorizationManager.getObjects(EasyMock.isA(UserSearchCriteria.class))).
            andReturn(Arrays.asList(
                AuthorizationObjectFactory.createCsmUser("b"),
                AuthorizationObjectFactory.createCsmUser("C"),
                AuthorizationObjectFactory.createCsmUser("A")));

        replayMocks();
        Collection<PscUser> actual = service.getAllUsers();
        verifyMocks();

        Iterator<PscUser> it = actual.iterator();
        assertEquals("A", it.next().getUsername());
        assertEquals("b", it.next().getUsername());
        assertEquals("C", it.next().getUsername());
    }

    public void testGetPscUsersFromCsmUsers() throws Exception {
        Map<SuiteRole, SuiteRoleMembership> expectedMemberships =
            Collections.singletonMap(SuiteRole.SYSTEM_ADMINISTRATOR,
                new SuiteRoleMembership(SuiteRole.SYSTEM_ADMINISTRATOR, null, null));
        expect(suiteRoleMembershipLoader.getRoleMemberships(5L)).andReturn(expectedMemberships);

        replayMocks();
        Collection<PscUser> actual = service.getPscUsers(Collections.singleton(csmUser), false);
        verifyMocks();

        assertEquals("Wrong number of PSC users", 1, actual.size());
        PscUser actualUser = actual.iterator().next();
        assertSame("Wrong CSM user", csmUser, actualUser.getCsmUser());
        assertSame("Wrong memberships", expectedMemberships, actualUser.getMemberships());
    }

    public void testGetPscUsersFromCsmUsersWithPartial() throws Exception {
        Map<SuiteRole, SuiteRoleMembership> expectedMemberships =
            Collections.singletonMap(SuiteRole.SYSTEM_ADMINISTRATOR,
                new SuiteRoleMembership(SuiteRole.SYSTEM_ADMINISTRATOR, null, null));
        expect(suiteRoleMembershipLoader.getProvisioningRoleMemberships(5L)).
            andReturn(expectedMemberships);

        replayMocks();
        Collection<PscUser> actual = service.getPscUsers(Collections.singleton(csmUser), true);
        verifyMocks();

        assertEquals("Wrong number of PSC users", 1, actual.size());
        PscUser actualUser = actual.iterator().next();
        assertSame("Wrong CSM user", csmUser, actualUser.getCsmUser());
        assertSame("Wrong memberships", expectedMemberships, actualUser.getMemberships());
    }

    public void testVisibleAssignments() throws Exception {
        PscUser guy = new PscUserBuilder().
            add(PscRole.STUDY_TEAM_ADMINISTRATOR).forSites(whatCheer).
            add(PscRole.STUDY_SUBJECT_CALENDAR_MANAGER).forSites(solon).forAllStudies().
            toUser();
        StudySubjectAssignment expectedAssignment =
            createAssignment(eg1701, solon, createSubject("F", "B"));

        expect(siteDao.getVisibleSiteIds(
            new VisibleSiteParameters().forParticipatingSiteIdentifiers("IA987", "IA846"))).
            andReturn(Arrays.asList(3, 7));
        expect(studyDao.getVisibleStudyIds(
            new VisibleStudyParameters().forParticipatingSiteIdentifiers("IA987", "IA846"))).
            andReturn(Arrays.asList(4));
        expect(assignmentDao.getAssignmentsInIntersection(Arrays.asList(4), Arrays.asList(3, 7))).
            andReturn(Arrays.asList(expectedAssignment));

        replayMocks();
        List<UserStudySubjectAssignmentRelationship> actual = service.getVisibleAssignments(guy);
        verifyMocks();

        assertEquals("Wrong number of visible assignments", 1, actual.size());
        assertSame("Wrong assignment visible", expectedAssignment, actual.get(0).getAssignment());
        assertSame("User not carried forward", guy, actual.get(0).getUser());
    }

    public void testGetVisibleAssignmentsDoesNotQueryAssignmentsForNonSubjectRoles() throws Exception {
        PscUser qa = new PscUserBuilder().
            add(PscRole.STUDY_QA_MANAGER).forSites(whatCheer).toUser();

        expect(siteDao.getVisibleSiteIds(new VisibleSiteParameters())).
            andReturn(Collections.<Integer>emptyList());
        expect(studyDao.getVisibleStudyIds(new VisibleStudyParameters())).
            andReturn(Collections.<Integer>emptyList());
        expect(assignmentDao.getAssignmentsInIntersection(
            Collections.<Integer>emptyList(), Collections.<Integer>emptyList())).
            andReturn(Arrays.<StudySubjectAssignment>asList());

        replayMocks();
        List<UserStudySubjectAssignmentRelationship> actual = service.getVisibleAssignments(qa);
        verifyMocks();

        assertEquals("Wrong number of assignments returned", 0, actual.size());
    }

    public void testGetVisibleStudySiteIds() throws Exception {
        PscUser guy = new PscUserBuilder().
            add(PscRole.STUDY_TEAM_ADMINISTRATOR).forSites(whatCheer).
            add(PscRole.STUDY_SUBJECT_CALENDAR_MANAGER).forSites(solon).forAllStudies().
            toUser();

        expect(siteDao.getVisibleSiteIds(
            new VisibleSiteParameters().forParticipatingSiteIdentifiers("IA987"))).
            andReturn(Arrays.asList(4));
        expect(studyDao.getVisibleStudyIds(
            new VisibleStudyParameters().forParticipatingSiteIdentifiers("IA987"))).
            andReturn(Arrays.asList(3, 7));
        expect(studySiteDao.getIntersectionIds(Arrays.asList(3, 7), Arrays.asList(4))).
            andReturn(Arrays.asList(34, 47));

        replayMocks();
        Collection<Integer> actual = service.getVisibleStudySiteIds(guy, PscRole.STUDY_TEAM_ADMINISTRATOR);
        verifyMocks();

        assertEquals("Wrong number of visible study sites", 2, actual.size());
        assertContains("Wrong study site IDs", actual, 34);
        assertContains("Wrong study site IDs", actual, 47);
    }

    public void testGetVisibleStudySiteIdsWhenNoRolesSpecified() throws Exception {
        PscUser guy = new PscUserBuilder().
            add(PscRole.STUDY_TEAM_ADMINISTRATOR).forSites(whatCheer).
            add(PscRole.STUDY_SUBJECT_CALENDAR_MANAGER).forSites(solon).forAllStudies().
            toUser();

        expect(siteDao.getVisibleSiteIds(
            new VisibleSiteParameters().forParticipatingSiteIdentifiers("IA987", "IA846"))).
            andReturn(Arrays.asList(21));
        expect(studyDao.getVisibleStudyIds(
            new VisibleStudyParameters().forParticipatingSiteIdentifiers("IA987", "IA846"))).
            andReturn(Arrays.asList(4, 84));
        expect(studySiteDao.getIntersectionIds(Arrays.asList(4, 84), Arrays.asList(21))).
            andReturn(Arrays.asList(336));

        replayMocks();
        Collection<Integer> actual = service.getVisibleStudySiteIds(guy);
        verifyMocks();

        assertEquals("Wrong number of visible study sites", 1, actual.size());
        assertContains("Wrong study site IDs", actual, 336);
    }

    public void testGetManagedAssignments() throws Exception {
        PscUser manager = new PscUserBuilder().setCsmUserId(15L).
            add(PscRole.STUDY_SUBJECT_CALENDAR_MANAGER).forAllSites().forAllStudies().
            toUser();
        StudySubjectAssignment expectedAssignment =
            createAssignment(eg1701, northLiberty, createSubject("F", "B"));

        expect(assignmentDao.getAssignmentsByManagerCsmUserId(15)).
            andReturn(Arrays.asList(expectedAssignment));

        replayMocks();
        List<UserStudySubjectAssignmentRelationship> actual = service.getManagedAssignments(manager);
        verifyMocks();

        assertEquals("Wrong number of visible assignments", 1, actual.size());
        assertSame("Wrong assignment visible", expectedAssignment, actual.get(0).getAssignment());
        assertSame("User not carried forward", manager, actual.get(0).getUser());
    }

    public void testGetManagedAssignmentsFiltersOutNoLongerManageableAssignments() throws Exception {
        PscUser manager = new PscUserBuilder().setCsmUserId(15L).
            add(PscRole.STUDY_SUBJECT_CALENDAR_MANAGER).forSites(whatCheer).forAllStudies().
            toUser();
        StudySubjectAssignment expectedOldAssignment =
            createAssignment(eg1701, northLiberty, createSubject("F", "B"));
        StudySubjectAssignment expectedStillManageableAssignment =
            createAssignment(eg1701, whatCheer, createSubject("W", "C"));

        expect(assignmentDao.getAssignmentsByManagerCsmUserId(15)).
            andReturn(Arrays.asList(expectedOldAssignment, expectedStillManageableAssignment));

        replayMocks();
        List<UserStudySubjectAssignmentRelationship> actual = service.getManagedAssignments(manager);
        verifyMocks();

        assertEquals("Wrong number of visible assignments", 1, actual.size());
        assertSame("Wrong assignment visible", expectedStillManageableAssignment, actual.get(0).getAssignment());
        assertSame("User not carried forward", manager, actual.get(0).getUser());
    }

    public void testGetTeamMembersIncludesDataReadersAndCalendarManagers() throws Exception {
        PscUser teamAdmin = new PscUserBuilder().
            add(PscRole.STUDY_TEAM_ADMINISTRATOR).forAllSites().
            toUser();

        User sscm = AuthorizationObjectFactory.createCsmUser( 6, "sally");
        User dr   = AuthorizationObjectFactory.createCsmUser(16, "joe");
        User both = AuthorizationObjectFactory.createCsmUser(96, "sigourney");

        expectGetCsmUsersForSuiteRole(SuiteRole.DATA_READER, both, dr);
        expectGetCsmUsersForSuiteRole(SuiteRole.STUDY_SUBJECT_CALENDAR_MANAGER, sscm, both);

        expect(suiteRoleMembershipLoader.getProvisioningRoleMemberships(6)).andReturn(
            Collections.singletonMap(SuiteRole.STUDY_SUBJECT_CALENDAR_MANAGER,
                catholicRoleMembership(SuiteRole.STUDY_SUBJECT_CALENDAR_MANAGER)));
        expect(suiteRoleMembershipLoader.getProvisioningRoleMemberships(16)).andReturn(
            Collections.singletonMap(SuiteRole.DATA_READER,
                catholicRoleMembership(SuiteRole.DATA_READER)));
        expect(suiteRoleMembershipLoader.getProvisioningRoleMemberships(96)).andReturn(
            new MapBuilder<SuiteRole, SuiteRoleMembership>().
                put(SuiteRole.DATA_READER, catholicRoleMembership(SuiteRole.DATA_READER)).
                put(SuiteRole.STUDY_SUBJECT_CALENDAR_MANAGER,
                    catholicRoleMembership(SuiteRole.STUDY_SUBJECT_CALENDAR_MANAGER)).
                toMap());

        replayMocks();
        List<PscUser> team = service.getTeamMembersFor(teamAdmin);
        verifyMocks();

        assertEquals("Wrong number of team members", 3, team.size());
    }

    public void testGetTeamMembersOnlyIncludesIntersectingUsers() throws Exception {
        PscUser teamAdmin = new PscUserBuilder().
            add(PscRole.STUDY_TEAM_ADMINISTRATOR).forSites(whatCheer).
            toUser();

        User wcSSCM    = AuthorizationObjectFactory.createCsmUser( 81, "bob");
        User wcDR      = AuthorizationObjectFactory.createCsmUser(243, "ben");
        User solonSSCM = AuthorizationObjectFactory.createCsmUser(729, "bill");

        expectGetCsmUsersForSuiteRole(SuiteRole.DATA_READER, wcDR);
        expectGetCsmUsersForSuiteRole(SuiteRole.STUDY_SUBJECT_CALENDAR_MANAGER, wcSSCM, solonSSCM);

        expect(suiteRoleMembershipLoader.getProvisioningRoleMemberships(81)).andReturn(
            Collections.singletonMap(SuiteRole.STUDY_SUBJECT_CALENDAR_MANAGER,
                createSuiteRoleMembership(PscRole.STUDY_SUBJECT_CALENDAR_MANAGER).forSites(whatCheer)));
        expect(suiteRoleMembershipLoader.getProvisioningRoleMemberships(243)).andReturn(
            Collections.singletonMap(SuiteRole.DATA_READER,
                createSuiteRoleMembership(PscRole.DATA_READER).forSites(whatCheer)));
        expect(suiteRoleMembershipLoader.getProvisioningRoleMemberships(729)).andReturn(
            Collections.singletonMap(SuiteRole.STUDY_SUBJECT_CALENDAR_MANAGER,
                createSuiteRoleMembership(PscRole.STUDY_SUBJECT_CALENDAR_MANAGER).forSites(solon)));

        replayMocks();
        List<PscUser> team = service.getTeamMembersFor(teamAdmin);
        verifyMocks();

        assertEquals("Wrong number of team members", 2, team.size());
        assertEquals("Wrong team member selected", "ben", team.get(0).getUsername());
        assertEquals("Wrong team member selected", "bob", team.get(1).getUsername());
    }

    public void testGetTeamMembersDoesNothingForNonTeamAdmin() throws Exception {
        PscUser someGuy = new PscUserBuilder().add(PscRole.SYSTEM_ADMINISTRATOR).toUser();

        replayMocks();
        List<PscUser> team = service.getTeamMembersFor(someGuy);
        verifyMocks();

        assertEquals("Should have no team", 0, team.size());
    }

    public void testGetColleaguesExcludesNonIntersectingUsers() throws Exception {
        PscUser main = new PscUserBuilder().
            add(PscRole.STUDY_SUBJECT_CALENDAR_MANAGER).forSites(northLiberty, solon).forAllStudies().
            toUser();

        User sal = AuthorizationObjectFactory.createCsmUser(  6, "sally");
        User joe = AuthorizationObjectFactory.createCsmUser( 16, "joe");
        User sig = AuthorizationObjectFactory.createCsmUser( 96, "sigourney");
        User gis = AuthorizationObjectFactory.createCsmUser(576, "giselle");

        expectGetCsmUsersForSuiteRole(SuiteRole.STUDY_SUBJECT_CALENDAR_MANAGER, sal, joe, sig, gis);

        expect(suiteRoleMembershipLoader.getRoleMemberships(6)).andReturn(
            Collections.singletonMap(SuiteRole.STUDY_SUBJECT_CALENDAR_MANAGER,
                createSuiteRoleMembership(PscRole.STUDY_SUBJECT_CALENDAR_MANAGER).forAllSites().forAllStudies()));
        expect(suiteRoleMembershipLoader.getRoleMemberships(16)).andReturn(
            Collections.singletonMap(SuiteRole.STUDY_SUBJECT_CALENDAR_MANAGER,
                createSuiteRoleMembership(PscRole.STUDY_SUBJECT_CALENDAR_MANAGER).forSites(whatCheer, solon).forAllStudies()));
        expect(suiteRoleMembershipLoader.getRoleMemberships(96)).andReturn(
            Collections.singletonMap(SuiteRole.STUDY_SUBJECT_CALENDAR_MANAGER,
                createSuiteRoleMembership(PscRole.STUDY_SUBJECT_CALENDAR_MANAGER).forSites(whatCheer).forAllStudies()));
        // gis is only partially provisioned, so she is in the group but doesn't have the membership
        expect(suiteRoleMembershipLoader.getRoleMemberships(576)).andReturn(
            Collections.<SuiteRole, SuiteRoleMembership>emptyMap());

        replayMocks();
        List<PscUser> colleagues = service.getColleaguesOf(main, PscRole.STUDY_SUBJECT_CALENDAR_MANAGER);
        verifyMocks();

        assertEquals("Wrong number of colleagues", 2, colleagues.size());
        assertEquals("Wrong colleague included", "joe", colleagues.get(0).getUsername());
        assertEquals("Wrong colleague included", "sally", colleagues.get(1).getUsername());
    }

    public void testGetColleaguesDoesNothingForNonMatchingRole() throws Exception {
        PscUser someGuy = new PscUserBuilder().add(PscRole.SYSTEM_ADMINISTRATOR).toUser();

        replayMocks();
        List<PscUser> colleagues = service.getColleaguesOf(someGuy, PscRole.DATA_IMPORTER);
        verifyMocks();

        assertEquals("Should have no colleagues", 0, colleagues.size());
    }

    private SuiteRoleMembership catholicRoleMembership(SuiteRole role) {
        SuiteRoleMembership srm = new SuiteRoleMembership(role, null, null);
        if (role.isSiteScoped()) srm.forAllSites();
        if (role.isStudyScoped()) srm.forAllStudies();
        return srm;
    }

    private void expectGetCsmUsersForSuiteRole(SuiteRole role, User... expected) throws CSObjectNotFoundException {
        expect(csmAuthorizationManager.getUsers(Integer.toString(role.ordinal()))).
            andReturn(new HashSet<User>(Arrays.asList(expected)));
    }
}
