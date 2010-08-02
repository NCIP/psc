package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.LegacyModeSwitch;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
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
import java.util.Map;

import static org.easymock.EasyMock.*;

public class PscUserServiceTest extends StudyCalendarTestCase {
    private PscUserService service;

    private edu.northwestern.bioinformatics.studycalendar.domain.User legacyUser;
    private User csmUser;
    private Group dataReaderGroup;

    private UserService userService;
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

        csmUser = new User();
        csmUser.setLoginName("John");
        csmUser.setUserId(5L);
        legacyUser = Fixtures.createUser(1, "John", 1L, true);

        dataReaderGroup = new Group();
        dataReaderGroup.setGroupName(SuiteRole.DATA_READER.getCsmName());
        dataReaderGroup.setGroupId(6L);
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
}
