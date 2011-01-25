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
@DiscriminatorValue(value = "5")
public class NotApplicable extends ScheduledActivityState {

    public NotApplicable() { }

    public NotApplicable(String reason,Date date) {
        super(reason,date);
    }

    ////// LOGIC

    protected void appendPreposition(StringBuilder sb) {
        sb.append("on");
    }

    ////// BEAN PROPERTIES

    @Override
    @Transient // use superclass annotation
    public ScheduledActivityMode getMode() { return ScheduledActivityMode.NOT_APPLICABLE; }
}