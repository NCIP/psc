package edu.northwestern.bioinformatics.studycalendar.domain;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Rhett Sutphin
 * @author Yufang Wang
 */
@Entity
@Table (name = "planned_activities")
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_planned_activities_id")
    }
)
public class PlannedActivity extends PlanTreeNode<Period> implements Comparable<PlannedActivity> {
    private Activity activity;
    private Population population;
    private Integer day;
    private String details;
    private String condition;
    private List<PlannedActivityLabel> plannedActivityLabels;

    public PlannedActivity() {
        plannedActivityLabels = new ArrayList<PlannedActivityLabel>();
    }

    ////// LOGIC

    @Override public Class<Period> parentClass() { return Period.class; }

    public int compareTo(PlannedActivity other) {
        // by day
        int dayDiff = getDay() - other.getDay();
        if (dayDiff != 0) return dayDiff;
        // then by activity
        return getActivity().compareTo(other.getActivity());
    }

    public void addPlannedActivityLabel(PlannedActivityLabel paLabel) {
        paLabel.setPlannedActivity(this);
        getPlannedActivityLabels().add(paLabel);
    }
    
    @Transient
    public List<Integer> getDaysInStudySegment() {
        int day = getDay();
        int actualDay = calculateDay(day);
        List<Integer> days = new ArrayList<Integer>();
        while (days.size() < getPeriod().getRepetitions()) {
            days.add(actualDay);
            actualDay += getPeriod().getDuration().getDays();
        }
        return days;
    }

    private int calculateDay(int day) {
        int daysInDuration = getPeriod().getDuration().getUnit().inDays();
        int dayToReturn;
        if(daysInDuration != 1 && day > 1) {
            int multiplication = day-1;
            dayToReturn = daysInDuration * multiplication + getPeriod().getStartDay();
        } else {
            //else case is for days, where everything should stay as before. no shifting is needed
            dayToReturn =  getPeriod().getStartDay() + getDay() - 1;
        }
        return dayToReturn;
    }

    @Transient
    public ScheduledActivityMode getInitialScheduledMode() {
        if (StringUtils.isBlank(getCondition())) {
            return ScheduledActivityMode.SCHEDULED;
        } else {
            return ScheduledActivityMode.CONDITIONAL;
        }
    }

    public List<Label> getLabels() {
        List<Label> labels = new ArrayList<Label>(getPlannedActivityLabels().size());
        for (PlannedActivityLabel paLabel : getPlannedActivityLabels()) {
            labels.add(paLabel.getLabel());
        }
        return labels;
    }

    public List<String> getLabelNames() {
        List<String> labels = new ArrayList<String>(getPlannedActivityLabels().size());
        for (PlannedActivityLabel paLabel : getPlannedActivityLabels()) {
            labels.add(paLabel.getLabel().getName());
        }
        return labels;
    }

    ////// BEAN PROPERTIES

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "activity_id")
    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }                                                                                            

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "period_id")
    public Period getPeriod() {
        return getParent();
    }

    public void setPeriod(Period period) {
        setParent(period);
    }

    @ManyToOne
    public Population getPopulation() {
        return population;
    }

    @OneToMany(mappedBy = "plannedActivity")
    @OrderBy // order by ID for testing consistency
    // TODO: why isn't this just "ALL"?
    @Cascade(value = { org.hibernate.annotations.CascadeType.DELETE, org.hibernate.annotations.CascadeType.LOCK, org.hibernate.annotations.CascadeType.MERGE,
            org.hibernate.annotations.CascadeType.PERSIST, org.hibernate.annotations.CascadeType.REFRESH, org.hibernate.annotations.CascadeType.REMOVE, org.hibernate.annotations.CascadeType.REPLICATE,
            org.hibernate.annotations.CascadeType.SAVE_UPDATE })
    public List<PlannedActivityLabel> getPlannedActivityLabels() {
        return plannedActivityLabels;
    }

    public void setPlannedActivityLabels(List<PlannedActivityLabel> plannedActivityLabels){
        this.plannedActivityLabels = plannedActivityLabels;
    }

    public void setPopulation(Population population) {
        this.population = population;
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }
    
    public String getDetails() {
        return details;
    }
    
    public void setDetails(String details) {
        this.details = details;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    ////// OBJECT METHODS

    @Override
    public PlannedActivity clone() {
        return (PlannedActivity) super.clone();
    }

    @Override
    public String toString() {
        return new StringBuilder(getClass().getSimpleName()).
            append("[id=").append(getId()).
            append("; activity=").append(getActivity()).
            append("; day=").append(getDay()).
            append("; population=").append(getPopulation() == null ? "<none>" : getPopulation().getAbbreviation()).
            append(']').toString();
    }
}
