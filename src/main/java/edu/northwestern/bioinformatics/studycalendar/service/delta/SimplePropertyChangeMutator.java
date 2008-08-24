package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import org.springframework.validation.DataBinder;
import org.springframework.validation.BindException;
import org.springframework.beans.MutablePropertyValues;

import java.util.Collections;

/**
 * @author Rhett Sutphin
 */
public class SimplePropertyChangeMutator implements Mutator {
    protected PropertyChange change;

    public SimplePropertyChangeMutator(PropertyChange change) {
        this.change = change;
    }

    public void apply(PlanTreeNode<?> source) {
        bind(source, change.getNewValue());
    }

    public void revert(PlanTreeNode<?> target) {
        bind(target, change.getOldValue());
    }

    protected void bind(Object source, Object targetValue) {
        DataBinder binder = new DataBinder(source);
        binder.bind(new MutablePropertyValues(
            Collections.singletonMap(change.getPropertyName(), targetValue)));
        if (binder.getBindingResult().hasErrors()) {
            throw new StudyCalendarSystemException("Unable to update property value",
                new BindException(binder.getBindingResult()));
        }
    }

    public boolean appliesToExistingSchedules() {
        return false;
    }

    public void apply(ScheduledCalendar calendar) {
        throw new StudyCalendarSystemException("%s cannot be applied to an existing schedule", change);
    }
}
