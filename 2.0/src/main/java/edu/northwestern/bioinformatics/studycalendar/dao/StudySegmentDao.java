package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;

/**
 * @author Moses Hohman
 * @author Rhett Sutphin
 */
public class StudySegmentDao extends StudyCalendarMutableDomainObjectDao<StudySegment> {
    @Override
    public Class<StudySegment> domainClass() {
        return StudySegment.class;
    }

    /**
     * Deletes the given study segment
     *
     * @param  segment the study segment to delete
     */
    public void delete(StudySegment segment) {
        getHibernateTemplate().delete(segment);
    }
}
