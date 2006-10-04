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
@Table (name = "planned_calendars")
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_planned_calendars_id")
    }
)
public class PlannedCalendar extends AbstractDomainObject {
    private Study study;
    private List<Epoch> epochs = new ArrayList<Epoch>();
    private boolean complete;

    ////// LOGIC

    public void addEpoch(Epoch epoch) {
        epochs.add(epoch);
        epoch.setPlannedCalendar(this);
    }

    @Transient
    public int getLengthInDays() {
        int len = 0;
        for (Epoch epoch : epochs) {
            len = Math.max(len, epoch.getLengthInDays());
        }
        return len;
    }

    @Transient
    public String getName() {
        return study == null ? null : study.getName();
    }

    @Transient
    public int getMaxArmCount() {
        int max = 0;
        for (Epoch epoch : epochs) {
            max = Math.max(max, epoch.getArms().size());
        }
        return max;
    }

    ////// BEAN PROPERTIES

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    @OneToMany (mappedBy = "plannedCalendar")
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
        if (study != null && study.getPlannedCalendar() != this) {
            study.setPlannedCalendar(this);
        }
        this.study = study;
    }

}
