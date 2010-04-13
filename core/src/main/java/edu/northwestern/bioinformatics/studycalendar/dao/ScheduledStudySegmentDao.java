package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;

import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class ScheduledStudySegmentDao extends StudyCalendarMutableDomainObjectDao<ScheduledStudySegment> implements DeletableDomainObjectDao<ScheduledStudySegment> {
    public Class<ScheduledStudySegment> domainClass() {
        return ScheduledStudySegment.class;
    }

    public void delete(ScheduledStudySegment t) {
        getHibernateTemplate().delete(t);
    }

    public void deleteAll(List<ScheduledStudySegment> t) {
        getHibernateTemplate().deleteAll(t);
    }
}
