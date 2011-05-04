package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.Child;
import edu.northwestern.bioinformatics.studycalendar.domain.Parent;
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
@DiscriminatorValue("remove")
public class Remove extends ChildrenChange {

    public static Remove create(Child<?> object) {
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

    public Differences deepEquals(Change o) {
        Differences differences =  new Differences();
        if (this == o) return differences;
        if (o == null || getClass() != o.getClass()) {
            differences.addMessage("object is not an instance of Remove");
            return differences;
        }

        Remove remove = (Remove) o;

        if (getChild() != null && remove.getChild() != null) {
            if (getChild().getGridId() != null ? !getChild().getGridId().equals(remove.getChild().getGridId())
                    : remove.getChild().getGridId() != null) {
                differences.addMessage("for different child");
            }
        }
        return differences;
    }

    @Override
    protected MergeLogic createMergeLogic(Delta<?> delta, Date updateTime) {
        return new RemoveMergeLogic(delta, updateTime);
    }

    ////// BEAN PROPERTIES

    @Override
    @Column(name = "old_value", nullable = false)
    public String getChildIdText() {
        return super.getChildIdText();
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
        private Date updateTime;
        private Reorder precedingReorder;
        private boolean hadPrecedingAdd;

        public RemoveMergeLogic(Delta<?> delta, Date updateTime) {
            this.delta = delta;
            this.updateTime = updateTime;
        }

        @Override
        public boolean encountered(Add change) {
            if (change.isSameChild(Remove.this)) {
                log.debug("Found equivalent add ({}).  Canceling.", change);
                delta.removeChange(change, updateTime);
                hadPrecedingAdd = true;
                return true;
            }
            return false;
        }

        @Override
        public boolean encountered(Reorder change) {
            if (change.isSameChild(Remove.this)) {
                precedingReorder = change;
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
            Collection<Child<?>> children = ((Parent) delta.getNode()).getChildren();
            for (Child<?> existing : children) {
                if (isSameChild(existing)) {
                    return true;
                }
            }
            log.debug("Child does not exist in live plan tree.  Will not remove.");
            return false;
        }

        @Override
        public void postProcess(boolean merged) {
            if (hadPrecedingAdd && precedingReorder != null) {
                delta.removeChange(precedingReorder, updateTime);
            }
            if (!merged && nodeHasChildToRemove()) {
                Remove.this.setUpdatedDate(updateTime);
                delta.addChange(Remove.this);
            }
        }
    }
}
