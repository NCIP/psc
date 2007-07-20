package edu.northwestern.bioinformatics.studycalendar.domain;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.Columns;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.validator.NotNull;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.ManyToOne;
import javax.persistence.Column;
import javax.persistence.Transient;
import javax.persistence.OneToMany;
import javax.persistence.JoinColumn;
import java.util.Date;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Collections;

import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.ScheduledEventState;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.DatedScheduledEventState;
import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;

/**
 * @author Rhett Sutphin
 */
@Entity
@Table
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_scheduled_events_id")
    }
)
public class ScheduledEvent extends AbstractMutableDomainObject {
    private ScheduledArm scheduledArm;
    private PlannedEvent plannedEvent;
    private Date idealDate;
    private String notes;
    private ScheduledEventState currentState;
    private List<ScheduledEventState> previousStates = new LinkedList<ScheduledEventState>();

    ////// LOGIC

    public void changeState(ScheduledEventState newState) {
        if (getCurrentState() != null) previousStates.add(getCurrentState());
        setCurrentState(newState);
    }

    @Transient
    public List<ScheduledEventState> getAllStates() {
        List<ScheduledEventState> all = new ArrayList<ScheduledEventState>(getPreviousStates());
        if (getCurrentState() != null) all.add(getCurrentState());
        return all;
    }

    @Transient
    public Date getActualDate() {
        Date actualDate = null;
        List<ScheduledEventState> states = getAllStates();
        Collections.reverse(states);
        for (ScheduledEventState state : states) {
            if (state instanceof DatedScheduledEventState) {
                actualDate = ((DatedScheduledEventState) state).getDate();
                break;
            }
        }
        if (actualDate == null) {
            actualDate = getIdealDate();
        }
        return actualDate;
    }

    ////// BEAN PROPERTIES

    @ManyToOne
    public ScheduledArm getScheduledArm() {
        return scheduledArm;
    }

    public void setScheduledArm(ScheduledArm scheduledArm) {
        this.scheduledArm = scheduledArm;
    }

    @ManyToOne
    public PlannedEvent getPlannedEvent() {
        return plannedEvent;
    }

    public void setPlannedEvent(PlannedEvent plannedEvent) {
        this.plannedEvent = plannedEvent;
    }

    @Type(type = "edu.northwestern.bioinformatics.studycalendar.utils.hibernate.ScheduledEventStateType")
    @Columns(columns = {
        @Column(name = "current_state_mode_id"),
        @Column(name = "current_state_reason"),
        @Column(name = "current_state_date")
    })
    public ScheduledEventState getCurrentState() {
        return currentState;
    }

    private void setCurrentState(ScheduledEventState currentState) {
        this.currentState = currentState;
    }

    @OneToMany(cascade = javax.persistence.CascadeType.ALL)
    @JoinColumn(name = "scheduled_event_id", insertable = true, updatable = true, nullable = false)
    @Cascade({CascadeType.ALL, CascadeType.DELETE_ORPHAN})
    @IndexColumn(name = "list_index")
    @NotNull
    public List<ScheduledEventState> getPreviousStates() {
        return previousStates;
    }

    public void setPreviousStates(List<ScheduledEventState> previousStates) {
        this.previousStates = previousStates;
    }

    public Date getIdealDate() {
        return idealDate;
    }

    public void setIdealDate(Date idealDate) {
        this.idealDate = idealDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
