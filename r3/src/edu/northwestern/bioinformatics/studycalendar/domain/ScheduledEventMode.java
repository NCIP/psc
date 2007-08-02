package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.ScheduledEventState;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Scheduled;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Occurred;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Canceled;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;

import java.util.Collection;

/**
 * Typedef enum representing the discriminator column for subclasses of
 * {@link edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.ScheduledEventState}.
 *
 * This class is an implementation detail -- it is not part of the public API for Study Calendar.
 *
 * @author Rhett Sutphin
 */
public class ScheduledEventMode<T extends ScheduledEventState> extends AbstractControlledVocabularyObject {
    public static final ScheduledEventMode<Scheduled> SCHEDULED
        = new ScheduledEventMode<Scheduled>(1, "scheduled", Scheduled.class);
    public static final ScheduledEventMode<Occurred> OCCURRED
        = new ScheduledEventMode<Occurred>(2, "occurred", Occurred.class);
    public static final ScheduledEventMode<Canceled> CANCELED
        = new ScheduledEventMode<Canceled>(3, "canceled", Canceled.class);

    private Class<T> clazz;

    private ScheduledEventMode(int id, String name, Class<T> clazz) {
        super(id, name);
        this.clazz = clazz;
    }

    public static ScheduledEventMode getById(int id) {
        return getById(ScheduledEventMode.class, id);
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

    public static Collection<ScheduledEventMode> values() {
        return values(ScheduledEventMode.class);
    }
}
