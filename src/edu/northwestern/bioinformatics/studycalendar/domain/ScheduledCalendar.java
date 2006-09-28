package edu.northwestern.bioinformatics.studycalendar.domain;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.CollectionId;
import org.hibernate.annotations.OrderBy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.ManyToMany;
import javax.persistence.JoinTable;
import java.util.List;
import java.util.LinkedList;

/**
 * @author Rhett Sutphin
 */
@Entity
@Table
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_scheduled_calendars_id")
    }
)
public class ScheduledCalendar extends AbstractDomainObject {
    private StudyParticipantAssignment assignment;
    private List<Arm> arms = new LinkedList<Arm>();
    private List<ScheduledEvent> events = new LinkedList<ScheduledEvent>();

    ////// BUSINESS METHODS

    public void addEvent(ScheduledEvent event) {
        getEvents().add(event);
        event.setScheduledCalendar(this);
    }

    public void addArm(Arm arm) {
        arms.add(arm);
    }

    ////// BEAN PROPERTIES

    @ManyToOne
    @JoinColumn(name = "assignment_id")
    public StudyParticipantAssignment getAssignment() {
        return assignment;
    }

    public void setAssignment(StudyParticipantAssignment assignment) {
        this.assignment = assignment;
    }

    @ManyToMany
    @JoinTable(
        name = "scheduled_arms",
        joinColumns = {@JoinColumn(name = "scheduled_calendar_id")},
        inverseJoinColumns = {@JoinColumn(name="arm_id")}
    )
    @IndexColumn(name="list_index")
    public List<Arm> getArms() {
        return arms;
    }

    public void setArms(List<Arm> arms) {
        this.arms = arms;
    }

    @OneToMany(mappedBy = "scheduledCalendar")
    @OrderBy(clause="ideal_date")
    @Cascade(value = {CascadeType.ALL, CascadeType.DELETE_ORPHAN})
    public List<ScheduledEvent> getEvents() {
        return events;
    }

    public void setEvents(List<ScheduledEvent> events) {
        this.events = events;
    }
}
