package edu.northwestern.bioinformatics.studycalendar.core.accesscontrol;

import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.UserService;
import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Padmaja Vedula
 * @author Rhett Sutphin
 */
public class ApplicationSecurityManager {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private UserService userService;

    public String getUserName() {
        String user = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.trace("getUserName(): Retrieved authentication {}", authentication);
        if (authentication != null) {
            user = authentication.getName();
        }
        return user;
    }

    public PscUser getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.trace("getUser(): Retrieved authentication {}", authentication);
        if (authentication == null) {
            return null;
        } else {
            return (PscUser) authentication.getPrincipal();
        }
    }

    public void removeUserSession() {
        log.debug("Removing authentication from {}", SecurityContextHolder.getContext());
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    /**
     * Reloads the logged in {@link User} in the current hibernate session.
     */
    // TODO: copy metadata
    public User getFreshUser() {
        return getFreshUser(false);
    }

    /**
     * Reloads the logged in {@link User} with assignments in the current hibernate session.
     */
    //
    public User getFreshUser(Boolean includeAssignments) {
       String userName = getUserName();
        if (userName ==  null) {
            return null;
        } else {
            return userService.getUserByName(userName, includeAssignments);
        }
    }

    @Required
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
