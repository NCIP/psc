package edu.northwestern.bioinformatics.studycalendar.domain;

/**
 * @author Rhett Sutphin
 */
public class ScheduledEventState extends AbstractControlledVocabularyObject {
    public static final ScheduledEventState SCHEDULED = new ScheduledEventState(1, "scheduled");
    public static final ScheduledEventState OCCURRED = new ScheduledEventState(2, "occurred");
    public static final ScheduledEventState CANCELED = new ScheduledEventState(3, "canceled");

    private ScheduledEventState(int id, String name) { super(id, name); }

    public static ScheduledEventState getById(int id) {
        return getById(ScheduledEventState.class, id);
    }
}
