package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.PlannedSchedule;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * @author Rhett Sutphin
 */
public class PlannedScheduleDao extends HibernateDaoSupport {

    public PlannedSchedule getById(int id) {
        return (PlannedSchedule) getHibernateTemplate().get(PlannedSchedule.class, id);
    }

    public void save(PlannedSchedule schedule) {
        getHibernateTemplate().saveOrUpdate(schedule);
    }

}
