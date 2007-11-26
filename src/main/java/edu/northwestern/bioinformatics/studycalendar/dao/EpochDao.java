package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.*;
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
}
