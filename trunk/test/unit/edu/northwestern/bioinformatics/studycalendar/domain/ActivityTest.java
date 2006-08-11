package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

/**
 * @author Rhett Sutphin
 */
public class ActivityTest extends StudyCalendarTestCase {
    private Activity a0, a1;

    protected void setUp() throws Exception {
        super.setUp();
        a0 = new Activity();
        a0.setType(Fixtures.getActivityType(4));
        a0.setName("Activity 0");
        a1 = new Activity();
        a1.setType(Fixtures.getActivityType(8));
        a1.setName("Activity 1");
    }

    public void testNaturalOrderByTypeFirst() throws Exception {
        a0.setName("Z");
        a1.setName("A");

        assertNegative(a0.compareTo(a1));
        assertPositive(a1.compareTo(a0));
    }

    public void testNaturalOrderConsidersName() throws Exception {
        a1.setType(a0.getType());

        assertNegative(a0.compareTo(a1));
        assertPositive(a1.compareTo(a0));
    }
}
