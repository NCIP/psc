package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.domain.tools.Differences;
import static gov.nih.nci.cabig.ctms.testing.MoreJUnitAssertions.assertOrder;
import junit.framework.TestCase;

/**
 * @author Rhett Sutphin
 */
public class PlannedActivityLabelTest extends TestCase {
    private PlannedActivityLabel pal0, pal1;
    private PlannedActivity pa;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        pa = Fixtures.createPlannedActivity("EKG", 2);
        pal0 = Fixtures.createPlannedActivityLabel(pa, "okay", 5);
        pal1 = Fixtures.createPlannedActivityLabel(pa, "okay", 3);
    }
    
    public void testLabelsAreAlwaysLowerCase() throws Exception {
        pal0.setLabel("FOOm");
        assertEquals("foom", pal0.getLabel());
    }

    public void testLabelsAreStrippedOfWhitespace() throws Exception {
        pal0.setLabel("a nice boat");
        assertEquals("a-nice-boat", pal0.getLabel());
    }

    public void testLabelWhitespaceIsMerged() throws Exception {
        pal0.setLabel("a \tfloating \n home");
        assertEquals("a-floating-home", pal0.getLabel());
    }

    public void testSetLabelNullWorks() throws Exception {
        pal0.setLabel(null);
    }

    public void testNaturalOrderIsByLabelFirst() throws Exception {
        pal0.setLabel("a"); pal1.setLabel("b");
        pal0.setRepetitionNumber(3); pal1.setRepetitionNumber(1);
        assertOrder(pal0, pal1);
    }

    public void testNaturalOrderIsByRepetitionSecond() throws Exception {
        pal0.setLabel("a"); pal1.setLabel("a");
        pal0.setRepetitionNumber(3); pal1.setRepetitionNumber(1);
        assertOrder(pal1, pal0);
    }

    public void testNaturalOrderPutsNullRepNumberFirst() throws Exception {
        pal0.setLabel("a"); pal1.setLabel("a");
        pal0.setRepetitionNumber(null); pal1.setRepetitionNumber(1);
        assertOrder(pal0, pal1);
    }

    public void testClone() throws Exception {
        pal0.setLabel("etc");
        PlannedActivityLabel clone = pal0.clone();
        assertNotSame(pal0, clone);
        assertNull("Parent not cleared", clone.getPlannedActivity());
        assertEquals(pal0.getLabel(), clone.getLabel());
    }
    
    public void testTransientClone() throws Exception {
        pal0.setLabel("bar");
        PlannedActivityLabel clone = pal0.transientClone();
        assertNotSame(clone, pal0);
        assertNull("Parent not cleared", clone.getPlannedActivity());
        assertEquals(pal0.getLabel(), clone.getLabel());
        assertTrue(clone.isMemoryOnly());
    }
    
   public void testIsDetachedWhenPlannedActivityNotPresent() throws Exception {
        PlannedActivityLabel pal = new PlannedActivityLabel();
        assertTrue("Planned activity label is attached to planned activity ", pal.isDetached());
    }

    public void testIsDetachedWhenPlannedActivityIsPresent() throws Exception {
        PlannedActivityLabel pal = new PlannedActivityLabel();
        pal.setPlannedActivity(pa);
        assertFalse("Planned activity label is detached from planned activity ", pal.isDetached());
    }
    
    public void testAppliesToAnyRepetitionWhenRepNumIsNull() {
        pal0.setRepetitionNumber(null);
        assertTrue(pal0.appliesToRepetition(4));
        assertTrue(pal0.appliesToRepetition(2));
        assertTrue(pal0.appliesToRepetition(13));
        assertTrue(pal0.appliesToRepetition(30));
    }

    public void testAppliesOnlyToSpecificRepWhenRepNumIsConcrete() {
        pal0.setRepetitionNumber(3);
        assertFalse(pal0.appliesToRepetition(0));
        assertFalse(pal0.appliesToRepetition(4));
        assertTrue(pal0.appliesToRepetition(3));
    }

    public void testDeepEqualsForDifferentLabel() throws Exception {
        PlannedActivityLabel pal1 = Fixtures.createPlannedActivityLabel("Label1", 5);
        PlannedActivityLabel pal2 = Fixtures.createPlannedActivityLabel("Label2", 5);
        Differences differences = pal1.deepEquals(pal2);
        assertNotNull(differences.getMessages());
        assertEquals("Planned Activity Labels are equals", "label label1 differs to label2", differences.getMessages().get(0));
    }

    public void testDeepEqualsForDifferentRepetitionNo() throws Exception {
        PlannedActivityLabel pal1 = Fixtures.createPlannedActivityLabel("Label", 5);
        PlannedActivityLabel pal2 = Fixtures.createPlannedActivityLabel("Label", 8);
        Differences differences = pal1.deepEquals(pal2);
        assertNotNull(differences.getMessages());
        assertEquals("Planned Activity Labels are equals", "label repetition number 5 differs to 8", differences.getMessages().get(0));
    }

}
