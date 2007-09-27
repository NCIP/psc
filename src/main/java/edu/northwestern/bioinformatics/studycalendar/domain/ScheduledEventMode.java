package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.ScheduledEventState;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Scheduled;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Occurred;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Canceled;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Conditional;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.NotAvailable;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

/**
 * Typedef enum representing the discriminator column for subclasses of
 * {@link edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.ScheduledEventState}.
 * <p>
 * This class is an implementation detail -- it is not needed in the public API for the PSC.
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
    public static final ScheduledEventMode<Conditional> CONDITIONAL
        = new ScheduledEventMode<Conditional>(4, "conditional", Conditional.class);
    public static final ScheduledEventMode<NotAvailable> NOT_AVAILABLE
        = new ScheduledEventMode<NotAvailable>(5, "NA", NotAvailable.class);


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

    public static List<ScheduledEventMode> getAvailableModes(ScheduledEventState state, boolean conditional) {
        List<ScheduledEventMode> modes = new ArrayList<ScheduledEventMode>();
        List<Class<? extends ScheduledEventState>> availableStates = state.getAvailableStates(conditional);
        for(ScheduledEventMode mode: values()) {
            if (availableStates.contains(mode.clazz)) modes.add(mode);
        }
        return modes;
    }

    public Class<T> getClazz() {
        return clazz;
    }
}
