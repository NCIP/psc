package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

/**
 * @author Rhett Sutphin
 */
public class ActivityTypeTest extends StudyCalendarTestCase {
    private ActivityType t1, t4;

    protected void setUp() throws Exception {
        super.setUp();
        t1 = Fixtures.getActivityType(1);
        t4 = Fixtures.getActivityType(4);
    }

    public void testNaturalOrderIsById() throws Exception {
        assertNegative(t1.compareTo(t4));
        assertPositive(t4.compareTo(t1));
    }

    public void testNaturalOrderWhenEqual() throws Exception {
        ActivityType t = new ActivityType();
        t.setId(1);
        assertEquals(0, t.compareTo(t1));
        assertEquals(0, t1.compareTo(t));
    }
}
