package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;

/**
 * @author Rhett Sutphin
 */
public interface Mutator {
    void apply(PlanTreeNode<?> source);
    void revert(PlanTreeNode<?> target);

    void apply(ScheduledCalendar calendar);
}
