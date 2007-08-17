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
    private boolean memoryOnly;

    public P getParent() {
        return parent;
    }

    public void setParent(P parent) {
        this.parent = parent;
    }

    /**
     * Tainting flag to indicated that this node instance shouldn't be saved.  I.e., it is a
     * transient copy of the persistent node with the same IDs, used only for
     * building concrete revision trees.
     *
     * TODO: enforce this somehow, possibly with a hibernate listener
     */
    public boolean isMemoryOnly() {
        return memoryOnly;
    }

    public void setMemoryOnly(boolean memoryOnly) {
        this.memoryOnly = memoryOnly;
    }

    public PlanTreeNode<P> transientClone() {
        PlanTreeNode<P> clone = this.clone();
        clone.setMemoryOnly(true);
        return clone;
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    protected PlanTreeNode<P> clone() {
        try {
            return (PlanTreeNode<P>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new StudyCalendarError("Clone is supported", e);
        }
    }

    @Override
    public String toString() {
        return new StringBuilder(getClass().getSimpleName())
            .append("[id=").append(getId()).append(']')
            .toString();
    }
}
