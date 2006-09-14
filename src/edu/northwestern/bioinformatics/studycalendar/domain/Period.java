package edu.northwestern.bioinformatics.studycalendar.domain;

import org.apache.commons.lang.math.IntRange;
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
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
public class Period extends AbstractDomainObject implements Named {
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
    public IntRange getDayRange() {
        return new IntRange(getStartDay(), getEndDay());
    }

    @Transient
    public Integer getEndDay() {
        if (startDay == null) return null;
        if (getDuration().getQuantity() == null) return null;
        return startDay + (getDuration().inDays() * repetitions) - 1;
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
    @Cascade(value = { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public List<PlannedEvent> getPlannedEvents() {
        return plannedEvents;
    }

    public void setPlannedEvents(List<PlannedEvent> plannedEvents) {
        this.plannedEvents = plannedEvents;
    }
}
