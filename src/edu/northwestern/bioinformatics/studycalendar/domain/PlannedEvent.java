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

/**
 * @author Rhett Sutphin
 */
@Entity
@Table (name = "planned_events")
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_planned_events_id")
    }
)
public class PlannedEvent extends AbstractDomainObject implements Comparable<PlannedEvent> {
    private Activity activity;
    private Period period;
    private Integer day;

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
            dayInArm += getPeriod().getDuration().inDays();
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
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
        if (!period.getPlannedEvents().contains(this)) {
            period.addPlannedEvent(this);
        }
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }
}
