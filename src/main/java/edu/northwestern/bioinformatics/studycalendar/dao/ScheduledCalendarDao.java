package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import org.hibernate.Hibernate;

import java.util.Collection;

/**
 * @author Rhett Sutphin
 */
public class ScheduledCalendarDao extends StudyCalendarMutableDomainObjectDao<ScheduledCalendar> {
    @Override
    public Class<ScheduledCalendar> domainClass() {
        return ScheduledCalendar.class;
    }

    /**
     * Fully readAndSave all the template ("scheduled") child objects of this calendar.
     * This is only necessary if the object is going to be passed outside of the scope of
     * its creating session.  (Otherwise, hibernate dynamic loading works fine.)
     * <p>
     * In practice, this only is necessary for objects that are returned from
     * the public API (if then).
     * @param scheduledCalendar
     * @see edu.northwestern.bioinformatics.studycalendar.web.OpenSessionInViewInterceptorFilter
     */
    public void initialize(ScheduledCalendar scheduledCalendar) {
        Hibernate.initialize(scheduledCalendar);
        for (ScheduledStudySegment scheduledStudySegment : scheduledCalendar.getScheduledStudySegments()) {
            Hibernate.initialize(scheduledStudySegment);
            Hibernate.initialize(scheduledStudySegment.getStudySegment());
            Hibernate.initialize(scheduledStudySegment.getStudySegment().getEpoch());
            for (ScheduledActivity event : scheduledStudySegment.getActivities()) {
                Hibernate.initialize(event);
                Hibernate.initialize(event.getPreviousStates());
            }
        }
    }

    /**
     * Find all the scheduled calendars for a planned study
     *
     * @param source the planned study
     * @return a list of all the scheduled calenadrs found that are on the planned study
     */
    public Collection<ScheduledCalendar> getAllFor(Study source) {
        return getHibernateTemplate().find(
            "from ScheduledCalendar cal where cal.assignment.studySite.study = ?", source);
    }
}
