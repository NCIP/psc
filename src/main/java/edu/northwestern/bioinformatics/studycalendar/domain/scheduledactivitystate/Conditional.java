package edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate;


import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
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
@DiscriminatorValue(value = "4")
public class Conditional extends DatedScheduledActivityState {
    public Conditional() { }

    public Conditional(String reason, Date date) {
        super(reason, date);
    }

    protected void appendPreposition(StringBuilder sb) {
        sb.append("for");
    }

    @Transient // use superclass annotation
    public ScheduledActivityMode getMode() { return ScheduledActivityMode.CONDITIONAL; }


    @Transient
    public List<Class<? extends ScheduledActivityState>> getAvailableStates(boolean conditional) {
        List<Class<? extends ScheduledActivityState>> availableStates = getAvailableConditionalStates(conditional);
        availableStates.add(Scheduled.class);
        return availableStates;
    }

}

