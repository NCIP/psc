package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

/**
 * @author Rhett Sutphin
 */
public class PopulationTest extends StudyCalendarTestCase {
    public void testNaturalOrderIsByName() throws Exception {
        Population w = Fixtures.createNamedInstance("Women", Population.class);
        Population m = Fixtures.createNamedInstance("men", Population.class);

        assertPositive(w.compareTo(m));
        assertNegative(m.compareTo(w));
    }
}
