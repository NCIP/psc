package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembershipLoader;
import gov.nih.nci.security.AuthorizationManager;
import gov.nih.nci.security.authorization.domainobjects.User;
import org.acegisecurity.userdetails.UsernameNotFoundException;

import java.util.Collections;
import java.util.Map;

import static org.easymock.EasyMock.*;

public class PscUserDetailsServiceTest extends StudyCalendarTestCase {
    private PscUserDetailsServiceImpl service;

    private User csmUser;
    private AuthorizationManager authorizationManager;
    private SuiteRoleMembershipLoader suiteRoleMembershipLoader;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        authorizationManager = registerMockFor(AuthorizationManager.class);
        suiteRoleMembershipLoader = registerMockFor(SuiteRoleMembershipLoader.class);

        service = new PscUserDetailsServiceImpl();
        service.setAuthorizationManager(authorizationManager);
        service.setSuiteRoleMembershipLoader(suiteRoleMembershipLoader);

        csmUser = new User();
        csmUser.setLoginName("John");
        csmUser.setUserId(5L);
    }

    public void testLoadKnownUser() throws Exception {
        expect(authorizationManager.getUser("John")).andReturn(csmUser);
        Map<SuiteRole,SuiteRoleMembership> expectedMemberships = Collections.singletonMap(SuiteRole.SYSTEM_ADMINISTRATOR,
            new SuiteRoleMembership(SuiteRole.SYSTEM_ADMINISTRATOR, null, null));
        expect(suiteRoleMembershipLoader.getRoleMemberships(csmUser.getUserId())).andReturn(
            expectedMemberships);
        replayMocks();

        PscUser actual = service.loadUserByUsername("John");
        assertNotNull(actual);
        assertSame("Wrong user", "John", actual.getUsername());
        assertSame("Wrong memberships", expectedMemberships, actual.getMemberships());
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
