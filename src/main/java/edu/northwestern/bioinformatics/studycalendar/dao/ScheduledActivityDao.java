package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class ScheduledActivityDao extends StudyCalendarMutableDomainObjectDao<ScheduledActivity> {
    @Override public Class<ScheduledActivity> domainClass() { return ScheduledActivity.class; }

    /**
    * Finds all the scheduled activities for a given calendar by the start date and end date given.
    *
    * @param  calendar the calendar to retrieve the scheduled activities from
    * @param  start the start date of the time span search
    * @param  end the end date of the time span search
    * @return      a list of scheduled activities for the criteria specified
    */
    @SuppressWarnings({ "unchecked" })
    public Collection<ScheduledActivity> getEventsByDate(ScheduledCalendar calendar, Date start, Date end) {
        StringBuilder builder = new StringBuilder("from ScheduledActivity e where e.scheduledStudySegment.scheduledCalendar = ?");
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

    /**
    * Finds all the scheduled activities for a given calendar by the start date and end date given.
    *
    * @param  calendar the calendar to retrieve the scheduled activities from
    * @param  start the start date of the time span search
    * @param  end the end date of the time span search
    * @return      a list of scheduled activities for the criteria specified
    */
    @SuppressWarnings({ "unchecked" })
    public Collection<ScheduledActivity> getEventsByIdealDate(ScheduledCalendar calendar, Date start, Date end) {
        StringBuilder builder = new StringBuilder("from ScheduledActivity e where e.scheduledStudySegment.scheduledCalendar = ?");
        List<Object> params = new ArrayList<Object>(3);
        params.add(calendar);
        if (start != null) {
            builder.append(" and e.idealDate >= ?");
            params.add(start);
        }
        if (end != null) {
            builder.append(" and e.idealDate <= ?");
            params.add(end);
        }
        return getHibernateTemplate().find(builder.toString(), params.toArray());
    }


    /**
    * Finds all the scheduled activities for the given calendar and planned activity
    *
    * @param  source the planned activity to search for in the scheduled activities
    * @param  calendar the calendar to search through for all the scheduled activities
    * @return      a list of all the scheduled activities that meet the criteria specified
    */
    @SuppressWarnings({ "unchecked" })
    public Collection<ScheduledActivity> getEventsFromPlannedActivity(
        PlannedActivity source, ScheduledCalendar calendar
    ) {
        return getHibernateTemplate().find(
            "from ScheduledActivity e where e.plannedActivity = ? and e.scheduledStudySegment.scheduledCalendar = ?",
            new Object[] { source, calendar });
    }
}
