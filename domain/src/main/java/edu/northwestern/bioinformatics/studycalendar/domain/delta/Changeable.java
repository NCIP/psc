package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.TransientCloneable;
import gov.nih.nci.cabig.ctms.domain.MutableDomainObject;

/**
 * Tag interface which indicates a domain object may be the node for a Delta
 *
 * @author Rhett Sutphin
 */
public interface Changeable extends MutableDomainObject, TransientCloneable<Changeable> {
    boolean isDetached();
}
