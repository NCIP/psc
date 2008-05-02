package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;

/**
 * @author Rhett Sutphin
 */
public class StudySubjectAssignmentDao extends StudyCalendarMutableDomainObjectDao<StudySubjectAssignment> {
    @Override
    public Class<StudySubjectAssignment> domainClass() {
        return StudySubjectAssignment.class;
    }
}
