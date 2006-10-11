package edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.domain.AbstractDomainObject;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEventMode;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

/**
 * @author Rhett Sutphin
 */
@Entity // This isn't really an entity, but the @OneToMany from ScheduledEvent doesn't work otherwise
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_scheduled_event_states_id")
    }
)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "scheduled_event_states")
@DiscriminatorColumn(name = "mode_id", discriminatorType = DiscriminatorType.INTEGER)
public abstract class ScheduledEventState extends AbstractDomainObject implements Cloneable, Serializable {
    private String reason;

    protected ScheduledEventState() { }

    protected ScheduledEventState(String reason) {
        this.reason = reason;
    }

    @Type(type = "scheduledEventMode")
    @Column(name = "mode_id", insertable = false, updatable = false)
    public abstract ScheduledEventMode getMode();
    protected void setMode(ScheduledEventMode mode) { /* for hibernate; value ignored */ }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    ////// OBJECT METHODS

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new StudyCalendarError("It is cloneable", e);
        }
    }
}
