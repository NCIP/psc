package edu.northwestern.bioinformatics.studycalendar.core.accesscontrol;

import edu.northwestern.bioinformatics.studycalendar.domain.User;
import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContextHolder;

/**
 * @author Padmaja Vedula
 * @author Rhett Sutphin
 */
public class ApplicationSecurityManager {

    public String getUserName() {
        String user = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            user = authentication.getName();
        }
        return user;
    }

    public User getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        } else {
            return (User) authentication.getPrincipal();
        }
    }

    public void removeUserSession() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }
}
