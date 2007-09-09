package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Rhett Sutphin
 */
public class ScheduledEventDao extends StudyCalendarMutableDomainObjectDao<ScheduledEvent> {
    @Override public Class<ScheduledEvent> domainClass() { return ScheduledEvent.class; }

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
}
