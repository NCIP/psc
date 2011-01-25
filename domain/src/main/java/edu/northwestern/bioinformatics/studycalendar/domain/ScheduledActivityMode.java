package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Canceled;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Conditional;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Missed;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.NotApplicable;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Occurred;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Scheduled;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.ScheduledActivityState;
import gov.nih.nci.cabig.ctms.lang.DateTools;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.Date;

/**
 * Typedef enum representing the discriminator column for subclasses of
 * {@link edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.ScheduledActivityState}.
 * <p>
 * This class is an implementation detail -- it is not needed in the public API for the PSC.
 *
 * @author Rhett Sutphin
 */
public class ScheduledActivityMode<T extends ScheduledActivityState> extends AbstractControlledVocabularyObject {
    public static final ScheduledActivityMode<Scheduled> SCHEDULED
        = new ScheduledActivityMode<Scheduled>(1, "scheduled", Scheduled.class);
    public static final ScheduledActivityMode<Occurred> OCCURRED
        = new ScheduledActivityMode<Occurred>(2, "occurred", Occurred.class);
    public static final ScheduledActivityMode<Canceled> CANCELED
        = new ScheduledActivityMode<Canceled>(3, "canceled", Canceled.class);
    public static final ScheduledActivityMode<Conditional> CONDITIONAL
        = new ScheduledActivityMode<Conditional>(4, "conditional", Conditional.class);
    public static final ScheduledActivityMode<NotApplicable> NOT_APPLICABLE
        = new ScheduledActivityMode<NotApplicable>(5, "NA", NotApplicable.class);
    public static final ScheduledActivityMode<Missed> MISSED
        = new ScheduledActivityMode<Missed>(6, "missed", Missed.class);


    private Class<T> clazz;

    private ScheduledActivityMode(int id, String name, Class<T> clazz) {
        super(id, name);
        this.clazz = clazz;
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

    public T createStateInstance() {
        try {
            return clazz.newInstance();
        } catch (InstantiationException e) {
            throw new StudyCalendarSystemException(e);
        } catch (IllegalAccessException e) {
            throw new StudyCalendarSystemException(e);
        }
    }

    public T createStateInstance(Date date, String reason) {
        T state = createStateInstance();
        state.setDate(date);
        state.setReason(reason);
        return state;
    }

    public T createStateInstance(int year, int month, int date, String reason) {
        return createStateInstance(DateTools.createDate(year, month, date), reason);
    }

    public static Collection<ScheduledActivityMode> values() {
        return values(ScheduledActivityMode.class);
    }

    public boolean isOutstanding() {
        return getUnscheduleMode() != null;
    }

    public ScheduledActivityMode<?> getUnscheduleMode() {
        if (this == SCHEDULED) return CANCELED;
        else if (this == CONDITIONAL) return NOT_APPLICABLE;
        else return null;
    }

    public Class<T> getClazz() {
        return clazz;
    }

    public String getDisplayName() {
        return StringUtils.capitalize(getName());
    }
}
