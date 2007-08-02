package edu.northwestern.bioinformatics.studycalendar.dao;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;

/**
 * @author Moses Hohman
 */
public class ArmDao extends HibernateDaoSupport {
    public Arm getById(int id) {
        return (Arm) getHibernateTemplate().get(Arm.class, id);
    }

    public void save(Arm arm) {
        getHibernateTemplate().saveOrUpdate(arm);
    }
}
