package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Date;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author Rhett Sutphin
 */
@Entity
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_scheduled_arms_id")
    }
)
public class ScheduledArm extends AbstractDomainObject {
    private ScheduledCalendar scheduledCalendar;
    private List<ScheduledEvent> events = new LinkedList<ScheduledEvent>();

    private Arm arm;

    ////// BUSINESS METHODS

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
            name.append(" (").append(armRepeats.indexOf(selfIndex) + 1).append(")");
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

    ///// OBJECT METHODS

    public String toString() {
        return new StringBuilder()
            .append(getClass().getSimpleName()).append('[')
            .append("name=").append(getName())
            .append("; events=").append(getEvents())
            .append(']').toString();
    }
}
