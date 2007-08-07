package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.auditing.LoginAuditDao;
import edu.northwestern.bioinformatics.studycalendar.domain.auditing.LoginAudit;
import gov.nih.nci.security.AuthenticationManager;
import gov.nih.nci.security.exceptions.CSException;

import java.sql.Timestamp;
import java.util.Date;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Padmaja Vedula
 * @author Rhett Sutphin
 */
public class LoginCommand {
//    private static final Log log = LogFactory.getLog(LoginCommand.class);
    private static final Logger log = LoggerFactory.getLogger(LoginCommand.class);
    private String username;
    private String password;

    private AuthenticationManager authenticationManager;
    private LoginAuditDao auditDao;

    public LoginCommand(AuthenticationManager authenticationManager, LoginAuditDao auditDao) {
        this.authenticationManager = authenticationManager;
        this.auditDao = auditDao;
    }

    
    ////// LOGIC

    public boolean login(String ipAddress) {
        // check login credentials using Authentication Manager of CSM
        boolean loginSuccess = false;
        try {
            loginSuccess = authenticationManager.login(getUsername(), getPassword());
        } catch (CSException ex) {
            log.debug("Authentication failed for " + getUsername() + " with exception", ex);
        }
        createAuditObject(ipAddress, new Timestamp(new Date().getTime()), new Boolean(loginSuccess).toString());
        if (loginSuccess) {
            log.debug("Login successful for " + getUsername());
        } else {
            log.debug("Log in failed for " + getUsername());
        }
        return loginSuccess;
    }

    ////// BOUND PROPERTIES

    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    
    public void createAuditObject(String ipAddress, Timestamp time, String loginStatus) {
    	LoginAudit auditObj = new LoginAudit();
    	String status = "Failure";
    	auditObj.setIpAddress(ipAddress);
    	if (loginStatus.equals("true")) {
    		status = "Success";
    	}
    	auditObj.setLoginStatus(status);
    	auditObj.setTime(time);
    	auditObj.setUserName(getUsername());
    	saveAudit(auditObj);
    	
    }
    public void saveAudit(LoginAudit auditObj) {
    	auditDao.save(auditObj);
    }
}
