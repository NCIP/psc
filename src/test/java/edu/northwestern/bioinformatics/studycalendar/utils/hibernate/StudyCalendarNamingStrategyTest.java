package edu.northwestern.bioinformatics.studycalendar.utils.hibernate;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Participant;
import edu.northwestern.bioinformatics.studycalendar.domain.StudyParticipantAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;

/**
 * @author Rhett Sutphin
 */
public class StudyCalendarNamingStrategyTest extends StudyCalendarTestCase {
    private static final String DC = "DON'T CARE";

    private StudyCalendarNamingStrategy strategy = new StudyCalendarNamingStrategy();

    public void testForeignKeyColumn() throws Exception {
        assertEquals("planned_event_id", strategy.foreignKeyColumnName("plannedEvent", DC, DC, DC));
    }

    public void testTableName() throws Exception {
        assertEquals("participants", strategy.classToTableName(Participant.class.getName()));
        assertEquals("study_participant_assignments", strategy.classToTableName(StudyParticipantAssignment.class.getName()));
        assertEquals("activities", strategy.classToTableName(Activity.class.getName()));
    }
}
