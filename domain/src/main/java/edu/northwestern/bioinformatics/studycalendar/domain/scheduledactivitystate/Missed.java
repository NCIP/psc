package edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: nshurupova
 * Date: Dec 3, 2007
 * Time: 12:28:43 PM
 * To change this template use File | Settings | File Templates.
 */
@Entity
@DiscriminatorValue(value = "6")
public class Missed extends ScheduledActivityState {

    public Missed() { }

    public Missed(String reason,Date date) {
        super(reason, date);
    }

    ////// LOGIC
    protected void appendPreposition(StringBuilder sb) {
        sb.append("on");
    }

    ////// BEAN PROPERTIES

    @Override
    @Transient // use superclass annotation
    public ScheduledActivityMode getMode() { return ScheduledActivityMode.MISSED; }
}



