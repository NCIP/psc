/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.domain.DeepComparable;
import edu.northwestern.bioinformatics.studycalendar.domain.NaturallyKeyed;
import edu.northwestern.bioinformatics.studycalendar.domain.TransientCloneable;
import edu.northwestern.bioinformatics.studycalendar.domain.tools.Differences;
import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Date;
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
public abstract class Change
    extends AbstractMutableDomainObject
    implements Cloneable, TransientCloneable<Change>, DeepComparable<Change>, NaturallyKeyed
{
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private Date updatedDate;
    private Delta<?> delta;
    private boolean memoryOnly;

    /**
     * Return the action used by this change.  It must match the discriminator value for the class.
     * @return
     */
    @Transient
    public abstract ChangeAction getAction();

    /**
     * Return true IFF this change would not affect the targeted node at all.
     * @return
     */
    @Transient
    public abstract boolean isNoop();

    @Transient
    public abstract Differences deepEquals(Change o);

    /**
     * Integrate the change embodied in this object into the given {@link Delta}.
     * This may be as simple as adding the change to the delta's change list, but
     * may extend to removing or modifying other changes.  Or, if this change is
     * already represented in the delta, this method may do nothing.
     * @param targetDelta
     * @param updateTime
     */
    public void mergeInto(Delta<?> targetDelta, Date updateTime) {
        if (this.isNoop()) return;
        log.debug("Merging {} into {}", this, targetDelta);
        List<Change> changes = targetDelta.getChanges();
        boolean merged = false;
        MergeLogic logic = createMergeLogic(targetDelta, updateTime);
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

    protected abstract MergeLogic createMergeLogic(Delta<?> delta, Date updateTime);

    /**
     * Notifies this change that another change in the same delta was removed.
     *
     * @param parent
     * @param deleted
     * @param deletedChangePosition The index of the deleted change in the delta (before it was deleted)
     * @param thisPreDeletePosition The index of this change in the delta (before the sibling was deleted)
     * @param updateTime
     */
    public void siblingDeleted(Delta<?> parent, Change deleted, int deletedChangePosition, int thisPreDeletePosition, Date updateTime) {
        SiblingDeletedLogic logic = createSiblingDeletedLogic(parent, updateTime, deletedChangePosition, thisPreDeletePosition);
        if (logic == null) return;
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

    protected SiblingDeletedLogic createSiblingDeletedLogic(Delta<?> delta, Date updateTime, int deletedChangePosition, int thisPreDeletePosition) {
        return null;
    }

    ////// IMPLEMENTATION OF TransientCloneable

    @Transient
    public boolean isMemoryOnly() {
        return memoryOnly;
    }

    public void setMemoryOnly(boolean memoryOnly) {
        this.memoryOnly = memoryOnly;
    }

    public Change transientClone() {
        Change clone = clone();
        clone.setMemoryOnly(true);
        return clone;
    }

    ////// BEAN PROPERTIES

    @ManyToOne
    @JoinColumn(insertable=false, updatable=false)
    public Delta<?> getDelta() {
        return delta;
    }

    public void setDelta(Delta<?> delta) {
        this.delta = delta;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    ////// OBJECT METHODS

    @Override
    public Change clone() {
        try {
            Change clone = (Change) super.clone();
            clone.setDelta(null);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new StudyCalendarError("Clone is supported", e);
        }
    }

    ////// INNER CLASSES

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
