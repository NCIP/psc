package edu.northwestern.bioinformatics.studycalendar.domain;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import junit.framework.TestCase;

/**
 * @author Rhett Sutphin
 */
public class SourceTest extends TestCase {
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
}
