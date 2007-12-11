package edu.northwestern.bioinformatics.studycalendar.dao;

import org.hibernate.Hibernate;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;

import java.util.Collection;

/**
 * @author Rhett Sutphin
 */
public class ScheduledCalendarDao extends StudyCalendarMutableDomainObjectDao<ScheduledCalendar> {
    @Override
    public Class<ScheduledCalendar> domainClass() {
        return ScheduledCalendar.class;
    }

    public void initialize(ScheduledCalendar scheduledCalendar) {
        Hibernate.initialize(scheduledCalendar);
        for (ScheduledStudySegment scheduledStudySegment : scheduledCalendar.getScheduledStudySegments()) {
            Hibernate.initialize(scheduledStudySegment);
            Hibernate.initialize(scheduledStudySegment.getStudySegment());
            Hibernate.initialize(scheduledStudySegment.getStudySegment().getEpoch());
            for (ScheduledActivity event : scheduledStudySegment.getEvents()) {
                Hibernate.initialize(event);
                Hibernate.initialize(event.getPreviousStates());
            }
        }
    }

    public Collection<ScheduledCalendar> getAllFor(Study source) {
        return getHibernateTemplate().find(
            "from ScheduledCalendar cal where cal.assignment.studySite.study = ?", source);
    }
}
