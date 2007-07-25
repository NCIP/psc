package edu.northwestern.bioinformatics.studycalendar.domain;

import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
import gov.nih.nci.cabig.ctms.domain.DomainObject;

/**
 * @author Rhett Sutphin
 */
public class PlanTreeNode<P extends DomainObject> extends AbstractMutableDomainObject
    implements Child<P>
{
    private P parent;

    public P getParent() {
        return parent;
    }

    public void setParent(P parent) {
        this.parent = parent;
    }
}
