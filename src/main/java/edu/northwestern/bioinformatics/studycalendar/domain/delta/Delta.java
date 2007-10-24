package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedEvent;
import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.ManyToOne;
import java.util.ArrayList;
import java.util.Collections;
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
public abstract class Delta<T extends PlanTreeNode<?>> extends AbstractMutableDomainObject {
    private List<Change> changes;
    private T node;
    private Revision revision;

    protected Delta() {
        changes = new ArrayList<Change>();
    }

    protected Delta(T node) {
        this();
        this.node = node;
    }

    ////// FACTORY

    @SuppressWarnings({ "unchecked" })
    public static <T extends PlanTreeNode<?>> Delta<T> createDeltaFor(T node, Change... changes) {
        Delta<?> delta;
        if (node instanceof PlannedCalendar) {
            delta = new PlannedCalendarDelta((PlannedCalendar) node);
        } else if (node instanceof Epoch) {
            delta = new EpochDelta((Epoch) node);
        } else if (node instanceof Arm) {
            delta = new ArmDelta((Arm) node);
        } else if (node instanceof Period) {
            delta = new PeriodDelta((Period) node);
        } else if (node instanceof PlannedEvent) {
            delta = new PlannedEventDelta((PlannedEvent) node);
        } else {
            throw new StudyCalendarError("Unimplemented node type: %s", node.getClass().getName());
        }
        delta.addChanges(changes);
        return (Delta<T>) delta;
    }

    ////// LOGIC

    public Delta<T> addChange(Change change) {
        changes.add(change);
        return this;
    }

    public Delta<T> addChanges(Change... newChanges) {
        for (Change c : newChanges) {
            addChange(c);
        }
        return this;
    }

    public Delta<T> removeChange(Change change) {
        int removedIndex = getChangesInternal().indexOf(change);
        if (getChangesInternal().remove(change)) {
            for (ListIterator<Change> it = getChanges().listIterator(); it.hasNext();) {
                Change sib = it.next();
                int sibOriginalIdx = it.previousIndex();
                if (removedIndex <= sibOriginalIdx) sibOriginalIdx++;
                sib.siblingDeleted(this, change, removedIndex, sibOriginalIdx);
            }
        }
        return this;
    }

    @Transient
    public List<Change> getChanges() {
        return Collections.unmodifiableList(getChangesInternal());
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
}
