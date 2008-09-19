package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

/**
 * @author Rhett Sutphin
 */
public class PlannedActivityLabelTest extends StudyCalendarTestCase {
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
}
