package edu.northwestern.bioinformatics.studycalendar.dao;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;

/**
 * @author Moses Hohman
 * @author Rhett Sutphin
 */
public class ArmDao extends WithBigIdDao<Arm> {
    public Class<Arm> domainClass() {
        return Arm.class;
    }

    public void save(Arm arm) {
        getHibernateTemplate().saveOrUpdate(arm);
    }
}
