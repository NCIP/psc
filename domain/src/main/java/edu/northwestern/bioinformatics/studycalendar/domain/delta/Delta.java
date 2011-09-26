package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.domain.DeepComparable;
import edu.northwestern.bioinformatics.studycalendar.domain.TransientCloneable;
import edu.northwestern.bioinformatics.studycalendar.domain.tools.Differences;
import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

/**
 * @author Rhett Sutphin
 */

@Entity
@Table(name = "deltas")
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_deltas_id")
    }
)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="node_type", discriminatorType = DiscriminatorType.STRING)
public abstract class Delta<T extends Changeable>
    extends AbstractMutableDomainObject
    implements Cloneable, TransientCloneable<Delta<T>>, DeepComparable<Delta<T>>
{
    private List<Change> changes;
    private T node;
    private Revision revision;
    private boolean memoryOnly;

    protected Delta() {
        changes = new ArrayList<Change>();
    }

    protected Delta(T node) {
        this();
        this.node = node;
    }

    ////// FACTORY

    @SuppressWarnings({ "unchecked" })
    public static <T extends Changeable> Delta<T> createDeltaFor(T node, Change... changes) {
        Delta<T> delta = DeltaNodeType.valueForNodeClass(node.getClass()).deltaInstance(node);
        delta.addChanges(changes);
        return delta;
    }

    ////// LOGIC

    public Delta<T> addChange(Change change) {
        changes.add(change);
        change.setDelta(this);
        return this;
    }

    public Delta<T> addChanges(Change... newChanges) {
        for (Change c : newChanges) {
            addChange(c);
        }
        return this;
    }

    public Delta<T> removeChange(Change change, Date updateTime) {
        int removedIndex = getChangesInternal().indexOf(change);
        if (getChangesInternal().remove(change)) {
            for (ListIterator<Change> it = getChanges().listIterator(); it.hasNext();) {
                Change sib = it.next();
                int sibOriginalIdx = it.previousIndex();
                if (removedIndex <= sibOriginalIdx) sibOriginalIdx++;
                sib.siblingDeleted(this, change, removedIndex, sibOriginalIdx, updateTime);
            }
            change.setDelta(null);
        }
        return this;
    }

    @Transient
    public List<Change> getChanges() {
        return Collections.unmodifiableList(getChangesInternal());
    }

    @Transient
    public String getBriefDescription() {
        return String.format("delta for %s %s", getNodeTypeDescription(), getNode().getGridId());
    }

    @Transient
    public DeltaNodeType getNodeType() {
        return DeltaNodeType.valueForDeltaClass(getClass());
    }

    @Transient
    public String getNodeTypeDescription() {
        return getNodeType().getNodeTypeName();
    }

    ////// IMPLEMENTATION OF TransientCloneable

    @Transient
    public boolean isMemoryOnly() {
        return memoryOnly;
    }

    public void setMemoryOnly(boolean memoryOnly) {
        this.memoryOnly = memoryOnly;
        for (Change change : getChanges()) {
            change.setMemoryOnly(memoryOnly);
        }
    }

    public Delta<T> transientClone() {
        Delta<T> clone = clone();
        clone.setMemoryOnly(true);
        return clone;
    }

    ////// BEAN PROPERTIES

    @Transient // here only -- mapped in subclasses
    public T getNode() {
        return node;
    }

    public void setNode(T node) {
        this.node = node;
    }

    @OneToMany
    @JoinColumn(name = "delta_id", nullable = false)
    @OrderBy // order by ID for testing consistency // TODO: explicit ordering
    @Cascade(value = { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    protected List<Change> getChangesInternal() {
        return changes;
    }

    protected void setChangesInternal(List<Change> changes) {
        this.changes = changes;
    }

    @ManyToOne(targetEntity = Amendment.class) // temporary, of course
    @JoinColumn(name = "amendment_id", insertable = false, updatable = false)
    public Revision getRevision() {
        return revision;
    }

    public void setRevision(Revision revision) {
        this.revision = revision;
    }

    ////// OBJECT METHODS

    @Override
    public String toString() {
        return  new StringBuilder(getClass().getSimpleName())
            .append("[node=").append(getNode()).append(']').toString();
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    public Delta<T> clone() {
        try {
            Delta<T> clone = (Delta<T>) super.clone();
            clone.setRevision(null);
            clone.setChangesInternal(new ArrayList<Change>(getChangesInternal().size()));
            for (Change change : getChangesInternal()) {
                clone.addChange(change.clone());
            }
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new StudyCalendarError("Clone is supported", e);
        }
    }

    public Differences deepEquals(Delta<T> that) {
        Differences differences =  new Differences();

        if (getNode() != null && that.getNode() != null) {
            if (getNode().getGridId() != null ? !getNode().getGridId().equals(that.getNode().getGridId())
                    : that.getNode().getGridId() != null) {
                differences.addMessage("for different node");
            }
        }

        for (int i = 0; i < Math.max(that.getChanges().size(), getChanges().size()); i++) {
            Change left =  getOrNull(this.getChanges(), i);
            Change right = getOrNull(that.getChanges(), i);
            if (left == null) {
                differences.recurseDifferences(right.getNaturalKey(), null, right);
            } else if (right == null) {
                differences.recurseDifferences(left.getNaturalKey(), left, null);
            } else if (left.getNaturalKey().equals(right.getNaturalKey())) {
                differences.recurseDifferences(left.getNaturalKey(), left, right);
            } else {
                differences.addMessage(
                    "%s replaced by %s", left.getNaturalKey(), right.getNaturalKey());
            }
        }

        return differences;
    }

    private <T> T getOrNull(List<T> l, int i) {
        return i < l.size() ? l.get(i) : null;
    }

    //Object Methods

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Delta)) return false;

        Delta delta = (Delta) o;
        if (node != null ? !node.equals(delta.getNode()) : delta.getNode() != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = node != null ? node.hashCode() : 0;
        return result;
    }
}
