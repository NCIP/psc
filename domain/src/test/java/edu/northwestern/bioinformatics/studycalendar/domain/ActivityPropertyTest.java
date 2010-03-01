package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.domain.tools.Differences;
import junit.framework.TestCase;


/**
 * @author Jalpa Patel
 */
public class ActivityPropertyTest extends TestCase {
    private ActivityProperty ap1, ap2;
    
    public void testDeepEqualsForDifferentNameSpace() throws Exception {
        ap1 = Fixtures.createActivityProperty("namespace","name","value");
        ap2 = Fixtures.createActivityProperty("namespace1","name","value");
        Differences differences = ap1.deepEquals(ap2);
        assertFalse(differences.getMessages().isEmpty());
        assertEquals("Activiy Property is not different", "ActivityProperty namespace namespace differs to namespace1", differences.getMessages().get(0));
    }

    public void testDeepEqualsForDifferentValue() throws Exception {
        ap1 = Fixtures.createActivityProperty("namespace","name","value");
        ap2 = Fixtures.createActivityProperty("namespace1","name","value1");
        Differences differences = ap1.deepEquals(ap2);
        assertFalse(differences.getMessages().isEmpty());
        assertEquals("Activiy Property is not different", "ActivityProperty value value differs to value1", differences.getMessages().get(0));
    }

    public void testDeepEqualsForDifferentName() throws Exception {
        ap1 = Fixtures.createActivityProperty("namespace","name","value");
        ap2 = Fixtures.createActivityProperty("namespace","name1","value");
        Differences differences = ap1.deepEquals(ap2);
        assertFalse(differences.getMessages().isEmpty());
        assertEquals("Activiy Property is not different", "ActivityProperty name name differs to name1", differences.getMessages().get(0));
    }

    public void testDeepEqualsForAllDifferentValue() throws Exception {
        ap1 = Fixtures.createActivityProperty("namespace","name","value");
        ap2 = Fixtures.createActivityProperty("namespace1","name1","value1");
        Differences differences = ap1.deepEquals(ap2);
        assertFalse(differences.getMessages().isEmpty());
        assertEquals(3, differences.getMessages().size());
        assertEquals("Activiy Property is not different", "ActivityProperty name name differs to name1", differences.getMessages().get(0));
        assertEquals("Activiy Property is not different", "ActivityProperty value value differs to value1", differences.getMessages().get(1));
        assertEquals("Activiy Property is not different", "ActivityProperty namespace namespace differs to namespace1", differences.getMessages().get(2));
    }

    public void testDeepEqualsForSameActivityProperty() throws Exception {
        ap1 = Fixtures.createActivityProperty("namespace","name","value");
        ap2 = Fixtures.createActivityProperty("namespace","name","value");
        Differences differences = ap1.deepEquals(ap2);
        assertTrue("Activity Properties are different", differences.getMessages().isEmpty());
    }
}
