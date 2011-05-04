package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.Child;
import edu.northwestern.bioinformatics.studycalendar.domain.Parent;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeOrderedInnerNode;
import edu.northwestern.bioinformatics.studycalendar.domain.tools.Differences;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.util.Collection;
import java.util.Date;

/**
 * @author Rhett Sutphin
 */
@Entity
@DiscriminatorValue("add")
public class Add extends ChildrenChange {
    private Integer index;

    public static Add create(Child<?> child, int index) {
        Add add = create(child);
        add.setIndex(index);
        return add;
    }

    public static Add create(Child<?> child) {
        Add add = new Add();
        add.setChild(child);
        return add;
    }

    ////// LOGIC

    @Override
    @Transient
    public ChangeAction getAction() { return ChangeAction.ADD; }

    @Override
    @Transient
    public boolean isNoop() {
        return false;
    }

    @Override
    protected MergeLogic createMergeLogic(Delta<?> delta, Date updateTime) {
        return new AddMergeLogic(delta, updateTime);
    }

    @Override
    protected SiblingDeletedLogic createSiblingDeletedLogic(
        Delta<?> delta, Date updateTime, int deletedChangePosition, int thisPreDeletePosition
    ) {
        return new AddSibDeletedLogic(delta, updateTime, deletedChangePosition, thisPreDeletePosition);
    }

    @Override // in order to map
    @Column(name = "new_value", nullable = false)
    public String getChildIdText() {
        return super.getChildIdText();
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
        if (getChild() != null) {
            sb.append("; child ").append(getChild());
        }
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

        if (index != null ? !index.equals(add.getIndex()) : add.getIndex() != null) return false;

        return true;
    }

    @Override
    public Differences deepEquals(Change o) {
        Differences differences =  new Differences();
        if (this == o) return differences;
        if (o == null || getClass() != o.getClass()) {
            differences.addMessage("object is not an instance of Add");
            return differences;
        }

        Add add = (Add) o;

        differences.registerValueDifference("index", getIndex(), add.getIndex());
        if (getChild() != null) {
            differences.recurseDifferences("child", getChild(), add.getChild());
        }

        return differences;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (index != null ? index.hashCode() : 0);
        return result;
    }

    private class AddMergeLogic extends MergeLogic {
        private final Delta<?> delta;
        private Date updateTime;

        public AddMergeLogic(Delta<?> delta, Date updateTime) {
            this.delta = delta;
            this.updateTime = updateTime;
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
                delta.removeChange(change, updateTime);
                if (getIndex() != null) {
                    Reorder reorder = new Reorder();
                    reorder.setToSameChildAs(Add.this);
                    reorder.setNewIndex(getIndex());
                    reorder.setUpdatedDate(updateTime);
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
            Collection<Child<?>> children = ((Parent) delta.getNode()).getChildren();
            for (Child<?> existing : children) {
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
                Add.this.setUpdatedDate(updateTime);
                delta.addChange(Add.this);
            }
        }
    }

    private class AddSibDeletedLogic extends SiblingDeletedLogic {
        private Delta<?> delta;
        private Date updateTime;
        private boolean thisAfter;

        public AddSibDeletedLogic(Delta<?> delta, Date updateTime, int delIndex, int thisIndex) {
            this.delta = delta;
            this.updateTime = updateTime;
            thisAfter = delIndex < thisIndex;
        }

        @Override
        public void siblingDeleted(Add change) {
            if (notApplicable()) return;
            decrementIf(
                (change.getIndex() == null) ||
                (change.getIndex() != null && change.getIndex() < getIndex()));
        }

        @Override
        @SuppressWarnings({ "RawUseOfParameterizedType", "unchecked" })
        public void siblingDeleted(Remove change) {
            if (notApplicable()) return;
            if (delta.getNode() instanceof PlanTreeOrderedInnerNode) {
                int removedElementIndex = ((PlanTreeOrderedInnerNode) delta.getNode()).indexOf((PlanTreeNode) change.getChild());
                incrementIf(removedElementIndex <= getIndex());
            }
        }

        @Override
        public void siblingDeleted(Reorder change) {
            if (notApplicable()) return;
            boolean within = change.isBetweenIndexesOrAtMax(getIndex()); // down
            if (within) {
                decrementIf(change.isMoveUp());
                incrementIf(change.isMoveDown());
            }
        }

        private boolean notApplicable() {
            return Add.this.getIndex() == null || !thisAfter;
        }

        private void incrementIf(boolean condition) {
            if (condition) {
                setIndex(getIndex() + 1);
                Add.this.setUpdatedDate(updateTime);
            }
        }

        private void decrementIf(boolean condition) {
            if (condition) {
                setIndex(getIndex() - 1);
                Add.this.setUpdatedDate(updateTime);
            }
        }
    }
}