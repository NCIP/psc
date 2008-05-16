package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.utils.DayRange;
import edu.northwestern.bioinformatics.studycalendar.utils.DefaultDayRange;
import edu.nwu.bioinformatics.commons.ComparisonUtils;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Moses Hohman
 * @author Rhett Sutphin
 */
@Entity
@Table(name = "periods")
@GenericGenerator(name = "id-generator", strategy = "native",
        parameters = {
        @Parameter(name = "sequence", value = "seq_periods_id")
                }
)
public class Period extends PlanTreeOrderedInnerNode<StudySegment, PlannedActivity>
        implements Named, Comparable<Period> {
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

    @Override
    public Class<StudySegment> parentClass() {
        return StudySegment.class;
    }

    @Override
    public Class<PlannedActivity> childClass() {
        return PlannedActivity.class;
    }

    public void addPlannedActivity(PlannedActivity event) {
        addChild(event);
    }

    @Transient
    public String getDisplayName() {
        return getName() == null ? "[period]" : getName();
    }

    @Transient
    public String getDisplayNameWithActivities() {
        return getDisplayName()+ " ("+getChildren().size()+" activities)";
    }

    @Transient
    public List<DayRange> getDayRanges() {
        List<DayRange> ranges = new ArrayList<DayRange>(getRepetitions());
        while (ranges.size() < getRepetitions()) {
            int rep = ranges.size();
            Integer dayCount = getDuration().getDays();
            int repStartDay = getStartDay() + rep * dayCount;
            int durationUnit = getDuration().getUnit().inDays();
            ranges.add(new DefaultDayRange(repStartDay, repStartDay + dayCount - durationUnit));
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
    @JoinColumn(name = "study_segment_id")
    public StudySegment getStudySegment() {
        return getParent();
    }

    public void setStudySegment(StudySegment studySegment) {
        setParent(studySegment);
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
    @AttributeOverride(name = "quantity", column = @Column(name = "duration_quantity")),
    @AttributeOverride(name = "unit", column = @Column(name = "duration_unit"))
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
    @OrderBy
    // order by ID for testing consistency
    // TODO: why isn't this just "ALL"?
    @Cascade(value = {CascadeType.DELETE, CascadeType.LOCK, CascadeType.MERGE,
            CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.REMOVE, CascadeType.REPLICATE,
            CascadeType.SAVE_UPDATE})
    public List<PlannedActivity> getPlannedActivities() {
        return getChildren();
    }

    public void setPlannedActivities(List<PlannedActivity> plannedActivities) {
        setChildren(plannedActivities);
    }

    ////// OBJECT METHODS

    @Override
    public Period clone() {
        Period clone = (Period) super.clone();
        clone.setDuration(getDuration().clone());
        return clone;
    }
}
