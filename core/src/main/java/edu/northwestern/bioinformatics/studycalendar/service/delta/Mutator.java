package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Changeable;

/**
 * @author Rhett Sutphin
 */
public interface Mutator {
    void apply(Changeable source);
    void revert(Changeable target);

    boolean appliesToExistingSchedules();
    void apply(ScheduledCalendar calendar);
}
