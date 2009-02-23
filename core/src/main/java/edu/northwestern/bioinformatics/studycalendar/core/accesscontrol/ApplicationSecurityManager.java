package edu.northwestern.bioinformatics.studycalendar.core.accesscontrol;

import edu.northwestern.bioinformatics.studycalendar.domain.User;
import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContextHolder;

/**
 * @author Padmaja Vedula
 * @author Rhett Sutphin
 */
public class ApplicationSecurityManager {

    @Deprecated
    public static String getUser() {
        return getUserName();
    }

    public static String getUserName() {
        String user = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            user = authentication.getName();
        }
        return user;
    }

    // TODO: remove the other getUser and rename this to getUser
    public static User getUserObj() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        } else {
            return (User) authentication.getPrincipal();
        }
    }

    public static void removeUserSession() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    // All-static class
    private ApplicationSecurityManager() { }
}
