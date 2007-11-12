package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Rhett Sutphin
 */
public class ScheduledActivityDao extends StudyCalendarMutableDomainObjectDao<ScheduledActivity> {
    @Override public Class<ScheduledActivity> domainClass() { return ScheduledActivity.class; }

    @SuppressWarnings({ "unchecked" })
    public Collection<ScheduledActivity> getEventsByDate(ScheduledCalendar calendar, Date start, Date end) {
        StringBuilder builder = new StringBuilder("from ScheduledActivity e where e.scheduledArm.scheduledCalendar = ?");
        List<Object> params = new ArrayList<Object>(3);
        params.add(calendar);
        if (start != null) {
            builder.append(" and e.currentState.date >= ?");
            params.add(start);
        }
        if (end != null) {
            builder.append(" and e.currentState.date <= ?");
            params.add(end);
        }
        return getHibernateTemplate().find(builder.toString(), params.toArray());
    }

    @SuppressWarnings({ "unchecked" })
    public Collection<ScheduledActivity> getEventsFromPlannedActivity(
        PlannedActivity source, ScheduledCalendar calendar
    ) {
        return getHibernateTemplate().find(
            "from ScheduledActivity e where e.plannedActivity = ? and e.scheduledArm.scheduledCalendar = ?",
            new Object[] { source, calendar });
    }
}
