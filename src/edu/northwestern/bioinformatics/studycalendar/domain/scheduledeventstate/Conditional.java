package edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate;


import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEventMode;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.apache.commons.lang.StringUtils;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.util.Date;

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
    }

    protected void appendPreposition(StringBuilder sb) {
        sb.append("for");
    }

    @Transient // use superclass annotation
    public ScheduledEventMode getMode() { return ScheduledEventMode.CONDITIONAL; }
}

