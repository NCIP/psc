package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;

import javax.persistence.Entity;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Transient;
import javax.persistence.Column;

/**
 * A {@link Change} representing the reordering of the node's children.  Specifically,
 * it defines the move of a child from its current position to another in a list.
 * <p>
 * To enable application of the change both forward and backward, both the new index
 * and the old index are included.  For ease of implementation, a reference to the child
 * to move is also included.  The reference to the child should trump the indexes.  That
 * is, if a mutator is performing an apply operation, it should ensure that the referenced
 * child ends up at the new index (not the element at old index, in the event there is a conflict). 
 *
 * @author Rhett Sutphin
 */
@Entity
@DiscriminatorValue("reorder")
public class Reorder extends ChildrenChange {
    private Integer newIndex;
    private Integer oldIndex;

    /** Factory method */
    public static Reorder create(PlanTreeNode<?> node, int oldIndex, int newIndex) {
        Reorder created = new Reorder();
        created.setChild(node);
        created.setOldIndex(oldIndex);
        created.setNewIndex(newIndex);
        return created;
    }

    @Transient
    @Override
    public ChangeAction getAction() { return ChangeAction.REORDER; }

    @Override
    protected MergeLogic createMergeLogic(Delta<?> delta) {
        return new ReorderMergeLogic(delta);
    }

    @Override
    @Column(name="attribute", nullable = false)
    public Integer getChildId() {
        return super.getChildId();
    }

    ////// BEAN PROPERTIES

    @Column(name="new_value")
    public Integer getNewIndex() {
        return newIndex;
    }

    public void setNewIndex(Integer newIndex) {
        this.newIndex = newIndex;
    }

    @Column(name="old_value")
    public Integer getOldIndex() {
        return oldIndex;
    }

    public void setOldIndex(Integer oldIndex) {
        this.oldIndex = oldIndex;
    }

    ////// OBJECT METHODS
    
    @Override
    public String toString() {
        return new StringBuilder(getClass().getSimpleName())
            .append("[id=").append(getId()).append("; move child id ").append(getChildId())
            .append(" from ").append(getOldIndex()).append(" to ").append(getNewIndex())
            .append(']')
            .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Reorder reorder = (Reorder) o;

        if (newIndex != null ? !newIndex.equals(reorder.newIndex) : reorder.newIndex != null)
            return false;
        if (oldIndex != null ? !oldIndex.equals(reorder.oldIndex) : reorder.oldIndex != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (newIndex != null ? newIndex.hashCode() : 0);
        result = 31 * result + (oldIndex != null ? oldIndex.hashCode() : 0);
        return result;
    }

    private class ReorderMergeLogic extends MergeLogic {
        private Delta<?> delta;
        private boolean merged = false;

        public ReorderMergeLogic(Delta<?> delta) {
            this.delta = delta;
        }

        @Override
        public boolean encountered(Reorder change) {
            if (change.isSameChild(Reorder.this)) {
                change.setNewIndex(getNewIndex());
                merged = true;
            }
            return true;
        }

        @Override public boolean encountered(Add change) { return true; }

        @Override public boolean encountered(Remove change) { return true; }

        @Override
        public void postProcess(boolean shortCircuited) {
            if (!merged || !shortCircuited) {
                delta.addChanges(Reorder.this);
            }
        }
    }
}
