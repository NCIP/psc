package edu.northwestern.bioinformatics.studycalendar.domain;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.FetchType;
import javax.persistence.Transient;
import java.util.List;
import java.util.ArrayList;

import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;

/**
 * @author Rhett Sutphin
 * @author Yufang Wang
 *  */
@Entity
@Table (name = "planned_events")
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_planned_events_id")
    }
)
public class PlannedEvent extends PlanTreeNode<Period> implements Comparable<PlannedEvent> {
    private Activity activity;
    private Integer day;
    private String details;
    private String conditionalDetails;
    private Boolean conditional;

    ////// LOGIC

    public int compareTo(PlannedEvent other) {
        // by day
        int dayDiff = getDay() - other.getDay();
        if (dayDiff != 0) return dayDiff;
        // then by activity
        return getActivity().compareTo(other.getActivity());
    }

    @Transient
    public List<Integer> getDaysInArm() {
        int dayInArm = getPeriod().getStartDay() + getDay() - 1;
        List<Integer> days = new ArrayList<Integer>();
        while (days.size() < getPeriod().getRepetitions()) {
            days.add(dayInArm);
            dayInArm += getPeriod().getDuration().getDays();
        }
        return days;
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

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
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

    public String getConditionalDetails() {
        return conditionalDetails;
    }

    public void setConditionalDetails(String conditionalDetails) {
        this.conditionalDetails = conditionalDetails;
    }

    public Boolean getConditional() {
        return conditional;
    }

    public void setConditional(Boolean conditional) {
        this.conditional = conditional;
    }
}
