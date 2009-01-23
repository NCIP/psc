package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.test.Fixtures;

/**
 * @author Rhett Sutphin
 */
public class ActivityTypeTest extends StudyCalendarTestCase {
    private ActivityType t1, t4;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        t1 = Fixtures.createActivityType("DISEASE_MEASURE");
        t4 = Fixtures.createActivityType("PROCEDURE");
    }

    public void testNaturalOrderIsByName() throws Exception {
        assertNegative(t1.compareTo(t4));
        assertPositive(t4.compareTo(t1));
    }

    public void testSelectorIsLowerCaseNoSpaceVersionOfName() throws Exception {
        assertEquals("activity-type-disease_measure", Fixtures.createActivityType("Disease Measure").getSelector());
    }
}
