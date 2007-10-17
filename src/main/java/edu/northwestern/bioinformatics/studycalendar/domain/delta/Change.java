package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;

import javax.persistence.*;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Rhett Sutphin
 */
@Entity
@Table(name = "changes")
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_changes_id")
    }
)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="action", discriminatorType = DiscriminatorType.STRING)
public abstract class Change extends AbstractMutableDomainObject {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Return the action used by this change.  It should match the discriminator value for the class.
     * @return
     */
    @Transient
    public abstract ChangeAction getAction();

    /**
     * Integrate the change embodied in this object into the given {@link Delta}.
     * This may be as simple as adding the change to the delta's change list, but
     * may extend to removing or modifying other changes.  Or, if this change is
     * already represented in the delta, this method may do nothing.
     * @param delta
     */
    public void mergeInto(Delta<?> delta) {
        log.debug("Merging {} into {}", this, delta);
        List<Change> changes = delta.getChanges();
        boolean merged = false;
        MergeLogic logic = createMergeLogic(delta);
        // walk back through changes until the merge logic says to stop
        for (int i = changes.size() - 1; i >= 0; i--) {
            Change c = changes.get(i);
            if (c.getAction() == ChangeAction.ADD) {
                merged = logic.encountered((Add) c);
            } else if (c.getAction() == ChangeAction.REMOVE) {
                merged = logic.encountered((Remove) c);
            } else if (c.getAction() == ChangeAction.REORDER) {
                merged = logic.encountered((Reorder) c);
            } else if (c.getAction() == ChangeAction.CHANGE_PROPERTY) {
                merged = logic.encountered((PropertyChange) c);
            }
            if (merged) break;
        }
        logic.postProcess(merged);
    }

    protected abstract MergeLogic createMergeLogic(Delta<?> delta);

    /**
     * Notifies this change that another change in the same delta was removed.
     *
     * @param parent
     * @param deleted
     * @param deletedChangePosition The index of the deleted change in the delta (before it was deleted)
     * @param thisPreDeletePosition The index of this change in the delta (before the sibling was deleted)
     */
    public void siblingDeleted(Delta<?> parent, Change deleted, int deletedChangePosition, int thisPreDeletePosition) {
        SiblingDeletedLogic logic = createSiblingDeletedLogic(parent, deletedChangePosition, thisPreDeletePosition);
        if (deleted.getAction() == ChangeAction.ADD) {
            logic.siblingDeleted((Add) deleted);
        } else if (deleted.getAction() == ChangeAction.REMOVE) {
            logic.siblingDeleted((Remove) deleted);
        } else if (deleted.getAction() == ChangeAction.REORDER) {
            logic.siblingDeleted((Reorder) deleted);
        } else if (deleted.getAction() == ChangeAction.CHANGE_PROPERTY) {
            logic.siblingDeleted((PropertyChange) deleted);
        }
    }

    // TODO: this will be abstract
    protected SiblingDeletedLogic createSiblingDeletedLogic(Delta<?> delta, int deletedChangePosition, int thisPreDeletePosition) {
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * Visitor-style template class for children to define their merge behavior.
     * {@link Change#mergeInto} will walk backwards over the list of changes in
     * the target delta, passing each one to the appropriate method in this class.
     * It will stop when one of the <code>encountered</code> methods returns true.
     * Once the walk is complete, {@link #postProcess} is called, passing
     * <code>true</code> if any of the <code>encountered</code> methods returned
     * true.
     */
    protected abstract class MergeLogic {
        public boolean encountered(Add change)             { return false; }
        public boolean encountered(Remove change)          { return false; }
        public boolean encountered(Reorder change)         { return false; }
        public boolean encountered(PropertyChange change)  { return false; }
        public void postProcess(boolean merged) { }
    }

    protected abstract class SiblingDeletedLogic {
        public void siblingDeleted(Add change)             { }
        public void siblingDeleted(Remove change)          { }
        public void siblingDeleted(Reorder change)         { }
        public void siblingDeleted(PropertyChange change)  { }
    }
}
