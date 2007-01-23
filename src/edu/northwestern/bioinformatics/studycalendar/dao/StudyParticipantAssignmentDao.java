package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.StudyParticipantAssignment;

/**
 * @author Rhett Sutphin
 */
public class StudyParticipantAssignmentDao extends WithBigIdDao<StudyParticipantAssignment> {
    @Override
    public Class<StudyParticipantAssignment> domainClass() {
        return StudyParticipantAssignment.class;
    }
}
