package edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol;

import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.Authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author Padmaja Vedula
 * @author Rhett Sutphin
 */
public class ApplicationSecurityManager {
    
    public static String getUser() {
        String user = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            user = authentication.getName();
        }
        return user;
    }

    public static void removeUserSession() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    // All-static class
    private ApplicationSecurityManager() { }
}
