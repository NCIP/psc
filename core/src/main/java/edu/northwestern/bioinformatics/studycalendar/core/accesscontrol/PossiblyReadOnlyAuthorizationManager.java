package edu.northwestern.bioinformatics.studycalendar.core.accesscontrol;

import gov.nih.nci.security.AuthorizationManager;

/**
 * @author Rhett Sutphin
 */
public interface PossiblyReadOnlyAuthorizationManager extends AuthorizationManager {
    boolean isReadOnly();
}
