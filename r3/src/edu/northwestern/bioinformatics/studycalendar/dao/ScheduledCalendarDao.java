package edu.northwestern.bioinformatics.studycalendar.dao;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;

/**
 * @author Rhett Sutphin
 */
public class ScheduledCalendarDao extends StudyCalendarDao<ScheduledCalendar> {
    public Class<ScheduledCalendar> domainClass() {
        return ScheduledCalendar.class;
    }

    public void save(ScheduledCalendar calendar) {
        getHibernateTemplate().saveOrUpdate(calendar);
    }
}
