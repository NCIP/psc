package edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.apache.commons.lang.StringUtils;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;

/**
 * @author Rhett Sutphin
 */
@Entity
@DiscriminatorValue(value = "3")
public class Canceled extends ScheduledActivityState {

    public Canceled() { }

    public Canceled(String reason,Date date) { super(reason, date); }

    protected void appendPreposition(StringBuilder sb) {
        sb.append("for");
    }

    @Transient
    public List<Class<? extends ScheduledActivityState>> getAvailableStates(boolean conditional) {
        List<Class<? extends ScheduledActivityState>> availableStates = new ArrayList();
        availableStates.add(Scheduled.class);
        availableStates.add(Canceled.class);
        return availableStates;
    }

    ////// BEAN PROPERTIES

    @Transient // use superclass annotation
    public ScheduledActivityMode getMode() { return ScheduledActivityMode.CANCELED; }
}
