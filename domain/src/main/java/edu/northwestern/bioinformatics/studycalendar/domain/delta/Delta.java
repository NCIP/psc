package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivityLabel;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
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
    implements Cloneable, TransientCloneable<Delta<T>>
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
        Delta<?> delta;
        if (node instanceof PlannedCalendar) {
            delta = new PlannedCalendarDelta((PlannedCalendar) node);
        } else if (node instanceof Epoch) {
            delta = new EpochDelta((Epoch) node);
        } else if (node instanceof StudySegment) {
            delta = new StudySegmentDelta((StudySegment) node);
        } else if (node instanceof Period) {
            delta = new PeriodDelta((Period) node);
        } else if (node instanceof PlannedActivity) {
            delta = new PlannedActivityDelta((PlannedActivity) node);
        } else if (node instanceof PlannedActivityLabel) {
            delta = new PlannedActivityLabelDelta((PlannedActivityLabel) node);
        } else if (node instanceof Population) {
            delta = new PopulationDelta((Population) node);
        } else if (node instanceof Study) {
            delta = new StudyDelta((Study)node);
        } else {
            throw new StudyCalendarError("Unimplemented changeable type: %s", node.getClass().getName());
        }
        delta.addChanges(changes);
        return (Delta<T>) delta;
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

    public Differences deepEquals(Object o) {
        Differences differences =  new Differences();
        if (this == o) return differences;
        if (!(o instanceof Delta)) {
            differences.addMessage("object is not instance of " +getClass());
            return differences;
        }

        Delta delta = (Delta) o;
        if (getNode() != null && delta.getNode() != null) {
            if (getNode().getGridId() != null ? !getNode().getGridId().equals(delta.getNode().getGridId())
                    : delta.getNode().getGridId() != null) {
                differences.addMessage("for different node");
            }
        }

        if (getChanges().size() != delta.getChanges().size()) {
            differences.addMessage(String.format("total no. of changes of delta %d differs to %d",
                        getChanges().size(), delta.getChanges().size()));
        } else {
            for (int i=0; i<getChanges().size(); i++) {
                Change change =  getChanges().get(i);
                Change matchingChange = (Change)delta.getChanges().get(i);
                Differences changeDifferences = change.deepEquals(matchingChange);
                if (changeDifferences.hasDifferences()) {
                    differences.addChildDifferences(change.getAction().getDisplayName(), changeDifferences);
                }
            }
        }
        return differences;
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
