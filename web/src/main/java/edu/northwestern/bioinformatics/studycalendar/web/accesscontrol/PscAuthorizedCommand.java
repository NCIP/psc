/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.accesscontrol;

import org.springframework.validation.Errors;

import java.util.Collection;

/**
 * @author Rhett Sutphin
 */
public interface PscAuthorizedCommand {
    /**
     * Return the set of authorizations describing a user which could have
     * access to the action described by this command.
     * <p>
     * The command is not guaranteed to be in a consistent state when this
     * method is called -- if there are binding problems, there could be
     * missing field values.  This method should provide a best-effort
     * set of authorizations in that case.
     *
     * @see PscAuthorizedHandler
     * @param bindErrors
     */
    Collection<ResourceAuthorization> authorizations(Errors bindErrors);
}
