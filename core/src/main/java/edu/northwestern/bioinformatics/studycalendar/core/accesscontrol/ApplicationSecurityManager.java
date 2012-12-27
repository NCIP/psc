/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.core.accesscontrol;

import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Padmaja Vedula
 * @author Rhett Sutphin
 */
public class ApplicationSecurityManager {
    private final Logger log = LoggerFactory.getLogger(getClass());

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
}
