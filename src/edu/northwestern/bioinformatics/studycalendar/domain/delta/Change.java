package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;

/**
 * @author Rhett Sutphin
 */
public abstract class Change extends AbstractMutableDomainObject {
    /**
     * Return the action used by this change.  It should match the discriminator value for the class.
     * @return
     */
    public abstract ChangeAction getAction();
}
