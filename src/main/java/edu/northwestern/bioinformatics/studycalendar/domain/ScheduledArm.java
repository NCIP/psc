package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Canceled;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.ScheduledEventState;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OrderBy;
import org.hibernate.annotations.Parameter;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import java.util.*;

import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;

/**
 * @author Rhett Sutphin
 */
@Entity
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_scheduled_arms_id")
    }
)
public class ScheduledArm extends AbstractMutableDomainObject {
    private ScheduledCalendar scheduledCalendar;
    private List<ScheduledEvent> events = new LinkedList<ScheduledEvent>();
    private Integer startDay;
    private Date startDate;

    private Arm arm;

    ////// LOGIC

    public void addEvent(ScheduledEvent event) {
        getEvents().add(event);
        event.setScheduledArm(this);
    }

    @Transient
    public String getName() {
        Epoch epoch = getArm().getEpoch();
        StringBuilder name = new StringBuilder(epoch.getName());
        if (epoch.getArms().size() > 1) {
            name.append(": ").append(getArm().getName());
        }

        int selfIndex = -1;
        List<Integer> armRepeats = new LinkedList<Integer>();
        for (int i = 0; i < getScheduledCalendar().getScheduledArms().size(); i++) {
            ScheduledArm sibling = getScheduledCalendar().getScheduledArms().get(i);
            if (sibling.getArm().equals(this.getArm())) armRepeats.add(i);
            if (sibling == this) selfIndex = i;
        }
        if (selfIndex == -1) throw new StudyCalendarSystemException("This scheduled arm is not a child of its parent");

        if (armRepeats.size() > 1) {
            name.append(" (").append(armRepeats.indexOf(selfIndex) + 1).append(')');
        }

        return name.toString();
    }

    @Transient
    public SortedMap<Date, List<ScheduledEvent>> getEventsByDate() {
        SortedMap<Date, List<ScheduledEvent>> byDate = new TreeMap<Date, List<ScheduledEvent>>();
        for (ScheduledEvent event : getEvents()) {
            Date key = event.getActualDate();
            if (!byDate.containsKey(key)) {
                byDate.put(key, new LinkedList<ScheduledEvent>());
            }
            byDate.get(key).add(event);
        }
        return byDate;
    }

    @Transient
    public Date getNextArmPerProtocolStartDate() {
        Date origin = getStartDate();
        if (origin != null) {
            Calendar defaultState = Calendar.getInstance();
            defaultState.setTime(origin);
            defaultState.add(Calendar.DATE, getArm().getLengthInDays());
            return defaultState.getTime();
        } else {
            return null;
        }
    }

    @Transient
    public boolean isComplete() {
        for (ScheduledEvent event : getEvents()) {
            if (event.getCurrentState().getMode().isOutstanding()) {
                return false;
            }
        }
        return true;
    }

    public void unscheduleOutstandingEvents(String reason) {
        for (ScheduledEvent event : getEvents()) event.unscheduleIfOutstanding(reason);
    }

    ////// BEAN PROPERTIES

    @OneToMany(mappedBy = "scheduledArm")
    @OrderBy(clause="ideal_date")
    @Cascade(value = { CascadeType.ALL, CascadeType.DELETE_ORPHAN})
    public List<ScheduledEvent> getEvents() {
        return events;
    }

    public void setEvents(List<ScheduledEvent> events) {
        this.events = events;
    }

    // This is annotated this way so that the IndexColumn in the parent
    // will work with the bidirectional mapping
    @ManyToOne
    @JoinColumn(insertable=false, updatable=false, nullable=false)
    public ScheduledCalendar getScheduledCalendar() {
        return scheduledCalendar;
    }

    public void setScheduledCalendar(ScheduledCalendar scheduledCalendar) {
        this.scheduledCalendar = scheduledCalendar;
    }

    @ManyToOne
    public Arm getArm() {
        return arm;
    }

    public void setArm(Arm arm) {
        this.arm = arm;
    }

    public Integer getStartDay() {
        return startDay;
    }

    public void setStartDay(Integer startDay) {
        this.startDay = startDay;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    ///// OBJECT METHODS

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName()).append('[')
            .append("name=");
        if (getArm() != null) {
            sb.append(getName());
        }
        return sb.append("; events=").append(getEvents())
            .append(']').toString();
    }
}
