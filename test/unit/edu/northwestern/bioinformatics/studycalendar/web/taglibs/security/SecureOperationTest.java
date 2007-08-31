package edu.northwestern.bioinformatics.studycalendar.web.taglibs.security;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import gov.nih.nci.security.acegi.csm.authorization.DelegatingObjectPrivilegeCSMAuthorizationCheck;
import org.acegisecurity.Authentication;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import static org.easymock.classextension.EasyMock.expect;

public class SecureOperationTest extends StudyCalendarTestCase {
    SecureOperation secureOperation = null;
    DelegatingObjectPrivilegeCSMAuthorizationCheck authorizationCheck = null;
    Authentication authentication = null;
    String username = "marty";
    String password = "mcfly";

    protected void setUp() throws Exception {
        super.setUp();
        secureOperation = new SecureOperation();
        authorizationCheck =
                registerMockFor(DelegatingObjectPrivilegeCSMAuthorizationCheck.class);
        authentication = new UsernamePasswordAuthenticationToken(username, password);
    }

    public void testIsAccessAllowed() throws Exception {
        expect(authorizationCheck.checkAuthorization(authentication, "ACCESS", "/pages/cal/studyList")).andReturn(true);
        replayMocks();

        int actual = secureOperation.isAllowed(authorizationCheck, authentication, "ACCESS", "/pages/cal/studyList");
        verifyMocks();

        assertEquals(1, actual);
    }
}
