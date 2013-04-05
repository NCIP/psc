/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedCommand;

import java.util.Map;

/**
 * @author nshurupova
 * @author Rhett Sutphin
 */
public interface EditCommand extends PscAuthorizedCommand {
    boolean apply();

    Map<String, Object> getModel();

    String getRelativeViewName();
}
