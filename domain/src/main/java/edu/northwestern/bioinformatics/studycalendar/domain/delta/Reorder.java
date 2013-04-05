/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeOrderedInnerNode;
import edu.northwestern.bioinformatics.studycalendar.domain.tools.Differences;
import gov.nih.nci.cabig.ctms.lang.ComparisonTools;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.util.Date;

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

    ////// LOGIC

    @Override
    @Transient
    public boolean isNoop() {
        // Null-safety is belt-and-suspenders here; it's not legal for a
        // reorder to have null for either value
        return ComparisonTools.nullSafeEquals(getOldIndex(), getNewIndex());
    }

    @Transient
    public boolean isMoveUp() {
        return getNewIndex() < getOldIndex();
    }

    @Transient
    public boolean isMoveDown() {
        return getOldIndex() < getNewIndex();
    }

    @Transient
    public boolean isBetweenIndexesOrAtMax(int value) {
        return (getOldIndex() >= value && value > getNewIndex())  // up
            || (getOldIndex() < value && value <= getNewIndex()); // down
    }

    @Transient
    @Override
    public ChangeAction getAction() { return ChangeAction.REORDER; }

    @Override
    protected MergeLogic createMergeLogic(Delta<?> delta, Date updateTime) {
        return new ReorderMergeLogic(delta, updateTime);
    }

    @Override
    protected SiblingDeletedLogic createSiblingDeletedLogic(Delta<?> delta, Date updateTime, int deletedChangePosition, int thisPreDeletePosition) {
        return new ReorderSibDeletedLogic(delta, updateTime, deletedChangePosition, thisPreDeletePosition);
    }

    @Override
    @Column(name="attribute", nullable = false)
    public String getChildIdText() {
        return super.getChildIdText();
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

    public Differences deepEquals(Change o) {
        Differences differences =  new Differences();
        if (this == o) return differences;
        if (o == null || getClass() != o.getClass()) {
            differences.addMessage("object is not an instance of Reorder");
            return differences;
        }

        Reorder reorder = (Reorder) o;

        if (getChild() != null && reorder.getChild() != null) {
            if (getChild().getGridId() != null ? !getChild().getGridId().equals(reorder.getChild().getGridId())
                    : reorder.getChild().getGridId() != null) {
                differences.addMessage("for different child");
            }
        }

        return differences.
            registerValueDifference("new index", this.getNewIndex(), reorder.getNewIndex()).
            registerValueDifference("old index", this.getOldIndex(), reorder.getOldIndex())
            ;
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

        if (newIndex != null ? !newIndex.equals(reorder.getNewIndex()) : reorder.getNewIndex() != null)
            return false;
        if (oldIndex != null ? !oldIndex.equals(reorder.getOldIndex()) : reorder.getOldIndex() != null)
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
        private Date updateTime;
        private boolean merged = false;

        public ReorderMergeLogic(Delta<?> delta, Date updateTime) {
            this.delta = delta;
            this.updateTime = updateTime;
        }

        @Override
        public boolean encountered(Reorder change) {
            if (change.isSameChild(Reorder.this)) {
                change.setNewIndex(getNewIndex());
                change.setUpdatedDate(updateTime);
                merged = true;
            }
            return true;
        }

        @Override public boolean encountered(Add change) { return true; }

        @Override public boolean encountered(Remove change) { return true; }

        @Override
        public void postProcess(boolean shortCircuited) {
            if (!merged || !shortCircuited) {
                Reorder.this.setUpdatedDate(updateTime);
                delta.addChanges(Reorder.this);
            }
        }
    }

    // This is quasi copied from Add, but different enough that it's difficult to build a
    // shared base class
    private class ReorderSibDeletedLogic extends SiblingDeletedLogic {
        private Delta<?> delta;
        private Date updateTime;
        private boolean thisAfter;
        private BeanWrapper thisWrapped;

        public ReorderSibDeletedLogic(Delta<?> delta, Date updateTime, int delIndex, int thisIndex) {
            this.delta = delta;
            this.updateTime = updateTime;
            thisAfter = delIndex < thisIndex;
            thisWrapped = new BeanWrapperImpl(Reorder.this);
        }

        @Override
        public void siblingDeleted(Add change) {
            if (notApplicable()) return;
            if (change.getIndex() == null) return;
            decrementIf("oldIndex",
                (change.getIndex() != null && change.getIndex() < getOldIndex()));
            decrementIf("newIndex",
                (change.getIndex() != null && change.getIndex() < getNewIndex()));
        }

        @Override
        @SuppressWarnings({ "RawUseOfParameterizedType", "unchecked" })
        public void siblingDeleted(Remove change) {
            if (notApplicable()) return;
            if (delta.getNode() instanceof PlanTreeOrderedInnerNode) {
                int removedElementIndex = ((PlanTreeOrderedInnerNode) delta.getNode()).indexOf((PlanTreeNode) change.getChild());
                incrementIf("oldIndex", removedElementIndex <= getOldIndex());
                incrementIf("newIndex", removedElementIndex <= getNewIndex());
            }
        }

        @Override
        public void siblingDeleted(Reorder change) {
            if (notApplicable()) return;
            boolean within = change.isBetweenIndexesOrAtMax(getOldIndex()); // down
            if (within) {
                decrementIf("oldIndex", change.isMoveUp());
                incrementIf("oldIndex", change.isMoveDown());
            }
        }

        private boolean notApplicable() {
            return !thisAfter;
        }

        private void incrementIf(String property, boolean condition) {
            if (condition) {
                thisWrapped.setPropertyValue(property, getIntProperty(property) + 1);
                Reorder.this.setUpdatedDate(updateTime);
            }
        }

        private void decrementIf(String property, boolean condition) {
            if (condition) {
                thisWrapped.setPropertyValue(property, getIntProperty(property) - 1);
                Reorder.this.setUpdatedDate(updateTime);
            }
        }

        private Integer getIntProperty(String property) {
            return (Integer) thisWrapped.getPropertyValue(property);
        }
    }
}
