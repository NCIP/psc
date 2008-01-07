package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import org.hibernate.Hibernate;

/**
 * @author Rhett Sutphin
 */
public class EpochDao extends StudyCalendarMutableDomainObjectDao<Epoch> {
    @Override public Class<Epoch> domainClass() { return Epoch.class; }

    public void initialize(Epoch epoch) {
        Hibernate.initialize(epoch);
        Hibernate.initialize(epoch.getStudySegments());
        for (StudySegment studySegment : epoch.getStudySegments()) {
            Hibernate.initialize(studySegment);
            Hibernate.initialize(studySegment.getPeriods());
            for (Period period : studySegment.getPeriods()) {
                Hibernate.initialize(period);
                Hibernate.initialize(period.getPlannedActivities());
                for (PlannedActivity event : period.getPlannedActivities()) {
                    Hibernate.initialize(event);
                    Hibernate.initialize(event.getActivity());
                }
            }
        }
    }

    public void delete(Epoch epoch) {
        getHibernateTemplate().delete(epoch);
    }
}
