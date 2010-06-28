package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.LegacyModeSwitch;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembershipLoader;
import gov.nih.nci.security.AuthorizationManager;
import gov.nih.nci.security.authorization.domainobjects.User;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;

import java.util.Collections;
import java.util.Map;

import static org.easymock.EasyMock.*;

public class PscUserDetailsServiceTest extends StudyCalendarTestCase {
    private edu.northwestern.bioinformatics.studycalendar.domain.User legacyUser;
    private User csmUser;
    private UserService userService;
    private AuthorizationManager authorizationManager;
    private PlatformTransactionManager transactionManager;
    private PscUserDetailsServiceImpl service;
    private SuiteRoleMembershipLoader suiteRoleMembershipLoader;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        userService = registerMockFor(UserService.class);
        authorizationManager = registerMockFor(AuthorizationManager.class);
        suiteRoleMembershipLoader = registerMockFor(SuiteRoleMembershipLoader.class);
        transactionManager = registerMockFor(PlatformTransactionManager.class);

        DefaultTransactionStatus status = new DefaultTransactionStatus(null, true, true, true, true, null);
        expect(transactionManager.getTransaction((TransactionDefinition) notNull())).
            andStubReturn(status);
        transactionManager.rollback(status);
        expectLastCall().asStub();

        service = new PscUserDetailsServiceImpl();
        service.setUserService(userService);
        service.setTransactionManager(transactionManager);
        service.setAuthorizationManager(authorizationManager);
        service.setSuiteRoleMembershipLoader(suiteRoleMembershipLoader);
        service.setLegacyModeSwitch(new LegacyModeSwitch());

        csmUser = new User();
        csmUser.setLoginName("John");
        csmUser.setUserId(5L);
        legacyUser = Fixtures.createUser(1, "John", 1L, true);
    }

    public void testLoadKnownUser() throws Exception {
        expect(authorizationManager.getUser("John")).andReturn(csmUser);
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
        expect(authorizationManager.getUser("John")).andReturn(null);
        replayMocks();

        try {
            service.loadUserByUsername(csmUser.getLoginName());
            fail("Exception not thrown");
        } catch (UsernameNotFoundException unfe) {
            // good
        }
        verifyMocks();
    }
}
