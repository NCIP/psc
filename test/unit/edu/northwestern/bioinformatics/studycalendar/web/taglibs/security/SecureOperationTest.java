package edu.northwestern.bioinformatics.studycalendar.web.taglibs.security;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import org.acegisecurity.*;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.vote.AbstractAccessDecisionManager;
import org.acegisecurity.vote.AffirmativeBased;
import static org.easymock.EasyMock.expectLastCall;

/**
 * @author John Dzak
 */

public class SecureOperationTest extends StudyCalendarTestCase {
    SecureOperation secureOperation = null;
    Authentication authentication = null;
    AbstractAccessDecisionManager authorizationCheck = null;

    protected void setUp() throws Exception {
        super.setUp();

        authorizationCheck =
                registerMockFor(AffirmativeBased.class);

        secureOperation = new SecureOperation();
        secureOperation.setAuthorizationDecisionManager(authorizationCheck);
        
        GrantedAuthority[] userRoles = {
                new GrantedAuthorityImpl("STUDY_COORDINATOR"),
                new GrantedAuthorityImpl("STUDY_ADMIN")
        };
        authentication = new UsernamePasswordAuthenticationToken("marty", "mcfly", userRoles);
    }

    public void testIsAccessAllowedPositive() throws Exception {
        ConfigAttributeDefinition pageRoles = createConfigAttributeDefinition(new String[]{"STUDY_COORDINATOR"});

        authorizationCheck.decide(authentication, "/test/one", pageRoles);
        replayMocks();

        int actual = secureOperation.isAllowed(authentication, "/test/one", pageRoles);
        verifyMocks();

        assertEquals("Incorrect page evaluation result: ", 1, actual);
    }

    public void testIsAccessAllowedNegative() throws Exception {
        ConfigAttributeDefinition pageRoles = createConfigAttributeDefinition(new String[]{"SITE_COORDINATOR"});
        
        authorizationCheck.decide(authentication, "/test/two", pageRoles);
        expectLastCall().andThrow(new AccessDeniedException(""));
        replayMocks();

        int actual = secureOperation.isAllowed(authentication, "/test/two", pageRoles);
        verifyMocks();

        assertEquals("Incorrect page evaluation result: ", 0, actual);
    }

    public static ConfigAttributeDefinition createConfigAttributeDefinition(String[] pageRoles) {
        ConfigAttributeDefinition def = new ConfigAttributeDefinition();
        for(String role: pageRoles) {
            def.addConfigAttribute(new SecurityConfig(role));
        }
        return def;
    }
}
