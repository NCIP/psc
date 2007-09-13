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
import java.util.List;
import java.util.ArrayList;

import edu.northwestern.bioinformatics.studycalendar.utils.DefaultDayRange;
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
public class Period extends PlanTreeOrderedInnerNode<Arm, PlannedEvent>
    implements Named, Comparable<Period>
{
    private static final int DEFAULT_REPETITIONS = 1;
    private static final int DEFAULT_START_DAY = 1;
    private static final int DEFAULT_DURATION_QUANTITY = 1;
    private static final Duration.Unit DEFAULT_DURATION_UNIT = Duration.Unit.day;

    private String name;
    private Integer startDay;
    private Duration duration;
    private int repetitions;

    public Period() {
        startDay = DEFAULT_START_DAY;
        repetitions = DEFAULT_REPETITIONS;
        getDuration().setQuantity(DEFAULT_DURATION_QUANTITY);
        getDuration().setUnit(DEFAULT_DURATION_UNIT);
    }

    ////// LOGIC

    @Override public Class<Arm> parentClass() { return Arm.class; }
    @Override public Class<PlannedEvent> childClass() { return PlannedEvent.class; }

    public void addPlannedEvent(PlannedEvent event) {
        addChild(event);
    }

    @Transient
    public String getDisplayName() {
        return getName() == null ? "[period]" : getName();
    }

    @Transient
    public List<DayRange> getDayRanges() {
        List<DayRange> ranges = new ArrayList<DayRange>(getRepetitions());
        while (ranges.size() < getRepetitions()) {
            int rep = ranges.size();
            Integer dayCount = getDuration().getDays();
            int repStartDay = getStartDay() + rep * dayCount;
            ranges.add(new DefaultDayRange(repStartDay, repStartDay + dayCount - 1));
        }
        return ranges;
    }

    @Transient
    public DayRange getTotalDayRange() {
        int dayCount = getDuration().getDays() * getRepetitions();
        return new DefaultDayRange(getStartDay(), getStartDay() + dayCount - 1);
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
        return getParent();
    }

    public void setArm(Arm arm) {
        setParent(arm);
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
    @Cascade(value = { CascadeType.DELETE, CascadeType.LOCK, CascadeType.MERGE,
            CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.REMOVE, CascadeType.REPLICATE,
            CascadeType.SAVE_UPDATE, CascadeType.DELETE_ORPHAN })
    public List<PlannedEvent> getPlannedEvents() {
        return getChildren();
    }

    public void setPlannedEvents(List<PlannedEvent> plannedEvents) {
        setChildren(plannedEvents);
    }
}
