package edu.northwestern.bioinformatics.studycalendar.dao;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;

/**
 * @author Rhett Sutphin
 */
public class ScheduledCalendarDao extends HibernateDaoSupport {
    public ScheduledCalendar getById(int id) {
        return (ScheduledCalendar) getHibernateTemplate().get(ScheduledCalendar.class, id);
    }
}
