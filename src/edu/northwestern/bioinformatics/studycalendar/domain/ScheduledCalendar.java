package edu.northwestern.bioinformatics.studycalendar.domain;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.CollectionId;
import org.hibernate.annotations.OrderBy;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.CascadeType;
import javax.persistence.ManyToMany;
import javax.persistence.JoinTable;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
@Entity
@Table (name = "scheduled_calendars")
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_scheduled_calendars_id")
    }
)
public class ScheduledCalendar extends AbstractDomainObject {
    private StudyParticipantAssignment assignment;
    private List<Arm> arms;
    private List<ScheduledEvent> events;

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

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "scheduled_calendar_id")
    @OrderBy(clause="ideal_date")
    public List<ScheduledEvent> getEvents() {
        return events;
    }

    public void setEvents(List<ScheduledEvent> events) {
        this.events = events;
    }
}
