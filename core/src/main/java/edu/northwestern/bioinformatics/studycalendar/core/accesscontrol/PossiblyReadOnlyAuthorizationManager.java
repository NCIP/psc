/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.core.accesscontrol;

import gov.nih.nci.security.AuthorizationManager;

/**
 * @author Rhett Sutphin
 */
public interface PossiblyReadOnlyAuthorizationManager extends AuthorizationManager {
    boolean isReadOnly();
}
