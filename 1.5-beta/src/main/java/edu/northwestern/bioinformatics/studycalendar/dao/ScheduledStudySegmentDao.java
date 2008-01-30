package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;

/**
 * @author Rhett Sutphin
 */
public class ScheduledStudySegmentDao extends StudyCalendarDao<ScheduledStudySegment> {
    public Class<ScheduledStudySegment> domainClass() {
        return ScheduledStudySegment.class;
    }
}
