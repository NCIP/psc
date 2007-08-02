package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.nwu.bioinformatics.commons.testing.CoreTestCase;

import edu.northwestern.bioinformatics.studycalendar.domain.StudyParticipantAssignment;

import java.util.Calendar;

/**
 * @author Rhett Sutphin
 */
public class StudyParticipantAssignmentDaoTest extends ContextDaoTestCase<StudyParticipantAssignmentDao> {
    public void testGetById() throws Exception {
        StudyParticipantAssignment assignment = getDao().getById(-10);

        assertEquals("Wrong id", -10, (int) assignment.getId());
        CoreTestCase.assertDayOfDate("Wrong start date", 2003, Calendar.FEBRUARY, 1,
            assignment.getStartDateEpoch());
        assertEquals("Wrong participant", -20, (int) assignment.getParticipant().getId());
        assertEquals("Wrong study site", -15, (int) assignment.getStudySite().getId());
        assertEquals("Wrong study id", "004-12", assignment.getStudyId());
    }
}
