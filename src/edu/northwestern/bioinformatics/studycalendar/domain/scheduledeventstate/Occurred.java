package edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEventMode;

import java.util.Date;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.Transient;

/**
 * @author Rhett Sutphin
 */
@Entity
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_scheduled_event_states_id")
    }
)
@DiscriminatorValue(value = "2")
public class Occurred extends DatedScheduledEventState {
    public Occurred() { }

    public Occurred(String reason, Date date) {
        super(reason, date);
    }

    protected void appendPreposition(StringBuilder sb) {
        sb.append("on");
    }

    @Transient // use superclass annotation
    public ScheduledEventMode getMode() { return ScheduledEventMode.OCCURRED; }
}
