package edu.northwestern.bioinformatics.studycalendar.domain;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.IndexColumn;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.List;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;

/**
 * @author Rhett Sutphin
 */
@Entity
@Table (name = "planned_calendars")
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_planned_calendars_id")
    }
)
public class PlannedCalendar extends PlanTreeOrderedInnerNode<Study, Epoch> {
    private boolean complete;
    private Amendment amendment;

    public PlannedCalendar() {
        amendment = new Amendment("[Original]");
    }

    ////// LOGIC

    @Override
    public Class<Epoch> childClass() { return Epoch.class; }

    public void addEpoch(Epoch epoch) {
        addChild(epoch);
    }

    @Transient
    public int getLengthInDays() {
        int len = 0;
        for (Epoch epoch : getEpochs()) {
            len = Math.max(len, epoch.getLengthInDays());
        }
        return len;
    }

    @Transient
    public String getName() {
        return getStudy() == null ? null : getStudy().getName();
    }

    @Transient
    public int getMaxArmCount() {
        int max = 0;
        for (Epoch epoch : getEpochs()) {
            max = Math.max(max, epoch.getArms().size());
        }
        return max;
    }

    @Override
    public PlannedCalendar transientClone() {
        PlanTreeNode<Study> clone = super.transientClone();
        clone.setParent(null);
        return (PlannedCalendar) clone;
    }

    ////// BEAN PROPERTIES

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    // This is annotated this way so that the IndexColumn will work with
    // the bidirectional mapping.  See section 2.4.6.2.3 of the hibernate annotations docs.
    @OneToMany
    @JoinColumn(name="planned_calendar_id", nullable=false)
    @IndexColumn(name="list_index")
    @Cascade(value = { CascadeType.ALL, CascadeType.DELETE_ORPHAN})
    public List<Epoch> getEpochs() {
        return getChildren();
    }

    public void setEpochs(List<Epoch> epochs) {
        setChildren(epochs);
    }

    @OneToOne
    @JoinColumn (name = "study_id", nullable = false)
    public Study getStudy() {
        return getParent();
    }

    public void setStudy(Study study) {
        if (study != null && study.getPlannedCalendar() != this) {
            study.setPlannedCalendar(this);
        }
        setParent(study);
    }

    @Transient // TODO: persistence
    public Amendment getAmendment() {
        return amendment;
    }

    public void setAmendment(Amendment amendment) {
        this.amendment = amendment;
    }
}
