package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import gov.nih.nci.cabig.ctms.domain.DomainObject;

import javax.persistence.Entity;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Transient;
import javax.persistence.Column;

import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeInnerNode;

import java.util.Collection;

/**
 * @author Rhett Sutphin
 */
@Entity
@DiscriminatorValue("remove")
public class Remove extends ChildrenChange {

    public static Remove create(PlanTreeNode<?> object) {
        Remove remove = new Remove();
        remove.setChild(object);
        return remove;
    }

    @Override
    @Transient
    public ChangeAction getAction() { return ChangeAction.REMOVE; }

    @Override
    @Transient
    public boolean isNoop() {
        return false;
    }

    @Override
    protected MergeLogic createMergeLogic(Delta<?> delta) {
        return new RemoveMergeLogic(delta);
    }

    ////// BEAN PROPERTIES

    @Override
    @Column(name = "old_value", nullable = false)
    public Integer getChildId() {
        return super.getChildId();
    }

    ////// OBJECT METHODS

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getSimpleName())
            .append("[id=").append(getId()).append("; child id=").append(getChildId());
        return sb.append(']').toString();
    }

    private class RemoveMergeLogic extends MergeLogic {
        private Delta<?> delta;

        public RemoveMergeLogic(Delta<?> delta) {
            this.delta = delta;
        }

        @Override
        public boolean encountered(Add change) {
            if (change.isSameChild(Remove.this)) {
                log.debug("Found equivalent add ({}).  Canceling.", change);
                delta.removeChange(change);
                return true;
            }
            return false;
        }

        @Override
        public boolean encountered(Remove change) {
            if (change.isSameChild(Remove.this)) {
                log.debug("Duplicate remove ({}).  Skipping.", change);
                return true;
            }
            return false;
        }

        private boolean nodeHasChildToRemove() {
            // Is the child we're removing in the target?
            Collection<PlanTreeNode<?>> children // the second cast is to work around a javac bug
                = (Collection<PlanTreeNode<?>>) PlanTreeInnerNode.cast(delta.getNode()).getChildren();
            for (PlanTreeNode<?> existing : children) {
                if (isSameChild(existing)) {
                    return true;
                }
            }
            log.debug("Child does not exist in live plan tree.  Will not remove.");
            return false;
        }

        @Override
        public void postProcess(boolean merged) {
            if (!merged && nodeHasChildToRemove()) {
                delta.addChange(Remove.this);
            }
        }
    }
}
