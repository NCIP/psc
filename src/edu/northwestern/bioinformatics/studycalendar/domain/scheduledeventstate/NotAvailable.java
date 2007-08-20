package edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.apache.commons.lang.StringUtils;

import javax.persistence.Entity;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Transient;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEventMode;

/**
 * @author Rhett Sutphin
 */
@Entity
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_scheduled_event_states_id")
    }
)
@DiscriminatorValue(value = "5")
public class NotAvailable extends ScheduledEventState {

    public NotAvailable() { }

    public NotAvailable(String reason) { super(reason); }

    ////// LOGIC

    @Transient
    public String getTextSummary() {
        StringBuilder sb = new StringBuilder().append(StringUtils.capitalize(getMode().getName()));
        if (getReason() != null) {
            sb.append(" - ").append(getReason());
        }
        return sb.toString();
    }

    ////// BEAN PROPERTIES

    @Transient // use superclass annotation
    public ScheduledEventMode getMode() { return ScheduledEventMode.NOT_AVAILABLE; }
}
