package edu.northwestern.bioinformatics.studycalendar.domain;

import gov.nih.nci.cabig.ctms.lang.DateTools;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.Date;

/**
 * Typedef enum representing the discriminator column for subclasses of
 * {@link ScheduledActivityState}.
 * <p>
 * This class is an implementation detail -- it is not needed in the public API for the PSC.
 *
 * @author Rhett Sutphin
 */
public class ScheduledActivityMode extends AbstractControlledVocabularyObject {
    public static final ScheduledActivityMode SCHEDULED
        = new ScheduledActivityMode(1, "scheduled", "for");
    public static final ScheduledActivityMode OCCURRED
        = new ScheduledActivityMode(2, "occurred", "on");
    public static final ScheduledActivityMode CANCELED
        = new ScheduledActivityMode(3, "canceled", "for");
    public static final ScheduledActivityMode CONDITIONAL
        = new ScheduledActivityMode(4, "conditional", "for");
    public static final ScheduledActivityMode NOT_APPLICABLE
        = new ScheduledActivityMode(5, "NA", "for");
    public static final ScheduledActivityMode MISSED
        = new ScheduledActivityMode(6, "missed", "on");

    private final String preposition;

    private ScheduledActivityMode(int id, String name, String preposition) {
        super(id, name);
        this.preposition = preposition;
    }

    public static ScheduledActivityMode getById(int id) {
        return getById(ScheduledActivityMode.class, id);
    }

    public static ScheduledActivityMode getByName(String name) {
        for (ScheduledActivityMode mode : values()) {
            if (name.equalsIgnoreCase(mode.getName())) {
                return mode;
            }
        }
        return null;
    }

    public ScheduledActivityState createStateInstance() {
        return new ScheduledActivityState(this);
    }

    public ScheduledActivityState createStateInstance(Date date, String reason) {
        ScheduledActivityState state = createStateInstance();
        state.setDate(date);
        state.setReason(reason);
        return state;
    }

    public ScheduledActivityState createStateInstance(int year, int month, int date, String reason) {
        return createStateInstance(DateTools.createDate(year, month, date), reason);
    }

    public static Collection<ScheduledActivityMode> values() {
        return values(ScheduledActivityMode.class);
    }

    public boolean isOutstanding() {
        return getUnscheduleMode() != null;
    }

    public ScheduledActivityMode getUnscheduleMode() {
        if (this == SCHEDULED) return CANCELED;
        else if (this == CONDITIONAL) return NOT_APPLICABLE;
        else return null;
    }

    public String getDisplayName() {
        return StringUtils.capitalize(getName());
    }

    /**
     * The preposition that goes with the action naming this mode.  E.g., it's "for" for
     * {@link .SCHEDULED} because you say "scheduled <b>for</b> 1/1/2006".
     */
    public String getPreposition() {
        return preposition;
    }
}
