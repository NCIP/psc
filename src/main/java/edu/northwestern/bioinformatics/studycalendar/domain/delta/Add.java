package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeInnerNode;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeOrderedInnerNode;

import javax.persistence.Entity;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Transient;
import javax.persistence.Column;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rhett Sutphin
 */
@Entity
@DiscriminatorValue("add")
public class Add extends ChildrenChange {
    private Integer index;

    public static Add create(PlanTreeNode<?> child, int index) {
        Add add = create(child);
        add.setIndex(index);
        return add;
    }

    public static Add create(PlanTreeNode<?> child) {
        Add add = new Add();
        add.setChild(child);
        return add;
    }

    ////// LOGIC

    @Override
    @Transient
    public ChangeAction getAction() { return ChangeAction.ADD; }

    @Override
    protected MergeLogic createMergeLogic(Delta<?> delta) {
        return new AddMergeLogic(delta);
    }

    @Override
    protected SiblingDeletedLogic createSiblingDeletedLogic(
        Delta<?> delta, int deletedChangePosition, int thisPreDeletePosition
    ) {
        return new AddSibDeletedLogic(delta, deletedChangePosition, thisPreDeletePosition);
    }

    @Override // in order to map
    @Column(name = "new_value", nullable = false)
    public Integer getChildId() {
        return super.getChildId();
    }

    ////// BEAN PROPERTIES

    @Column(name = "attribute")
    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    ////// OBJECT METHODS

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getSimpleName())
            .append("[id=").append(getId()).append("; child id ").append(getChildId());
        if (getIndex() != null) {
            sb.append(" at index ").append(getIndex());
        }
        return sb.append(']').toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Add add = (Add) o;

        if (index != null ? !index.equals(add.index) : add.index != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (index != null ? index.hashCode() : 0);
        return result;
    }

    private class AddMergeLogic extends MergeLogic {
        private final Delta<?> delta;

        public AddMergeLogic(Delta<?> delta) {
            this.delta = delta;
        }

        @Override
        public boolean encountered(Add change) {
            if (change.isSameChild(Add.this)) {
                // do nothing -- already added
                log.debug("Child is already in an add in the delta.  Will not add again.");
                return true;
            }
            return false;
        }

        @Override
        public boolean encountered(Remove change) {
            if (change.isSameChild(Add.this)) {
                log.debug("Found equivalent remove ({}).  Canceling.", change);
                delta.removeChange(change);
                if (getIndex() != null) {
                    Reorder reorder = new Reorder();
                    reorder.setToSameChildAs(Add.this);
                    reorder.setNewIndex(getIndex());
                    delta.addChange(reorder);
                    log.debug("Replaced with {}", reorder);
                }
                return true;
            }
            return false;
        }

        private boolean nodeHasChildAlready() {
            // Is the child we're adding already in the target?
            // We check this here because there might have been a remove we needed to cancel
            Collection<PlanTreeNode<?>> children // the second cast is to work around a javac bug
                = (Collection<PlanTreeNode<?>>) PlanTreeInnerNode.cast(delta.getNode()).getChildren();
            for (PlanTreeNode<?> existing : children) {
                if (isSameChild(existing)) {
                    log.debug("Child was already applied to live plan tree.  Will not add again.");
                    return true;
                }
            }
            return false;
        }

        @Override
        public void postProcess(boolean merged) {
            if (!merged && !nodeHasChildAlready()) {
                delta.addChange(Add.this);
            }
        }
    }

    private class AddSibDeletedLogic extends SiblingDeletedLogic {
        private Delta<?> delta;
        private boolean thisAfter;

        public AddSibDeletedLogic(Delta<?> delta, int delIndex, int thisIndex) {
            this.delta = delta;
            thisAfter = delIndex < thisIndex;
        }

        @Override
        public void siblingDeleted(Add change) {
            if (Add.this.getIndex() == null) return;
            if (!thisAfter) return;
            decrementIf(
                (change.getIndex() == null) ||
                (change.getIndex() != null && change.getIndex() < getIndex()));
        }

        @Override
        public void siblingDeleted(Remove change) {
            if (Add.this.getIndex() == null) return;
            if (!thisAfter) return;
            if (delta.getNode() instanceof PlanTreeOrderedInnerNode) {
                int removedElementIndex = ((PlanTreeOrderedInnerNode) delta.getNode()).indexOf(change.getChild());
                if (removedElementIndex < getIndex()) {
                    setIndex(getIndex() + 1);
                }
            }
        }

        @Override
        public void siblingDeleted(Reorder change) {
            if (Add.this.getIndex() == null) return;
            if (!thisAfter) return;
            decrementIf(change.getNewIndex() < getIndex());
        }

        private void decrementIf(boolean condition) {
            if (condition) setIndex(getIndex() - 1);
        }
    }
}