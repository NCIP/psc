package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.test.Fixtures;
import static gov.nih.nci.cabig.ctms.testing.MoreJUnitAssertions.*;
import junit.framework.TestCase;

import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class ActivityTest extends TestCase {
    private Activity a0, a1;

    protected void setUp() throws Exception {
        super.setUp();
        a0 = Fixtures.createActivity("Activity 0", Fixtures.createNamedInstance("DISEASE_MEASURE", ActivityType.class));
        a1 = Fixtures.createActivity("Activity 1", Fixtures.createNamedInstance("OTHER", ActivityType.class));
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

    public void testNaturalOrderByNameIsCaseInsensitive() throws Exception {
        a1.setType(a0.getType());

        a0.setName("calcium");
        a1.setName("Ytterbium");

        assertNegative(a0.compareTo(a1));
        assertPositive(a1.compareTo(a0));
    }

    public void testNaturalKeyIsCode() throws Exception {
        a0.setCode("ETC");
        assertEquals("ETC", a0.getNaturalKey());
    }

    public void testPropertyChangeKeyIncludesSourceName() throws Exception {
        a0.setCode("1000000");

        assertEquals("Fixtures Source|1000000", a0.getUniqueKey());
    }

    public void testPropertyChangeKeyEscapesPipeInSourceName() throws Exception {
        Source s = Fixtures.createSource("Pipe|y");
        a0.setSource(s);
        a0.setCode("1");

        assertEquals("Pipe\\|y|1", a0.getUniqueKey());
    }

    public void testPropertyChangeKeyEscapesPipeInCode() throws Exception {
        a0.setCode("110|40");

        assertEquals("Fixtures Source|110\\|40", a0.getUniqueKey());
    }

    public void testDecodePropertyChangeKey() throws Exception {
        Map<String, String> actual = Activity.splitPropertyChangeKey("Fixtures Source|120");
        assertEquals("Fixtures Source", actual.get("source"));
        assertEquals("120", actual.get("code"));
    }

    public void testDecodePropertyChangeKeyWithEscapedPipes() throws Exception {
        Map<String, String> actual = Activity.splitPropertyChangeKey("Pipe\\|y|12\\|0");
        assertEquals("Pipe|y", actual.get("source"));
        assertEquals("12|0", actual.get("code"));
    }

    public void testTransientClone() throws Exception {
        a0.setDescription("something");
        a0.setCode("S");
        Activity clone = a0.transientClone();
        assertNull(clone.getSource());
        assertTrue(clone.isMemoryOnly());
    }
}
