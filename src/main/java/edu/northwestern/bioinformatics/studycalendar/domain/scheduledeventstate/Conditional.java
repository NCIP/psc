package edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate;


import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEventMode;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.util.Date;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
@Entity
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_scheduled_event_states_id")
    }
)
@DiscriminatorValue(value = "4")
public class Conditional extends DatedScheduledEventState {
    public Conditional() { }

    public Conditional(String reason, Date date) {
        super(reason, date);
        setConditional(true);
    }

    protected void appendPreposition(StringBuilder sb) {
        sb.append("for");
    }

    @Transient // use superclass annotation
    public ScheduledEventMode getMode() { return ScheduledEventMode.CONDITIONAL; }


    @Transient
    public List<Class<? extends ScheduledEventState>> getAvailableStates() {
        List<Class<? extends ScheduledEventState>> availableStates = getAvailableConditionalStates();
        availableStates.add(Scheduled.class);
        return availableStates;
    }

}

