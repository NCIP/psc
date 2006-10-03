package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * @author Rhett Sutphin
 */
public class PlannedCalendarDao extends StudyCalendarDao<PlannedCalendar> {
    public Class<PlannedCalendar> domainClass() {
        return PlannedCalendar.class;
    }

    public void save(PlannedCalendar calendar) {
        getHibernateTemplate().saveOrUpdate(calendar);
    }
}
