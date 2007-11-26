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
}
