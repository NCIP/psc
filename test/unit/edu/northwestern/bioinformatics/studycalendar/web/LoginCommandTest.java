package edu.northwestern.bioinformatics.studycalendar.web;

import java.util.Date;
import java.sql.Timestamp;

import edu.northwestern.bioinformatics.studycalendar.domain.auditing.LoginAudit;
import edu.northwestern.bioinformatics.studycalendar.dao.auditing.LoginAuditDao;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import gov.nih.nci.security.AuthenticationManager;
import gov.nih.nci.security.exceptions.CSException;
import gov.nih.nci.cabig.ctms.domain.MutableDomainObject;
import org.easymock.classextension.EasyMock;
import static org.easymock.classextension.EasyMock.expect;

/**
 * @author Rhett Sutphin
 * @author Padmaja Vedula
 */
public class LoginCommandTest extends StudyCalendarTestCase {
    private static final String USERNAME = "alice";
    private static final String PASSWORD = "wonderland";
    private static final String IPADDRESS = "123.0.0.1";

    private LoginCommand command;

    private AuthenticationManager authenticationManager;
    private LoginAuditDao auditDao;

    protected void setUp() throws Exception {
        super.setUp();
        authenticationManager = registerMockFor(AuthenticationManager.class);
        auditDao = registerMockFor(LoginAuditDao.class, LoginAuditDao.class.getMethod("save", MutableDomainObject.class));
        command = new LoginCommand(authenticationManager, auditDao);
        command.setUsername(USERNAME);
        command.setPassword(PASSWORD);
    }

    public void testLoginSuccessfulWhenSuccessful() throws Exception {
        expect(authenticationManager.login(USERNAME, PASSWORD)).andReturn(true);
        auditDao.save((LoginAudit) EasyMock.notNull());
        replayMocks();
        assertTrue(command.login(IPADDRESS));
        verifyMocks();
    }

    public void testLoginFailsWhenFails() throws Exception {
        expect(authenticationManager.login(USERNAME, PASSWORD)).andReturn(false);
        auditDao.save((LoginAudit) EasyMock.notNull());
        replayMocks();
        assertFalse(command.login(IPADDRESS));
        verifyMocks();
    }
    
    public void testLoginFailsWhenException() throws Exception {
        expect(authenticationManager.login(USERNAME, PASSWORD)).andThrow(new CSException());
        auditDao.save((LoginAudit) EasyMock.notNull());
        replayMocks();
        assertFalse(command.login(IPADDRESS));
        verifyMocks();
    }
    
    /**
     * @throws Exception
     */
    public void testCreateLoginAudit() throws Exception {
    	LoginAudit loginAudit = new LoginAudit();
    	loginAudit.setIpAddress("123.0.0.1");
    	loginAudit.setLoginStatus("Success");
    	loginAudit.setTime(new Timestamp(new Date().getTime()));
    	loginAudit.setUserName("study_admin");
		auditDao.save(loginAudit);
		replayMocks();
		
		command.saveAudit(loginAudit);
		verifyMocks();
    }
}
