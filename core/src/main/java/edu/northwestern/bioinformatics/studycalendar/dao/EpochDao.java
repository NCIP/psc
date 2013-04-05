/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import org.hibernate.Hibernate;

import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class EpochDao extends ChangeableDao<Epoch> {
    @Override public Class<Epoch> domainClass() { return Epoch.class; }

    /**
     * Fully readAndSave all the template ("planned") child objects of this epoch.
     * This is only necessary if the object is going to be passed outside of the scope of
     * its creating session.  (Otherwise, hibernate dynamic loading works fine.)
     * <p>
     * In practice, this only is necessary for objects that are returned from
     * the public API (if then).
     * @param epoch
     * @see edu.northwestern.bioinformatics.studycalendar.web.OpenSessionInViewInterceptorFilter
     */
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

    /**
    * Deletes an epoch
    *
    * @param  epoch the epoch to delete
    */
    public void delete(Epoch epoch) {
        getHibernateTemplate().delete(epoch);
    }

    public void deleteOrphans() {
        deleteOrphans("Epoch", "plannedCalendar", "PlannedCalendarDelta", "cal");
    }

    public void deleteAll(List<Epoch> t) {
        getHibernateTemplate().deleteAll(t);
    }
}
