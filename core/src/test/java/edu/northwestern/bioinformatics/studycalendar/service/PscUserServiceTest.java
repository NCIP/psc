package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.PscUserBuilder;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import static org.easymock.EasyMock.*;

public class PscUserServiceTest extends StudyCalendarTestCase {
    private PscUserService service;

    private edu.northwestern.bioinformatics.studycalendar.domain.User legacyUser;
    private User csmUser;
    private Group dataReaderGroup;
    private Site whatCheer, northLiberty, solon;
    private Study eg1701;

    private UserService userService;
    private SiteDao siteDao;
    private StudyDao studyDao;
    private StudySubjectAssignmentDao assignmentDao;
    private PlatformTransactionManager transactionManager;
    private SuiteRoleMembershipLoader suiteRoleMembershipLoader;
    private CsmHelper csmHelper;
    private AuthorizationManager csmAuthorizationManager;
    private LegacyModeSwitch aSwitch;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        userService = registerMockFor(UserService.class);
        csmAuthorizationManager = registerMockFor(AuthorizationManager.class);
        suiteRoleMembershipLoader = registerMockFor(SuiteRoleMembershipLoader.class);
        transactionManager = registerMockFor(PlatformTransactionManager.class);
        aSwitch = new LegacyModeSwitch();

        siteDao = registerDaoMockFor(SiteDao.class);
        studyDao = registerDaoMockFor(StudyDao.class);
        assignmentDao = registerDaoMockFor(StudySubjectAssignmentDao.class);

        csmHelper = registerMockFor(CsmHelper.class);
        csmAuthorizationManager = registerMockFor(AuthorizationManager.class);

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
        service.setStudySubjectAssignmentDao(assignmentDao);

        csmUser = new User();
        csmUser.setLoginName("John");
        csmUser.setUserId(5L);
        legacyUser = Fixtures.createUser(1, "John", 1L, true);

        dataReaderGroup = new Group();
        dataReaderGroup.setGroupName(SuiteRole.DATA_READER.getCsmName());
        dataReaderGroup.setGroupId(6L);

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
        expect(csmHelper.getRoleCsmGroup(SuiteRole.DATA_READER)).andStubReturn(dataReaderGroup);
        expect(csmAuthorizationManager.getUsers("6")).
            andReturn(Collections.singleton(AuthorizationObjectFactory.createCsmUser("jimbo")));

        replayMocks();
        Collection<User> actual =
            service.getCsmUsers(PscRole.DATA_READER);
        verifyMocks();

        assertEquals("Wrong number of users returned", 1, actual.size());
        assertEquals("Wrong user returned", "jimbo", actual.iterator().next().getLoginName());
    }

    public void testGetCsmUsersForRoleWhenAuthorizationManagerFails() throws Exception {
        expect(csmHelper.getRoleCsmGroup(SuiteRole.DATA_READER)).andStubReturn(dataReaderGroup);
        expect(csmAuthorizationManager.getUsers("6")).andThrow(new CSObjectNotFoundException("Nope"));

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
        expect(assignmentDao.getAssignmentsInIntersection(Arrays.asList(3, 7), Arrays.asList(4))).
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
}
