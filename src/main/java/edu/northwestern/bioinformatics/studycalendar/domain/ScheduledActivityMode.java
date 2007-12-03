package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.*;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

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

    public T createStateInstance() {
        try {
            return clazz.newInstance();
        } catch (InstantiationException e) {
            throw new StudyCalendarSystemException(e);
        } catch (IllegalAccessException e) {
            throw new StudyCalendarSystemException(e);
        }
    }

    public static Collection<ScheduledActivityMode> values() {
        return values(ScheduledActivityMode.class);
    }

    public static List<ScheduledActivityMode> getAvailableModes(ScheduledActivityState state, boolean conditional) {
        List<ScheduledActivityMode> modes = new ArrayList<ScheduledActivityMode>();
        List<Class<? extends ScheduledActivityState>> availableStates = state.getAvailableStates(conditional);
        for(ScheduledActivityMode mode: values()) {
            if (availableStates.contains(mode.clazz)) modes.add(mode);
        }
        return modes;
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
}
