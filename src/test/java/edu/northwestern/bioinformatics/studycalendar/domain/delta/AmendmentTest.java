package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createAmendments;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import gov.nih.nci.cabig.ctms.lang.DateTools;

import java.util.Calendar;

/**
 * @author Rhett Sutphin
 */
public class AmendmentTest extends StudyCalendarTestCase {
    private Amendment a3, a2, a1, a0;
    private Amendment b2;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        a3 = createAmendments("A0", "A1", "A2", "A3");
        a2 = a3.getPreviousAmendment();
        a1 = a2.getPreviousAmendment();
        a0 = a1.getPreviousAmendment();

        b2 = createAmendments("B0", "B1", "B2");
    }
    
    public void testIsPreviousAmendment() throws Exception {
        assertFalse(a3.hasPreviousAmendment(a3));
        assertTrue(a3.hasPreviousAmendment(a2));
        assertTrue(a3.hasPreviousAmendment(a1));
        assertTrue(a3.hasPreviousAmendment(a0));

        assertFalse(a2.hasPreviousAmendment(a3));
        assertFalse(a2.hasPreviousAmendment(a2));
        assertTrue(a2.hasPreviousAmendment(a1));
        assertTrue(a2.hasPreviousAmendment(a0));

        assertFalse(a1.hasPreviousAmendment(a3));
        assertFalse(a1.hasPreviousAmendment(a2));
        assertFalse(a1.hasPreviousAmendment(a1));
        assertTrue(a1.hasPreviousAmendment(a0));

        assertFalse(a2.hasPreviousAmendment(b2));
    }
    
    public void testCount() throws Exception {
        assertEquals(3, a3.getPreviousAmendmentsCount());
        assertEquals(2, a2.getPreviousAmendmentsCount());
        assertEquals(1, a1.getPreviousAmendmentsCount());
        assertEquals(0, a0.getPreviousAmendmentsCount());
    }

    public void testDisplayNameWithDateOnly() throws Exception {
        a3.setName(null);
        a3.setDate(DateTools.createDate(2005, Calendar.MARCH, 6));
        assertEquals("03/06/2005", a3.getDisplayName());
    }

    public void testDisplayNameWithBlankName() throws Exception {
        a3.setName("  ");
        a3.setDate(DateTools.createDate(2005, Calendar.MARCH, 6));
        assertEquals("03/06/2005", a3.getDisplayName());
    }

    public void testDisplayNameWithDateAndName() throws Exception {
        a3.setDate(DateTools.createDate(2005, Calendar.MARCH, 6));
        assertEquals("03/06/2005 (A3)", a3.getDisplayName());
    }
    
    public void testDisplayNameForOriginalAmendment() throws Exception {
        a0.setName(Amendment.INITIAL_TEMPLATE_AMENDMENT_NAME);
        assertEquals("Initial template", a0.getDisplayName());
    }

    public void testIsInitialTemplate() throws Exception {
        a0.setName(null);
        assertFalse(a0.isInitialTemplate());
        a0.setName("3");
        assertFalse(a0.isInitialTemplate());
        a0.setName(Amendment.INITIAL_TEMPLATE_AMENDMENT_NAME);
        assertTrue(a0.isInitialTemplate());
    }

    public void testDefaultsToMandatory() throws Exception {
        assertTrue(new Amendment().isMandatory());
    }

    public void testNaturalKeyWithName() throws Exception {
        a0.setDate(DateTools.createDate(2005, Calendar.MAY, 4));
        a0.setName("4");
        assertEquals("2005-05-04~4", a0.getNaturalKey());
    }

    public void testNaturalKeyWithoutName() throws Exception {
        a0.setDate(DateTools.createDate(2009, Calendar.NOVEMBER, 6));
        a0.setName(null);
        assertEquals("2009-11-06", a0.getNaturalKey());
    }

    public void testDecomposeNaturalKeyWithName() throws Exception {
        Amendment.Key actual = Amendment.decomposeNaturalKey("2003-08-11~fred");
        assertDayOfDate(2003, Calendar.AUGUST,  11, actual.getDate());
        assertEquals("Wrong name", "fred", actual.getName());
    }
    
    public void testDecomposeNaturalKeyWithoutName() throws Exception {
        Amendment.Key actual = Amendment.decomposeNaturalKey("2001-01-12");
        assertDayOfDate(2001, Calendar.JANUARY,  12, actual.getDate());
        assertNull("Wrong name", actual.getName());
    }
}
