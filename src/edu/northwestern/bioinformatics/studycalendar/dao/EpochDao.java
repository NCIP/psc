package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.*;
import org.hibernate.Hibernate;

/**
 * @author Rhett Sutphin
 */
public class EpochDao extends StudyCalendarDao<Epoch> {
    public Class<Epoch> domainClass() { return Epoch.class; }


    public void save(Epoch epoch) {
        getHibernateTemplate().saveOrUpdate(epoch);
    }

    public void initialize(Epoch epoch) {
        Hibernate.initialize(epoch);
        Hibernate.initialize(epoch.getArms());
        for (Arm arm : epoch.getArms()) {
            Hibernate.initialize(arm);
            Hibernate.initialize(arm.getPeriods());
            for (Period period : arm.getPeriods()) {
                Hibernate.initialize(period);
                Hibernate.initialize(period.getPlannedEvents());
                for (PlannedEvent event : period.getPlannedEvents()) {
                    Hibernate.initialize(event);
                    Hibernate.initialize(event.getActivity());
                }
            }
        }
    }
}
