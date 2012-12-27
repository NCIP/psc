/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.domain.tools.Differences;

import java.util.Map;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createSource;
import static gov.nih.nci.cabig.ctms.testing.MoreJUnitAssertions.*;

/**
 * @author Rhett Sutphin
 */
public class ActivityTest extends DomainTestCase {
    private Activity a0, a1, reconsent;

    protected void setUp() throws Exception {
        super.setUp();
        a0 = Fixtures.createActivity("Activity 0", Fixtures.createNamedInstance("DISEASE_MEASURE", ActivityType.class));
        a1 = Fixtures.createActivity("Activity 1", Fixtures.createNamedInstance("OTHER", ActivityType.class));
        reconsent = Fixtures.createActivity("Reconsent");
        reconsent.setType(null);
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
    
    public void testNaturalOrderingMayIncludeReconsents() throws Exception {
        assertPositive(a0.compareTo(reconsent));
        assertNegative(reconsent.compareTo(a0));
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

    public void testDeepEqualsForSameActivity() throws Exception {
        Activity a = Fixtures.createActivity("A1");
        Activity b = Fixtures.createActivity("A1");
        Differences differences = a.deepEquals(b);
        assertTrue("ACtivities are different", differences.getMessages().isEmpty());
    }

    public void testDeepEqualsForDifferentActivityName() throws Exception {
        Activity a = Fixtures.createActivity("A1", "A");
        Activity b = Fixtures.createActivity("A2", "A");

        assertDifferences(a.deepEquals(b), "name \"A1\" does not match \"A2\"");
    }

    public void testDeepEqualsForDifferentActivityCode() throws Exception {
        Activity a = Fixtures.createActivity("A", "a1");
        Activity b = Fixtures.createActivity("A", "a2");

        assertDifferences(a.deepEquals(b), "code \"a1\" does not match \"a2\"");
    }

    public void testDeepEqualsForDifferentActivityDescription() throws Exception {
        Activity a = Fixtures.createActivity("A");
        a.setDescription("foo");
        Activity b = Fixtures.createActivity("A");
        b.setDescription("bar");

        assertDifferences(a.deepEquals(b), "description \"foo\" does not match \"bar\"");
    }

    public void testDeepEqualsForDifferentSource() throws Exception {
        Activity a = Fixtures.createActivity("A", "A", createSource("The Sea"), null);
        Activity b = Fixtures.createActivity("A", "A", createSource("The Sky"), null);

        assertDifferences(a.deepEquals(b), "source The Sea does not match The Sky");
    }

    public void testDeepEqualsForDifferentType() throws Exception {
        Activity a = Fixtures.createActivity("A", Fixtures.createActivityType("DISEASE_MEASURE"));
        Activity b = Fixtures.createActivity("A", Fixtures.createActivityType("PROCEDURE"));

        assertDifferences(a.deepEquals(b), "type DISEASE_MEASURE does not match PROCEDURE");
    }

    public void testDeepEqualsForActivityProperty() throws Exception {
        Activity a = Fixtures.createActivity("A");
        Activity b = Fixtures.createActivity("A");
        Fixtures.addActivityProperty(a, "namespace", "name", "value");
        Fixtures.addActivityProperty(b, "namespace1", "name1", "value");

        assertDifferences(a.deepEquals(b),
            "missing property namespace:name:value",
            "extra property namespace1:name1:value");
    }
}
