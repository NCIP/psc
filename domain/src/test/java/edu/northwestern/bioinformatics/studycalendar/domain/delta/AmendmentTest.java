package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.test.Fixtures;
import static edu.northwestern.bioinformatics.studycalendar.test.Fixtures.createAmendments;
import edu.nwu.bioinformatics.commons.DateUtils;
import gov.nih.nci.cabig.ctms.lang.DateTools;
import static gov.nih.nci.cabig.ctms.testing.MoreJUnitAssertions.*;
import junit.framework.TestCase;

import java.util.Calendar;
import java.util.Date;

/**
 * @author Rhett Sutphin
 */
public class AmendmentTest extends TestCase {
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

    public void testNaturalKeyWithBlankName() throws Exception {
        a0.setDate(DateTools.createDate(2022, Calendar.MARCH, 6));
        a0.setName(" ");
        assertEquals("2022-03-06", a0.getNaturalKey());
    }

    public void testDecomposeNaturalKeyWithName() throws Exception {
        Amendment.Key actual = Amendment.decomposeNaturalKey("2003-08-11~fred");
        assertDayOfDate(2003, Calendar.AUGUST, 11, actual.getDate());
        assertEquals("Wrong name", "fred", actual.getName());
    }

    public void testDecomposeNaturalKeyWithoutName() throws Exception {
        Amendment.Key actual = Amendment.decomposeNaturalKey("2001-01-12");
        assertDayOfDate(2001, Calendar.JANUARY, 12, actual.getDate());
        assertNull("Wrong name", actual.getName());
    }

    private void ensureUpdatedDate(Amendment expectedAmendment, Date expectedDate) {
        expectedAmendment.getDeltas().clear();
        Delta<?> delta = Delta.createDeltaFor(new Epoch(), PropertyChange.create("name", "A", "B"));
        delta.getChanges().get(0).setUpdatedDate(expectedDate);
        expectedAmendment.addDelta(delta);
    }

    public void testLastModifiedDateIsNullIfAllDatesAreNull() throws Exception {
        a3.setReleasedDate(null);
        ensureUpdatedDate(a3, null);

        assertNull(a3.getLastModifiedDate());
    }

    public void testLastModifiedDatePrefersUpdatedDateIfLater() throws Exception {
        a3.setReleasedDate(DateUtils.createDate(2007, Calendar.OCTOBER, 19));
        ensureUpdatedDate(a3, DateUtils.createDate(2007, Calendar.OCTOBER, 20));

        assertDayOfDate(2007, Calendar.OCTOBER, 20, a3.getLastModifiedDate());
    }

    public void testLastModifiedDatePrefersReleasedDateIfLater() throws Exception {
        a3.setReleasedDate(DateUtils.createDate(2007, Calendar.OCTOBER, 21));
        ensureUpdatedDate(a3, DateUtils.createDate(2007, Calendar.OCTOBER, 20));

        assertDayOfDate(2007, Calendar.OCTOBER, 21, a3.getLastModifiedDate());
    }

    public void testLastModifiedDatePrefersUpdatedDateIfReleasedDateNull() throws Exception {
        a3.setReleasedDate(null);
        ensureUpdatedDate(a3, DateUtils.createDate(2007, Calendar.OCTOBER, 20));

        assertDayOfDate(2007, Calendar.OCTOBER, 20, a3.getLastModifiedDate());
    }

    public void testLastModifiedDatePrefersReleasedDateIfNoDeltas() throws Exception {
        a3.setReleasedDate(DateUtils.createDate(2007, Calendar.OCTOBER, 20));
        a3.getDeltas().clear();

        assertDayOfDate(2007, Calendar.OCTOBER, 20, a3.getLastModifiedDate());
    }

    public void testLastModifiedDatePrefersReleasedDateIfNoChanges() throws Exception {
        a3.setReleasedDate(DateUtils.createDate(2007, Calendar.OCTOBER, 20));
        a3.addDelta(new EpochDelta());

        assertDayOfDate(2007, Calendar.OCTOBER, 20, a3.getLastModifiedDate());
    }

    public void testUpdatedDateWhenOneChangeHasAnUpdatedDate() throws Exception {
        a3.addDelta(Delta.createDeltaFor(new Epoch(),
            PropertyChange.create("name", "A", "B"), Add.create(new StudySegment())));
        a3.getDeltas().get(0).getChanges().get(0).setUpdatedDate(DateUtils.createDate(2005, Calendar.MAY, 3));

        assertDayOfDate(2005, Calendar.MAY, 3, a3.getUpdatedDate());
    }

    public void testUpdatedDateIsMaxWithinOneDelta() throws Exception {
        a3.addDelta(Delta.createDeltaFor(new Epoch(),
            PropertyChange.create("name", "A", "B"), Add.create(new StudySegment())));
        a3.getDeltas().get(0).getChanges().get(0).setUpdatedDate(DateUtils.createDate(2005, Calendar.MAY, 3));
        a3.getDeltas().get(0).getChanges().get(1).setUpdatedDate(DateUtils.createDate(2005, Calendar.MAY, 4));

        assertDayOfDate(2005, Calendar.MAY, 4, a3.getUpdatedDate());
    }
    
    public void testUpdatedDateIsMaxAcrossDeltas() throws Exception {
        a3.addDelta(Delta.createDeltaFor(new Epoch(),
            PropertyChange.create("name", "A", "B")));
        a3.addDelta(Delta.createDeltaFor(new Epoch(),
            Add.create(new StudySegment())));
        a3.getDeltas().get(0).getChanges().get(0).setUpdatedDate(DateUtils.createDate(2005, Calendar.MAY, 3));
        a3.getDeltas().get(1).getChanges().get(0).setUpdatedDate(DateUtils.createDate(2005, Calendar.MAY, 4));

        assertDayOfDate(2005, Calendar.MAY, 4, a3.getUpdatedDate());
    }

    public void testCloneDeepClonesDeltas() throws Exception {
        Amendment src = Fixtures.createAmendment(null, DateTools.createDate(2008, Calendar.JUNE, 22));
        src.addDelta(Delta.createDeltaFor(new Study()));

        Amendment clone = src.clone();
        assertEquals("Wrong number of cloned changes", 1, clone.getDeltas().size());
        assertNotSame("Deltas not deep cloned", src.getDeltas().get(0), clone.getDeltas().get(0));
    }
    
    public void testCloneDeepClonesPreviousAmendments() throws Exception {
        Amendment src = Fixtures.createAmendments("C", "B", "A");
        Amendment clone = src.clone();

        assertNotSame("Previous amendment not cloned",
            src.getPreviousAmendment(), clone.getPreviousAmendment());
        assertNotSame("Previous previous amendment not cloned",
            src.getPreviousAmendment().getPreviousAmendment(),
            clone.getPreviousAmendment().getPreviousAmendment());
    }

    public void testSetMemOnlyRecursiveToDeltas() throws Exception {
        Amendment amendment = Fixtures.createAmendment(null, DateTools.createDate(2008, Calendar.JUNE, 22));
        amendment.addDelta(Delta.createDeltaFor(new Study()));
        amendment.setMemoryOnly(true);
        assertTrue(amendment.getDeltas().get(0).isMemoryOnly());
    }

    public void testSetMemOnlyRecursiveToPreviousAmendments() throws Exception {
        Amendment amendment = Fixtures.createAmendments("C", "B", "A");
        amendment.setMemoryOnly(true);
        assertTrue(amendment.getPreviousAmendment().isMemoryOnly());
    }
}
