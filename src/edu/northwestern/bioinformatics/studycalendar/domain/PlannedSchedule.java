package edu.northwestern.bioinformatics.studycalendar.domain;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Transient;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.JoinColumn;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.List;
import java.util.ArrayList;

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
    private List<Arm> arms = new ArrayList<Arm>();
    private boolean complete;

    ////// LOGIC

    public void addArm(Arm arm) {
        arms.add(arm);
        arm.setPlannedSchedule(this);
    }

    @Transient
    public int getLengthInDays() {
        int len = 0;
        for (Arm arm : arms) {
            len = Math.max(len, arm.getLengthInDays());
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
    public List<Arm> getArms() {
        return arms;
    }

    public void setArms(List<Arm> arms) {
        this.arms = arms;
    }

    @OneToOne
    @JoinColumn (name = "study_id")
    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        this.study = study;
    }

}
