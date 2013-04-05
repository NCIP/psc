/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.domain.tools.Differences;

import static gov.nih.nci.cabig.ctms.testing.MoreJUnitAssertions.*;

/**
 * @author Rhett Sutphin
 */
public class ActivityTypeTest extends DomainTestCase {
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

    public void testDeepEqualsForSameActivityTypeName() throws Exception {
        ActivityType type1 = Fixtures.createActivityType("DISEASE_MEASURE");
        ActivityType type2 = Fixtures.createActivityType("DISEASE_MEASURE");
        Differences differences = type1.deepEquals(type2);
        assertTrue("Activiy type is different", differences.getMessages().isEmpty());
    }

    public void testDeepEqualsForDifferentActivityTypeName() throws Exception {
        ActivityType type1 = Fixtures.createActivityType("DISEASE_MEASURE");
        ActivityType type2 = Fixtures.createActivityType("PROCEDURE");
        Differences differences = type1.deepEquals(type2);
        assertDifferences(differences, "name \"DISEASE_MEASURE\" does not match \"PROCEDURE\"");
    }
}
