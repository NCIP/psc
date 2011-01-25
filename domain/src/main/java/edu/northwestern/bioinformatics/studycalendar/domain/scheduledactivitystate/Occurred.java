package edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.util.Date;

/**
 * @author Rhett Sutphin
 */
@Entity
@DiscriminatorValue(value = "2")
public class Occurred extends ScheduledActivityState {
    public Occurred() { }

    public Occurred(String reason, Date date) {
        super(reason, date);
    }

    protected void appendPreposition(StringBuilder sb) {
        sb.append("on");
    }


    @Transient // use superclass annotation
    public ScheduledActivityMode getMode() { return ScheduledActivityMode.OCCURRED; }
}
