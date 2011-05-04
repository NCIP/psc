package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.domain.tools.Differences;
import junit.framework.TestCase;

import java.util.Map;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createSource;
import static gov.nih.nci.cabig.ctms.testing.MoreJUnitAssertions.*;

/**
 * @author Rhett Sutphin
 */
public class ActivityTest extends TestCase {
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
        Activity a1 = Fixtures.createActivity("A1");
        Activity a2 = Fixtures.createActivity("A1");
        Differences differences = a1.deepEquals(a2);
        assertTrue("ACtivities are different", differences.getMessages().isEmpty());
    }

    public void testDeepEqualsForDifferentActivityName() throws Exception {
        Activity a1 = Fixtures.createActivity("A1");
        Activity a2 = Fixtures.createActivity("A2");
        Differences differences = a1.deepEquals(a2);
        assertFalse(differences.getMessages().isEmpty());
        assertEquals("Activiy is not different", "activity name \"A1\" does not match \"A2\"", differences.getMessages().get(0));
    }

    public void testDeepEqualsForDifferentActivityCode() throws Exception {
        Activity a1 = Fixtures.createActivity("A");
        a1.setCode("a1");
        Activity a2 = Fixtures.createActivity("A");
        a2.setCode("a2");
        Differences differences = a1.deepEquals(a2);
        assertFalse(differences.getMessages().isEmpty());
        assertEquals("Activiy is not different", "activity code \"a1\" does not match \"a2\"", differences.getMessages().get(0));
    }

    public void testDeepEqualsForDifferentActivityDescription() throws Exception {
        Activity a1 = Fixtures.createActivity("A");
        a1.setDescription("foo");
        Activity a2 = Fixtures.createActivity("A");
        a2.setDescription("bar");
        Differences differences = a1.deepEquals(a2);
        assertFalse(differences.getMessages().isEmpty());
        assertEquals("Activiy is not different", "activity description \"foo\" does not match \"bar\"", differences.getMessages().get(0));
    }

    public void testDeepEqualsForDifferentSource() throws Exception {
        Source source1 = createSource("Source1");
        Source source2 = createSource("Source2");
        Activity a1 = Fixtures.createActivity("A");
        Activity a2 = Fixtures.createActivity("A");
        a1.setSource(source1);
        a2.setSource(source2);
        Differences differences = a1.deepEquals(a2);
        assertFalse(differences.getChildDifferences().isEmpty());
        assertEquals("Activiy is not different", "Source name Source1 differs to Source2", differences.getChildDifferences().get("Activity A").getMessages().get(0));
    }

    public void testDeepEqualsForDifferentType() throws Exception {
        ActivityType type1 = Fixtures.createActivityType("DISEASE_MEASURE");
        ActivityType type2 = Fixtures.createActivityType("PROCEDURE");
        Activity a1 = Fixtures.createActivity("A");
        Activity a2 = Fixtures.createActivity("A");
        a1.setType(type1);
        a2.setType(type2);
        Differences differences = a1.deepEquals(a2);
        assertFalse(differences.getChildDifferences().isEmpty());
        assertEquals("Activiy is not different", "ActivityType name DISEASE_MEASURE differs to PROCEDURE", differences.getChildDifferences().get("Activity A").getMessages().get(0));
    }

    public void testDeepEqualsForActivityProperty() throws Exception {
        Activity a1 = Fixtures.createActivity("A");
        Activity a2 = Fixtures.createActivity("A");
        ActivityProperty ap1 = Fixtures.createActivityProperty("namespace","name","value");
        ActivityProperty ap2 = Fixtures.createActivityProperty("namespace1","name1","value");
        a1.addProperty(ap1);
        a2.addProperty(ap2);
        Differences differences = a1.deepEquals(a2);
        Differences childDifferences = differences.getChildDifferences().get("Activity A");
        assertFalse(differences.getChildDifferences().isEmpty());
        assertEquals("Activiy is not different", "ActivityProperty name name differs to name1",
                childDifferences.getMessages().get(0));
        assertEquals("Activiy is not different", "ActivityProperty namespace namespace differs to namespace1",
                childDifferences.getMessages().get(1));
    }
}
