package edu.northwestern.bioinformatics.studycalendar.domain;

import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
import gov.nih.nci.cabig.ctms.domain.DomainObject;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;

/**
 * @author Rhett Sutphin
 * @param <P> parent class
 */
public class PlanTreeNode<P extends DomainObject> extends AbstractMutableDomainObject
    implements Child<P>, Cloneable
{
    private P parent;

    public P getParent() {
        return parent;
    }

    public void setParent(P parent) {
        this.parent = parent;
    }

    public PlanTreeNode<P> contentClone() {
        PlanTreeNode<P> clone = this.clone();
        clone.setId(null);
        return clone;
    }

    @Override
    protected PlanTreeNode<P> clone() {
        try {
            return (PlanTreeNode<P>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new StudyCalendarError("Clone is supported", e);
        }
    }
}
