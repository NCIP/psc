package edu.northwestern.bioinformatics.studycalendar.domain;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.apache.commons.lang.StringUtils;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.FetchType;
import javax.persistence.Transient;
import java.util.List;
import java.util.ArrayList;

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
    private Integer day;
    private String details;
    private String condition;

    ////// LOGIC

    @Override public Class<Period> parentClass() { return Period.class; }

    public int compareTo(PlannedActivity other) {
        // by day
        int dayDiff = getDay() - other.getDay();
        if (dayDiff != 0) return dayDiff;
        // then by activity
        return getActivity().compareTo(other.getActivity());
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

    ////// BEAN PROPERTIES

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
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
}
