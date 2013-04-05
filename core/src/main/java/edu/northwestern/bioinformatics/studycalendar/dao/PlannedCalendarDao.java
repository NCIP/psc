/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import org.hibernate.Hibernate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Rhett Sutphin
 */
@Transactional(readOnly = true)
public class PlannedCalendarDao extends StudyCalendarMutableDomainObjectDao<PlannedCalendar> implements DeletableDomainObjectDao<PlannedCalendar> {
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

    @Transactional(readOnly = false)
    public void delete(PlannedCalendar plannedCalendar) {
        getHibernateTemplate().delete(plannedCalendar);
    }

    public void deleteAll(List<PlannedCalendar> t) {
        getHibernateTemplate().deleteAll(t);
    }
}
