package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.Arm;

/**
 * @author Moses Hohman
 * @author Rhett Sutphin
 */
public class ArmDao extends StudyCalendarGridIdentifiableDao<Arm> {
    public Class<Arm> domainClass() {
        return Arm.class;
    }

    public void save(Arm arm) {
        getHibernateTemplate().saveOrUpdate(arm);
    }
}
