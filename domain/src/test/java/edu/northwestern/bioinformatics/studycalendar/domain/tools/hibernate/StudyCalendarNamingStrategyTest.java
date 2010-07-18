package edu.northwestern.bioinformatics.studycalendar.domain.tools.hibernate;

import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import junit.framework.TestCase;

/**
 * @author Rhett Sutphin
 */
public class StudyCalendarNamingStrategyTest extends TestCase {
    private static final String DC = "DON'T CARE";

    private StudyCalendarNamingStrategy strategy = new StudyCalendarNamingStrategy();

    public void testForeignKeyColumn() throws Exception {
        assertEquals("planned_activity_id", strategy.foreignKeyColumnName("plannedActivity", DC, DC, DC));
    }

    public void testForeignKeyColumnWithNoPropertyName() throws Exception {
        assertEquals("planned_activity_id", strategy.foreignKeyColumnName(null, "PlannedActivity", "planned_activities", DC));
    }

    public void testTableName() throws Exception {
        assertEquals("subjects", strategy.classToTableName(Subject.class.getName()));
        assertEquals("study_subject_assignments", strategy.classToTableName(StudySubjectAssignment.class.getName()));
        assertEquals("activities", strategy.classToTableName(Activity.class.getName()));
    }
}
