package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedEvent;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Rhett Sutphin
 */
public class ScheduledEventDao extends StudyCalendarMutableDomainObjectDao<ScheduledEvent> {
    @Override public Class<ScheduledEvent> domainClass() { return ScheduledEvent.class; }

    @SuppressWarnings({ "unchecked" })
    public Collection<ScheduledEvent> getEventsByDate(ScheduledCalendar calendar, Date start, Date end) {
        StringBuilder builder = new StringBuilder("from ScheduledEvent e where e.scheduledArm.scheduledCalendar = ?");
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
    public Collection<ScheduledEvent> getScheduledEventsFromPlannedEvent(
        PlannedEvent source, ScheduledCalendar calendar
    ) {
        return getHibernateTemplate().find(
            "from ScheduledEvent e where e.plannedEvent = ? and e.scheduledArm.scheduledCalendar = ?",
            new Object[] { source, calendar });
    }
}
