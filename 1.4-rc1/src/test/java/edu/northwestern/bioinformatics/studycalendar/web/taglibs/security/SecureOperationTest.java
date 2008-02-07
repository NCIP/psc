package edu.northwestern.bioinformatics.studycalendar.web.taglibs.security;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import org.acegisecurity.*;
import org.acegisecurity.intercept.web.PathBasedFilterInvocationDefinitionMap;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.vote.AbstractAccessDecisionManager;
import org.acegisecurity.vote.AffirmativeBased;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

/**
 * @author John Dzak
 */

public class SecureOperationTest extends StudyCalendarTestCase {
    SecureOperation secureOperation;
    Authentication authentication;
    AbstractAccessDecisionManager authorizationCheck;
    PathBasedFilterInvocationDefinitionMap definitionMap;

    protected void setUp() throws Exception {
        super.setUp();

        authorizationCheck = registerMockFor(AffirmativeBased.class);
        definitionMap = registerMockFor(PathBasedFilterInvocationDefinitionMap.class);

        secureOperation = new SecureOperation();
        secureOperation.setAuthorizationDecisionManager(authorizationCheck);
        secureOperation.setDefinitionMap(definitionMap);
        
        GrantedAuthority[] userRoles = {
                new GrantedAuthorityImpl("STUDY_COORDINATOR"),
                new GrantedAuthorityImpl("STUDY_ADMIN")
        };
        authentication = new UsernamePasswordAuthenticationToken("marty", "mcfly", userRoles);
    }

    public void testIsAccessAllowedPositive() throws Exception {
        ConfigAttributeDefinition pageRoles = createConfigAttributeDefinition(new String[]{"STUDY_COORDINATOR"});

        authorizationCheck.decide(authentication, "/test/a", pageRoles);
        replayMocks();

        int actual = secureOperation.isAllowed(authentication, "/test/a", pageRoles);
        verifyMocks();

        assertEquals("Incorrect page evaluation result: ", 1, actual);
    }

    public void testIsAccessAllowedNegative() throws Exception {
        ConfigAttributeDefinition pageRoles = createConfigAttributeDefinition(new String[]{"SITE_COORDINATOR"});
        
        authorizationCheck.decide(authentication, "/test/b", pageRoles);
        expectLastCall().andThrow(new AccessDeniedException(""));
        replayMocks();

        int actual = secureOperation.isAllowed(authentication, "/test/b", pageRoles);
        verifyMocks();

        assertEquals("Incorrect page evaluation result: ", 0, actual);
    }

    public void testElementRolesPositive() throws Exception {
        ConfigAttributeDefinition pageRoles = createConfigAttributeDefinition(new String[]{"SITE_COORDINATOR", "STUDY_COORDINATOR"});
        expect(definitionMap.lookupAttributes("/test/two")).andReturn(pageRoles);
        replayMocks();

        ConfigAttributeDefinition def = secureOperation.getElementRoles("/test/two");
        verifyMocks();

        assertNotNull("ConfigAttributeDefinition is null", def);
        assertEquals("ConfigAttributeDefinition wrong size", 2, def.size());
    }

    public void testElementRolesNegative() throws Exception {
        expect(definitionMap.lookupAttributes("/test/none")).andReturn(null);
        replayMocks();

        ConfigAttributeDefinition def = secureOperation.getElementRoles("/test/none");
        verifyMocks();

        assertNotNull("ConfigAttributeDefinition is null", def);
        assertEquals("ConfigAttributeDefinition wrong size", 0, def.size());
    }

    public static ConfigAttributeDefinition createConfigAttributeDefinition(String[] pageRoles) {
        ConfigAttributeDefinition def = new ConfigAttributeDefinition();
        for(String role: pageRoles) {
            def.addConfigAttribute(new SecurityConfig(role));
        }
        return def;
    }
}
