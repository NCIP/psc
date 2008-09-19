package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

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
public class PlannedActivity extends PlanTreeNode<Period> implements Comparable<PlannedActivity>, Parent<PlannedActivityLabel, SortedSet<PlannedActivityLabel>> {
    private Activity activity;
    private Population population;
    private Integer day;
    private String details;
    private String condition;
    private SortedSet<PlannedActivityLabel> plannedActivityLabels;

    public PlannedActivity() {
        plannedActivityLabels = new TreeSet<PlannedActivityLabel>();
    }

    ////// LOGIC

    public Class<Period> parentClass() { return Period.class; }

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

    public PlannedActivityLabel removePlannedActivityLabel(PlannedActivityLabel paLabel) {
        if (getPlannedActivityLabels().remove(paLabel)) {
            paLabel.setParent(null);
            return paLabel;
        } else {
            return null;
        }
    }
    
    @Transient
    public List<Integer> getDaysInStudySegment() {
        int dayInStudySegment = getPeriod().getStartDay() + getDay() - 1;
        List<Integer> days = new ArrayList<Integer>();
        while (days.size() < getPeriod().getRepetitions()) {
            days.add(dayInStudySegment);
            dayInStudySegment += getPeriod().getDuration().getDays();
        }
        return days;
    }

    @Transient
    public ScheduledActivityMode getInitialScheduledMode() {
        if (StringUtils.isBlank(getCondition())) {
            return ScheduledActivityMode.SCHEDULED;
        } else {
            return ScheduledActivityMode.CONDITIONAL;
        }
    }

    /**
     * Returns all the labels that are used for any repetition in the planned activity
     * @return
     */
    @Transient
    public SortedSet<String> getLabels() {
        SortedSet<String> labels = new TreeSet<String>();
        for (PlannedActivityLabel paLabel : getPlannedActivityLabels()) {
            labels.add(paLabel.getLabel());
        }
        return labels;
    }

    @Transient
    public List<SortedSet<String>> getLabelsByRepetition() {
        if (getParent() == null) {
            throw new StudyCalendarSystemException("This method does not work unless the planned activity is part of a period");
        }
        int reps = getParent().getRepetitions();
        List<SortedSet<String>> byReps = new ArrayList<SortedSet<String>>(reps);
        while (byReps.size() < getParent().getRepetitions()) {
            int rep = byReps.size();
            SortedSet<String> labels = new TreeSet<String>(PlannedActivityLabel.labelOrder());
            for (PlannedActivityLabel paLabel : getPlannedActivityLabels()) {
                if (paLabel.getRepetitionNumber() == null || paLabel.getRepetitionNumber() == rep) {
                    labels.add(paLabel.getLabel());
                }
            }
            byReps.add(labels);
        }
        return byReps;
    }

    public Class<PlannedActivityLabel> childClass() {
        return PlannedActivityLabel.class;
    }

    public void addChild(PlannedActivityLabel child) {
        addPlannedActivityLabel(child);
    }

    public PlannedActivityLabel removeChild(PlannedActivityLabel child) {
        return removePlannedActivityLabel(child);
    }

    @Transient
    public SortedSet<PlannedActivityLabel> getChildren() {
        return getPlannedActivityLabels();
    }

    public void setChildren(SortedSet<PlannedActivityLabel> children) {
        setPlannedActivityLabels(children);
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
    @Cascade(value = { CascadeType.ALL })
    @Sort(type = SortType.NATURAL)
    public SortedSet<PlannedActivityLabel> getPlannedActivityLabels() {
        return plannedActivityLabels;
    }

    public void setPlannedActivityLabels(SortedSet<PlannedActivityLabel> plannedActivityLabels){
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
        PlannedActivity clone = (PlannedActivity) super.clone();
        SortedSet<PlannedActivityLabel> clonedPlannedActivityLabels
            = new TreeSet<PlannedActivityLabel>();
        for (PlannedActivityLabel label: getPlannedActivityLabels()){
            clonedPlannedActivityLabels.add(label.clone());
        }
        clone.setPlannedActivityLabels(clonedPlannedActivityLabels);
        return clone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlannedActivity)) return false;

        PlannedActivity that = (PlannedActivity) o;

        if (activity != null ? !activity.equals(that.activity) : that.activity != null)
            return false;
        if (condition != null ? !condition.equals(that.condition) : that.condition != null)
            return false;
        if (day != null ? !day.equals(that.day) : that.day != null) return false;
        if (details != null ? !details.equals(that.details) : that.details != null) return false;
        if (plannedActivityLabels != null ? !plannedActivityLabels.equals(that.plannedActivityLabels) : that.plannedActivityLabels != null)
            return false;
        if (population != null ? !population.equals(that.population) : that.population != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        result = (activity != null ? activity.hashCode() : 0);
        result = 31 * result + (population != null ? population.hashCode() : 0);
        result = 31 * result + (day != null ? day.hashCode() : 0);
        result = 31 * result + (details != null ? details.hashCode() : 0);
        result = 31 * result + (condition != null ? condition.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return new StringBuilder(getClass().getSimpleName()).
            append("[id=").append(getId()).
            append("; activity=").append(getActivity()).
            append("; day=").append(getDay()).
            append("; population=").append(getPopulation() == null ? "<none>" : getPopulation().getAbbreviation()).
            append("; labels=").append(getLabels() == null ? "<none>" : getLabels()).
            append(']').toString();
    }
}
