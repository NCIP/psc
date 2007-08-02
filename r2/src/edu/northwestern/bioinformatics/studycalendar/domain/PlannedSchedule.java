package edu.northwestern.bioinformatics.studycalendar.domain;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
@Entity
@Table (name = "planned_schedules")
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_planned_schedules_id")
    }
)
public class PlannedSchedule extends AbstractDomainObject {
    private Study study;
    private List<Epoch> epochs = new ArrayList<Epoch>();
    private boolean complete;

    ////// LOGIC

    public void addEpoch(Epoch epoch) {
        epochs.add(epoch);
        epoch.setPlannedSchedule(this);
    }

    @Transient
    public int getLengthInDays() {
        int len = 0;
        for (Epoch epoch : epochs) {
            len = Math.max(len, epoch.getLengthInDays());
        }
        return len;
    }

    ////// BEAN PROPERTIES

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    @OneToMany (mappedBy = "plannedSchedule")
    @Cascade (value = { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public List<Epoch> getEpochs() {
        return epochs;
    }

    public void setEpochs(List<Epoch> epochs) {
        this.epochs = epochs;
    }

    @OneToOne
    @JoinColumn (name = "study_id", nullable = false)
    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        if (study != null && study.getPlannedSchedule() != this) {
            study.setPlannedSchedule(this);
        }
        this.study = study;
    }

}
