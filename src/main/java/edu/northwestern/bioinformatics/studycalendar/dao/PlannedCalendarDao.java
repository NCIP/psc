package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import org.hibernate.Hibernate;

/**
 * @author Rhett Sutphin
 */
public class PlannedCalendarDao extends StudyCalendarMutableDomainObjectDao<PlannedCalendar> {
    @Override
    public Class<PlannedCalendar> domainClass() {
        return PlannedCalendar.class;
    }

    /**
     * Fully readAndSave all the template ("planned") child objects of this calendar.
     * This is only necessary if the object is going to be passed outside of the scope of
     * its creating session.  (Otherwise, hibernate dynamic loading works fine.)
     * <p>
     * In practice, this only is necessary for objects that are returned from
     * the public API (if then).
     * @param calendar
     * @see edu.northwestern.bioinformatics.studycalendar.web.OpenSessionInViewInterceptorFilter
     */
    public void initialize(PlannedCalendar calendar) {
        Hibernate.initialize(calendar);
        Hibernate.initialize(calendar.getEpochs());
        for (Epoch epoch : calendar.getEpochs()) {
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
}
