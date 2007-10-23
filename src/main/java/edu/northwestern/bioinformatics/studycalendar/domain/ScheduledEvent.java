package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.DatedScheduledEventState;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.ScheduledEventState;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Conditional;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
import org.hibernate.annotations.*;
import org.hibernate.annotations.CascadeType;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.*;

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
    private String details;
    private Activity activity;
    private Amendment sourceAmendment;
    private Integer repetitionNumber;

    ////// LOGIC

    public void changeState(ScheduledEventState newState) {
        if (isChangeable()){
            if (getCurrentState() != null) {
                previousStates.add(getCurrentState());
            }
            setCurrentState(newState);
        }
    }

    @Transient
    private boolean isChangeable() {
        Date endDate;
        if (scheduledArm != null
                && scheduledArm.getScheduledCalendar() != null
                && scheduledArm.getScheduledCalendar().getAssignment() != null
                && scheduledArm.getScheduledCalendar().getAssignment().getEndDateEpoch() != null) {
            endDate = scheduledArm.getScheduledCalendar().getAssignment().getEndDateEpoch();
            return getActualDate().before(endDate);
        }
        return true;
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

    @Transient
    public boolean isConditionalState() {
        return ScheduledEventMode.CONDITIONAL == getCurrentState().getMode()? true: false;
    }

    @Transient
    public boolean isValidNewState(Class<? extends ScheduledEventState> newStateClass) {
        return currentState.getAvailableStates(isConditionalEvent()).contains(newStateClass);
    }

    @Transient
    public boolean isConditionalEvent() {
        boolean conditional = (currentState instanceof Conditional);
        if (previousStates != null && previousStates.size() > 0) {
            conditional = (previousStates.get(0) instanceof Conditional);
        }
        return conditional;
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

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id")
    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    @ManyToOne(optional = false)
    public Amendment getSourceAmendment() {
        return sourceAmendment;
    }

    public void setSourceAmendment(Amendment sourceAmendment) {
        this.sourceAmendment = sourceAmendment;
    }

    public Integer getRepetitionNumber() {
        return repetitionNumber;
    }

    public void setRepetitionNumber(Integer repetitionNumber) {
        this.repetitionNumber = repetitionNumber;
    }
}
