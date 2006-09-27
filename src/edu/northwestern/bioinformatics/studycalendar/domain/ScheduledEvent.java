package edu.northwestern.bioinformatics.studycalendar.domain;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import javax.persistence.Column;
import java.util.Date;

/**
 * @author Rhett Sutphin
 */
@Entity
@Table (name = "scheduled_events")
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_scheduled_events_id")
    }
)
public class ScheduledEvent extends AbstractDomainObject {
    private PlannedEvent plannedEvent;
    private Date idealDate;
    private Date actualDate;
    private ScheduledEventState state;
    private String notes;

    ////// BEAN PROPERTIES

    @ManyToOne
    @JoinColumn (name = "planned_event_id")
    public PlannedEvent getPlannedEvent() {
        return plannedEvent;
    }

    public void setPlannedEvent(PlannedEvent plannedEvent) {
        this.plannedEvent = plannedEvent;
    }

    public Date getIdealDate() {
        return idealDate;
    }

    public void setIdealDate(Date idealDate) {
        this.idealDate = idealDate;
    }

    public Date getActualDate() {
        return actualDate;
    }

    public void setActualDate(Date actualDate) {
        this.actualDate = actualDate;
    }

    @Type(type="scheduledEventState")
    @Column(name="scheduled_event_state_id")
    public ScheduledEventState getState() {
        return state;
    }

    public void setState(ScheduledEventState state) {
        this.state = state;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
