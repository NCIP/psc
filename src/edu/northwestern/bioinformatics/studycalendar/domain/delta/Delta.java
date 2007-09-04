package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

import org.hibernate.annotations.*;

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
    private Revision revision;
    private List<Change> changes;
    private T node;

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
        } else {
            throw new StudyCalendarError("Unimplemented node type: %s", node.getClass().getName());
        }
        delta.getChanges().addAll(Arrays.asList(changes));
        return (Delta<T>) delta;
    }

    ////// LOGIC

    ////// BEAN PROPERTIES

    public void addChange(Change change ) {
        changes.add(change);
    }

    @Transient
    public T getNode() {
        return node;
    }

    public void setNode(T node) {
        this.node = node;
    }

    @Transient
    public Revision getRevision() {
        return revision;
    }

    public void setRevision(Revision revision) {
        this.revision = revision;
    }
    @OneToMany
    @JoinColumn(name = "delta_id", nullable = false)
    @OrderBy // order by ID for testing consistency
    @Cascade(value = { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public List<Change> getChanges() {
        return changes;
    }

    public void setChanges(List<Change> changes) {
        this.changes = changes;
    }

    ////// OBJECT METHODS

    @Override
    public String toString() {
        return  new StringBuilder(getClass().getSimpleName())
            .append("[node=").append(getNode()).append(']').toString();
    }
}
