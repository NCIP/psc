package edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEventMode;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.util.Date;
import java.util.List;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

/**
 * @author Rhett Sutphin
 */
@Entity
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_scheduled_event_states_id")
    }
)
@DiscriminatorValue(value = "1")
public class Scheduled extends DatedScheduledEventState {
    public Scheduled() { }

    public Scheduled(String reason, Date date) {
        super(reason, date);
    }

    protected void appendPreposition(StringBuilder sb) {
        sb.append("for");
    }

    @Transient // use superclass annotation
    public ScheduledEventMode getMode() { return ScheduledEventMode.SCHEDULED; }


    @Transient
    public List<Class<? extends ScheduledEventState>> getAvailableStates(boolean conditional) {
        List<Class<? extends ScheduledEventState>> availableStates = getAvailableConditionalStates(conditional);
        availableStates.add(Occurred.class);
        availableStates.add(Scheduled.class);
        availableStates.add(Canceled.class);
        return availableStates;
    }
}
