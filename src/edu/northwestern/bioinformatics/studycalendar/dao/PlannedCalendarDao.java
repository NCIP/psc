package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * @author Rhett Sutphin
 */
public class PlannedCalendarDao extends HibernateDaoSupport {

    public PlannedCalendar getById(int id) {
        return (PlannedCalendar) getHibernateTemplate().get(PlannedCalendar.class, id);
    }

    public void save(PlannedCalendar calendar) {
        getHibernateTemplate().saveOrUpdate(calendar);
    }

}
