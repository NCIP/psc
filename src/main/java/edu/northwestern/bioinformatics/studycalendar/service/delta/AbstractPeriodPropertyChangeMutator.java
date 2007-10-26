package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rhett Sutphin
 */
abstract class AbstractPeriodPropertyChangeMutator extends SimplePropertyChangeMutator {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected TemplateService templateService;

    public AbstractPeriodPropertyChangeMutator(PropertyChange change, TemplateService templateService) {
        super(change);
        this.templateService = templateService;
    }

    @Override
    public boolean appliesToExistingSchedules() { return true; }

    @Override
    public abstract void apply(ScheduledCalendar calendar);

    protected Period getChangedPeriod() {
        // second cast is for javac bug
        return (Period) (PlanTreeNode) change.getDelta().getNode();
    }

    protected Collection<ScheduledArm> getScheduledArmsToMutate(ScheduledCalendar calendar) {
        return calendar.getScheduledArmsFor(
            templateService.findParent(getChangedPeriod()));
    }
}
