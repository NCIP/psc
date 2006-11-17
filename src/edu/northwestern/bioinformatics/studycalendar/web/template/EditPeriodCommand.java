package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedEvent;
import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.utils.DayRange;
import edu.northwestern.bioinformatics.studycalendar.utils.DefaultDayRange;

import java.util.Iterator;

/**
 * @author Rhett Sutphin
 */
public class EditPeriodCommand implements PeriodCommand {
    private PeriodDao periodDao;
    private Period period;

    public EditPeriodCommand(Period period, PeriodDao periodDao) {
        this.period = period;
        this.periodDao = periodDao;
    }

    public Period getPeriod() {
        return period;
    }

    public Arm getArm() {
        return getPeriod().getArm();
    }

    public void apply() {
        // look for PlannedEvents that are now invalid
        DayRange peDayRange = new DefaultDayRange(1, getPeriod().getDuration().getDays());
        for (Iterator<PlannedEvent> it = getPeriod().getPlannedEvents().iterator(); it.hasNext();) {
            PlannedEvent event = it.next();
            if (!peDayRange.containsDay(event.getDay())) {
                it.remove();
            }
        }
        periodDao.save(getPeriod());
    }
}
