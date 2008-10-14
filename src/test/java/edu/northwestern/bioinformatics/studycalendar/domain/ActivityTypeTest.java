package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

/**
 * @author Rhett Sutphin
 */
public class ActivityTypeTest extends StudyCalendarTestCase {
    private ActivityType t1, t4;

    protected void setUp() throws Exception {
        super.setUp();
        t1 = ActivityType.DISEASE_MEASURE;
        t4 = ActivityType.PROCEDURE;
    }

    public void testNaturalOrderIsById() throws Exception {
        assertNegative(t1.compareTo(t4));
        assertPositive(t4.compareTo(t1));
    }

    public void testGetByName() {
        assertEquals(ActivityType.DISEASE_MEASURE, ActivityType.getByName(ActivityType.DISEASE_MEASURE.getName()));
        assertNull("must return null for non existing activity", ActivityType.getByName("not existing activity"));
    }
}
