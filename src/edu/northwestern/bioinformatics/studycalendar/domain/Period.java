package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.nwu.bioinformatics.commons.ComparisonUtils;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;

import edu.northwestern.bioinformatics.studycalendar.utils.DayRange;

/**
 * @author Moses Hohman
 * @author Rhett Sutphin
 */
@Entity
@Table (name = "periods")
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_periods_id")
    }
)
public class Period extends AbstractDomainObject implements Named, Comparable<Period> {
    public static final int DEFAULT_REPETITIONS = 1;

    private String name;
    private Arm arm;
    private Integer startDay;
    private Duration duration;
    private int repetitions = DEFAULT_REPETITIONS;
    private List<PlannedEvent> plannedEvents;

    public Period() {
        plannedEvents = new LinkedList<PlannedEvent>();
    }

    ////// LOGIC

    public void addPlannedEvent(PlannedEvent event) {
        getPlannedEvents().add(event);
        event.setPeriod(this);
    }

    @Transient
    public List<DayRange> getDayRanges() {
        List<DayRange> ranges = new ArrayList<DayRange>(getRepetitions());
        while (ranges.size() < getRepetitions()) {
            int rep = ranges.size();
            Integer dayCount = getDuration().getDays();
            int repStartDay = getStartDay() + rep * dayCount;
            ranges.add(new DayRange(repStartDay, repStartDay + dayCount - 1));
        }
        return ranges;
    }

    @Transient
    public DayRange getTotalDayRange() {
        int dayCount = getDuration().getDays() * getRepetitions();
        return new DayRange(getStartDay(), getStartDay() + dayCount - 1);
    }

    @Transient
    public Duration getTotalDuration() {
        return new Duration(getDuration().getQuantity() * getRepetitions(), getDuration().getUnit());
    }

    public boolean isFirstDayOfRepetition(int day) {
        return getTotalDayRange().containsDay(day) && getRepetitionRelativeDay(day) == 1;
    }

    public boolean isLastDayOfRepetition(int day) {
        return getTotalDayRange().containsDay(day) && getRepetitionRelativeDay(day) == getDuration().getDays();
    }

    private int getRepetitionRelativeDay(int day) {
        return (day - getStartDay()) % getDuration().getDays() + 1;
    }

    public int compareTo(Period other) {
        int startCompare = ComparisonUtils.nullSafeCompare(getStartDay(), other.getStartDay());
        if (startCompare != 0) return startCompare;

        int lengthCompare = ComparisonUtils.nullSafeCompare(getTotalDuration(), other.getTotalDuration());
        if (lengthCompare != 0) return lengthCompare;

        int repCompare = ComparisonUtils.nullSafeCompare(getRepetitions(), other.getRepetitions());
        if (repCompare != 0) return -1 * repCompare;

        return ComparisonUtils.nullSafeCompare(getName(), other.getName());
    }

    ////// BEAN PROPERTIES

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "arm_id")
    public Arm getArm() {
        return arm;
    }

    public void setArm(Arm arm) {
        this.arm = arm;
    }

    @Column(name = "start_day")
    public Integer getStartDay() {
        return startDay;
    }

    public void setStartDay(Integer startDay) {
        this.startDay = startDay;
    }

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name="quantity", column = @Column(name = "duration_quantity")),
        @AttributeOverride(name="unit", column = @Column(name = "duration_unit"))
    })
    public Duration getDuration() {
        if (duration == null) duration = new Duration();
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public int getRepetitions() {
        return repetitions;
    }

    public void setRepetitions(int repetitions) {
        this.repetitions = repetitions;
    }

    @OneToMany(mappedBy = "period")
    @OrderBy // order by ID for testing consistency
    @Cascade(value = { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public List<PlannedEvent> getPlannedEvents() {
        return plannedEvents;
    }

    public void setPlannedEvents(List<PlannedEvent> plannedEvents) {
        this.plannedEvents = plannedEvents;
    }
}
