package edu.northwestern.bioinformatics.studycalendar.domain;

import gov.nih.nci.cabig.ctms.domain.DomainObject;

/**
 * @author Rhett Sutphin
 */
public interface Child<P extends DomainObject> extends DomainObject {
    void setParent(P parent);
    P getParent();
}
