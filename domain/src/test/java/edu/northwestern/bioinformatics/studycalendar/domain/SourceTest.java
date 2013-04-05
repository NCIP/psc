/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.domain.tools.Differences;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;

/**
 * @author Rhett Sutphin
 */
public class SourceTest extends DomainTestCase {
    private Source source = createSource("Test-o");

    public void testTransientClone() throws Exception {
        Source actual = source.transientClone();
        assertNotSame("Clone is same obj", source, actual);
        assertEquals("Name not copied", source.getName(), actual.getName());
        assertTrue("Clone not marked transient", actual.isMemoryOnly());
    }

    public void testTransientCloneDoesNotIncludeActivities() throws Exception {
        createActivity("W", "W", source, Fixtures.createActivityType("LAB_TEST"));
        createActivity("X", "X", source, Fixtures.createActivityType("LAB_TEST"));
        assertEquals("Test setup failure", 2, source.getActivities().size());

        Source clone = source.transientClone();
        assertEquals("Should have no activities", 0, clone.getActivities().size());
    }

    public void testDeepEqualsForSameSourceName() throws Exception {
        Source source1 = createSource("Source");
        Source source2 = createSource("Source");
        Differences differences = source1.deepEquals(source2);
        assertTrue("Activiy source is different", differences.getMessages().isEmpty());
    }

    public void testDeepEqualsForDifferentSourceName() throws Exception {
        Source source1 = createSource("Source1");
        Source source2 = createSource("Source2");

        assertDifferences(source1.deepEquals(source2), "name \"Source1\" does not match \"Source2\"");
    }

    public void testIsManualActivityTargetTracksManualFlag() throws Exception {
        source.setManualFlag(false);
        assertFalse(source.isManualActivityTarget());
        source.setManualFlag(true);
        assertTrue(source.isManualActivityTarget());
    }
}
