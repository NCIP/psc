package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.Changeable;
import gov.nih.nci.cabig.ctms.domain.DomainObject;

/**
 * @author Rhett Sutphin
 */
public interface Child<P extends DomainObject>
    extends Changeable, Cloneable, DeepComparable<Object>
{
    Class<P> parentClass();

    void setParent(P parent);
    P getParent();

    Child<P> clone();
}
