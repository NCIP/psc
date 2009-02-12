package edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate;

import org.apache.commons.lang.StringUtils;

import javax.persistence.Entity;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Transient;
import java.util.List;
import java.util.Date;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;

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

    @Override
    @Transient
    public List<Class<? extends ScheduledActivityState>> getAvailableStates(boolean conditional) {
        List<Class<? extends ScheduledActivityState>> availableStates = getAvailableConditionalStates(conditional);
        availableStates.add(Scheduled.class);
        return availableStates;
    }

    ////// BEAN PROPERTIES

    @Override
    @Transient // use superclass annotation
    public ScheduledActivityMode getMode() { return ScheduledActivityMode.MISSED; }
}



