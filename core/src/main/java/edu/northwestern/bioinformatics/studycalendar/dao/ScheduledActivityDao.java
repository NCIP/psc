/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.*;

/**
 * @author Rhett Sutphin
 */
public class ScheduledActivityDao extends StudyCalendarMutableDomainObjectDao<ScheduledActivity> implements DeletableDomainObjectDao<ScheduledActivity> {
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
        Calendar startCal = Calendar.getInstance();
        Calendar endCal = Calendar.getInstance();
        if (start != null) {
            startCal.setTime(start);
            startCal.set(Calendar.HOUR_OF_DAY, 0);
            startCal.set(Calendar.MINUTE, 0);
            startCal.set(Calendar.SECOND, 0);
            start = startCal.getTime();
        }

        if (end != null) {
            endCal.setTime(end);
            endCal.set(Calendar.HOUR_OF_DAY, 23);
            endCal.set(Calendar.MINUTE, 59);
            endCal.set(Calendar.SECOND, 59);
            end = endCal.getTime();
        }

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

    public void delete(ScheduledActivity t) {
        getHibernateTemplate().delete(t);
    }

    public void deleteAll(List<ScheduledActivity> t) {
        getHibernateTemplate().deleteAll(t);
    }
}
