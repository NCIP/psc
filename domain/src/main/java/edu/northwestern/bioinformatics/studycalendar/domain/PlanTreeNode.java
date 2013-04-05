/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
import gov.nih.nci.cabig.ctms.domain.DomainObject;

/**
 * @author Rhett Sutphin
 * @param <P> parent class
 */
public abstract class PlanTreeNode<P extends DomainObject>
    extends AbstractMutableDomainObject
    implements Child<P>, Cloneable
{
    private P parent;
    private boolean memoryOnly;

    ////// LOGIC

    /**
     * Returns true if the segment of the plan tree to which this node belongs
     * is not directly associated with a study.  (That is, it is part of an unapplied revision.)
     *
     * @return
     */
    public boolean isDetached() {
        if (getParent() == null) {
            return true;
        } else if (getParent() instanceof PlanTreeNode) {
            return ((PlanTreeNode) getParent()).isDetached();
        } else {
            return false;
        }
    }

    public void clearIds() {
        setId(null);
        setGridId(null);
    }

    ////// Child IMPLEMENTATION

    public P getParent() {
        return parent;
    }

    public void setParent(P parent) {
        this.parent = parent;
    }

    ////// TransientCloneable IMPLEMENTATION

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

    ////// OBJECT METHODS

    @Override
    @SuppressWarnings({"unchecked"})
    public PlanTreeNode<P> clone() {
        try {
            return (PlanTreeNode<P>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new StudyCalendarError("Clone is supported", e);
        }
    }

    //    @Override
    @SuppressWarnings({"unchecked"})
    protected PlanTreeNode<P> copy() {
        PlanTreeNode<P> copy = clone();
        copy.setId(null);
        copy.setGridId(null);
        copy.setVersion(null);
        return copy;

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getSimpleName())
                .append("[id=").append(getId());
        if (isMemoryOnly()) sb.append("; transient copy");
        sb.append(']');
        return sb.toString();
    }

}
