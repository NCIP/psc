package edu.northwestern.bioinformatics.studycalendar.domain;

/**
 * Typedef enum representing the discriminator column for subclasses of
 * {@link edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.ScheduledEventState}.
 *
 * This class is an implementation detail -- it is not part of the public API for Study Calendar.
 *
 * @author Rhett Sutphin
 */
public class ScheduledEventMode extends AbstractControlledVocabularyObject {
    public static final ScheduledEventMode SCHEDULED = new ScheduledEventMode(1, "scheduled");
    public static final ScheduledEventMode OCCURRED = new ScheduledEventMode(2, "occurred");
    public static final ScheduledEventMode CANCELED = new ScheduledEventMode(3, "canceled");

    private ScheduledEventMode(int id, String name) { super(id, name); }

    public static ScheduledEventMode getById(int id) {
        return getById(ScheduledEventMode.class, id);
    }
}
