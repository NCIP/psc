/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.domain.tools.Differences;

import static edu.northwestern.bioinformatics.studycalendar.domain.DomainAssertions.assertDayRange;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import static gov.nih.nci.cabig.ctms.testing.MoreJUnitAssertions.*;

/**
 * @author Moses Hohman
 * @author Rhett Sutphin
 */
public class PeriodTest extends DomainTestCase {
    private Period period;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        period = createPeriod("Test", 7, 28, 4);
    }

    public void testDisplayNameWithName() throws Exception {
        period.setName("Name-o");
        assertEquals("Name-o", period.getDisplayName());
    }

    public void testDisplayNameWithNoName() throws Exception {
        period.setName(null);
        assertEquals("[period]", period.getDisplayName());
    }

    public void testDurationNeverNull() {
        Period p = new Period();
        p.setDuration(null);
        assertNotNull(p.getDuration());
    }

    public void testDefaults() throws Exception {
        period = new Period();
        assertEquals("Default start day is 1", 1, (int) period.getStartDay());
        assertEquals("Default duration is 1 day", 1, (int) period.getDuration().getQuantity());
        assertEquals("Default duration is 1 day", Duration.Unit.day, period.getDuration().getUnit());
        assertEquals("Default reps is 1", 1, period.getRepetitions());
    }

    public void testDayRange() {
        period.setStartDay(1);
        period.setRepetitions(2);
        period.setDuration(new Duration(2, Duration.Unit.week));
        assertDayRange(1, 28, period.getTotalDayRange());
    }

    public void testDayRangeWithNegativeStart() throws Exception {
        period.setStartDay(-7);
        period.setDuration(new Duration(4, Duration.Unit.day));
        period.setRepetitions(1);

        assertDayRange(-7, -4, period.getTotalDayRange());
        period.setRepetitions(2);
        assertDayRange(-7, 0, period.getTotalDayRange());
        period.setRepetitions(3);
        assertDayRange(-7, 4, period.getTotalDayRange());
    }

    public void testAddPlannedActivityMaintainsBidirectionality() throws Exception {
        PlannedActivity e = createPlannedActivity("Any", 5);
        assertNull(e.getPeriod());
        period.addPlannedActivity(e);
        assertSame(period, e.getPeriod());
    }

    public void testAddPlannedActivitiesAdds() throws Exception {
        assertEquals(0, period.getPlannedActivities().size());
        PlannedActivity e = createPlannedActivity("Any", 5);
        period.addPlannedActivity(e);

        assertEquals(1, period.getPlannedActivities().size());
        assertSame(e, period.getPlannedActivities().iterator().next());
    }

    public void testAddPlannedActivityWithNoDay() throws Exception {
        PlannedActivity pa = new PlannedActivity();
        try {
            period.addChild(pa);
            fail("Exception not thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("Cannot add a planned activity without a day", e.getMessage());
        }
    }

    public void testAddPlannedActivityOutOfDayRangeHighFails() throws Exception {
        PlannedActivity pa = createPlannedActivity("hi", 29);
        try {
            period.addChild(pa);
            fail("Exception not thrown");
        } catch (StudyCalendarValidationException scve) {
            assertEquals(
                    "Cannot add a planned activity for day 29 to " + period + ".  Planned activity days always start with 1.  The maximum for this period is 28.  The offending planned activity is " + pa + '.',
                    scve.getMessage());
        }
    }

    public void testAddPlannedActivityOutOfDayRangeLowFails() throws Exception {
        period.getDuration().setQuantity(51);
        PlannedActivity pa = createPlannedActivity("lo", -4);
        try {
            period.addChild(pa);
            fail("Exception not thrown");
        } catch (StudyCalendarValidationException scve) {
            assertEquals(
                    "Cannot add a planned activity for day -4 to " + period + ".  Planned activity days always start with 1.  The maximum for this period is 51.  The offending planned activity is " + pa + '.',
                    scve.getMessage());
        }
    }

    public void testAddPlannedActivityAtDayZeroFails() throws Exception {
        period.getDuration().setUnit(Duration.Unit.week);
        PlannedActivity pa = createPlannedActivity("Darryl", 0);
        try {
            period.addChild(pa);
            fail("Exception not thrown");
        } catch (StudyCalendarValidationException scve) {
            assertEquals(
                    "Cannot add a planned activity for day 0 to " + period + ".  Planned activity days always start with 1.  The maximum for this period is 196.  The offending planned activity is " + pa + '.',
                    scve.getMessage());
        }
    }

    public void testAddPlannedActivityWithNoDurationFails() throws Exception {
        period.getDuration().setQuantity(null);
        PlannedActivity pa = createPlannedActivity("F", 8);
        try {
            period.addChild(pa);
            fail("Exception not thrown");
        } catch (IllegalStateException ise) {
            assertEquals("Cannot add a planned activity unless the period has a duration",
                    ise.getMessage());
        }
    }

    public void testSortAscendingByStartDayFirst() throws Exception {
        Period p1 = createPeriod("DC1", 1, 7, 1);
        Period p2 = createPeriod("DC2", 2, 6, 1);
        assertNegative(p1.compareTo(p2));
        assertPositive(p2.compareTo(p1));
    }

    public void testSortAscendingByTotalDaysSecond() throws Exception {
        Period p1 = createPeriod("DC1", 2, 7, 1);
        Period p2 = createPeriod("DC2", 2, 3, 2);
        assertNegative(p2.compareTo(p1));
        assertPositive(p1.compareTo(p2));
    }

    public void testSortDescendingByRepetitionsThird() throws Exception {
        Period p1 = createPeriod("DC1", 1, 21, 1);
        Period p2 = createPeriod("DC2", 1, 7, 3);
        assertNegative(p2.compareTo(p1));
        assertPositive(p1.compareTo(p2));
    }

    public void testSortByNameLast() throws Exception {
        Period p1 = createPeriod("B", 1, 7, 1);
        Period p2 = createPeriod("A", 1, 7, 1);
        assertNegative(p2.compareTo(p1));
        assertPositive(p1.compareTo(p2));
    }

    public void testIsFirstDayOfRep() throws Exception {
        Period p1 = createPeriod("A", 1, 7, 8);
        assertTrue(p1.isFirstDayOfRepetition(1));
        assertTrue(p1.isFirstDayOfRepetition(8));
        assertTrue(p1.isFirstDayOfRepetition(15));
        assertTrue(p1.isFirstDayOfRepetition(50));
        assertFalse("Out of range considered first day", p1.isFirstDayOfRepetition(57));
        assertFalse("Second day considered first day", p1.isFirstDayOfRepetition(2));
        assertFalse("Second day considered first day", p1.isFirstDayOfRepetition(9));
    }

    public void testIsFirstDayOfRepNonOnePeriodStart() throws Exception {
        Period p1 = createPeriod("B", 4, 5, 5);
        assertTrue(p1.isFirstDayOfRepetition(4));
        assertTrue(p1.isFirstDayOfRepetition(9));
        assertTrue(p1.isFirstDayOfRepetition(14));
        assertTrue(p1.isFirstDayOfRepetition(19));
        assertTrue(p1.isFirstDayOfRepetition(24));
        assertFalse("Out of range considered first day", p1.isFirstDayOfRepetition(29));
        assertFalse("Second day considered first day", p1.isFirstDayOfRepetition(5));
        assertFalse("Second day considered first day", p1.isFirstDayOfRepetition(10));
    }

    public void testIsLastDayOfRep() throws Exception {
        Period p1 = createPeriod("A", 1, 7, 8);
        assertTrue(p1.isLastDayOfRepetition(7));
        assertTrue(p1.isLastDayOfRepetition(14));
        assertTrue(p1.isLastDayOfRepetition(21));
        assertTrue(p1.isLastDayOfRepetition(56));
        assertFalse("Out of range considered last day", p1.isLastDayOfRepetition(63));
        assertFalse("Second day considered last day", p1.isLastDayOfRepetition(2));
        assertFalse("Second day considered last day", p1.isLastDayOfRepetition(9));
    }

    public void testIsLastDayOfRepNonOnePeriodStart() throws Exception {
        Period p1 = createPeriod("B", 4, 5, 5);
        assertTrue(p1.isLastDayOfRepetition(8));
        assertTrue(p1.isLastDayOfRepetition(13));
        assertTrue(p1.isLastDayOfRepetition(18));
        assertTrue(p1.isLastDayOfRepetition(23));
        assertTrue(p1.isLastDayOfRepetition(28));
        assertFalse("Out of range considered last day", p1.isLastDayOfRepetition(33));
        assertFalse("Second day considered last day", p1.isLastDayOfRepetition(5));
        assertFalse("Second day considered last day", p1.isLastDayOfRepetition(10));
    }

    public void testIsFirstDayOfRepWithNegDays() throws Exception {
        Period p1 = createPeriod("A", -6, 5, 2);
        assertTrue(p1.isFirstDayOfRepetition(-6));
        assertTrue(p1.isFirstDayOfRepetition(-1));
        assertFalse("Out of range considered first day", p1.isFirstDayOfRepetition(5));
        assertFalse("Second day considered first day", p1.isFirstDayOfRepetition(-5));
    }

    public void testIsLastDayOfRepWithNegDays() throws Exception {
        Period p1 = createPeriod("A", -6, 5, 2);
        assertTrue(p1.isLastDayOfRepetition(-2));
        assertTrue(p1.isLastDayOfRepetition(3));
        assertFalse("Out of range considered last day", p1.isFirstDayOfRepetition(5));
        assertFalse("Second day considered last day", p1.isFirstDayOfRepetition(-5));
    }

    public void testCloneClonesDuration() throws Exception {
        Period p1 = new Period();
        p1.getDuration().setQuantity(17);
        Period p1c = p1.clone();
        p1c.getDuration().setQuantity(42);
        assertEquals("Update wrote through from clone to original", 17, (int) p1.getDuration().getQuantity());
        assertEquals("Update of clone didn't take", 42, (int) p1c.getDuration().getQuantity());
        assertNotSame(p1.getDuration(), p1c.getDuration());
    }

    public void testCopyPeriod() throws Exception {
        period = new Period();
        period.setParent(new StudySegment());
        period.setGridId("grid 0");
        period.setId(1);
        period.getDuration().setQuantity(17);
        period.getDuration().setUnit(Duration.Unit.month);
        period.setStartDay(2);
        period.setRepetitions(3);
        Period copiedPeriod = (Period) period.copy();

        assertNotNull(copiedPeriod);
        assertNull(copiedPeriod.getId());
        assertNull(copiedPeriod.getGridId());

        assertNotSame("copied node and source node must be different", copiedPeriod, period);

        assertEquals("must copy name", copiedPeriod.getName(), period.getName());
        assertEquals("must copy repetitions", copiedPeriod.getRepetitions(), period.getRepetitions());
        assertEquals("must copy startDay", copiedPeriod.getStartDay(), period.getStartDay());
        assertEquals("must copy duration", copiedPeriod.getDuration(), period.getDuration());
        assertNotSame("duration must not be same reference", copiedPeriod.getDuration(), period.getDuration());


    }

    public void testClonePlannedActivity() throws Exception {
        Period p1 = new Period();
        PlannedActivity pa1 = new PlannedActivity();
        pa1.setDay(1);
        p1.addPlannedActivity(pa1);
        p1.getDuration().setQuantity(17);
        Period p1c = p1.clone();
        p1c.getDuration().setQuantity(42);
        assertEquals("must clone planned activities also", 1, p1c.getPlannedActivities().size());
        assertSame(p1c.getPlannedActivities().get(0).getParent(), p1c);
    }

    public void testCompareTo() {
        period = createPeriod("name", 3, Duration.Unit.day, 15, 3);
        Period anotherPeriod = createPeriod("name", 3, Duration.Unit.day, 15, 3);
        assertTrue("both periods are not equal", anotherPeriod.compareTo(period) == 0);
        period.setId(1);
        assertTrue("both periods are equal", anotherPeriod.compareTo(period) > 0);
    }

    public void testFindMatchingPlannedActivityWhenPresent() throws Exception {
        PlannedActivity pa1 = setGridId("Inf", createPlannedActivity("Aleph", 1));
        PlannedActivity pa2 = setGridId("Zer0", createPlannedActivity("Null", 8));
        period.addPlannedActivity(pa1);
        period.addPlannedActivity(pa2);
        assertSame("Not found", pa1, period.findNaturallyMatchingChild("Inf"));
    }

    public void testFindMatchingPlannedActivityWhenNotPresent() throws Exception {
        PlannedActivity pa1 = setGridId("Inf", createPlannedActivity("Aleph", 1));
        PlannedActivity pa2 = setGridId("Zer0", createPlannedActivity("Null", 8));
        period.addPlannedActivity(pa1);
        period.addPlannedActivity(pa2);
        assertNull(period.findNaturallyMatchingChild("Zip"));
    }
    
    public void testEqualsWhenPeriodsHaveSameAttributes() throws Exception {
        Period p1 = createPeriod("Test", 7, 28, 4);
        Period p2 = createPeriod("Test", 7, 28, 4);
        assertEquals("Periods are not equals", p1, p2);
    }

    public void testEqualsWhenPeriodsHaveDifferentName() throws Exception {
        Period p1 = createPeriod("Test1", 7, 28, 4);
        Period p2 = createPeriod("Test2", 7, 28, 4);
        assertNotEquals("Periods are equals", p1, p2);
    }

    public void testEqualsWhenPeriodsHaveDifferentDuration() throws Exception {
        Period p1 = createPeriod("Test", 7, 30, 4);
        Period p2 = createPeriod("Test", 7, 28, 4);
        assertNotEquals("Periods are equals", p1, p2);
    }

    public void testDeepEqualsForDifferentRepetitions() throws Exception {
        Period a = createPeriod("Test1", 7, 28, 4);
        Period b = createPeriod("Test1", 7, 28, 5);

        assertDifferences(a.deepEquals(b), "repetition does not match: 4 != 5");
    }

    public void testDeepEqualsForDifferentName() throws Exception {
        Period a = createPeriod("Test1", 7, 28, 4);
        Period b = createPeriod("Test2", 7, 28, 4);

        assertDifferences(a.deepEquals(b), "name \"Test1\" does not match \"Test2\"");
    }

    public void testDeepEqualsForDifferentStartDay() throws Exception {
        Period a = createPeriod("Test1", 7, 28, 4);
        Period b = createPeriod("Test1", 8, 28, 4);

        assertDifferences(a.deepEquals(b), "start day does not match: 7 != 8");
    }

    public void testDeepEqualsForDifferentDurationUnit() throws Exception {
        Period a = createPeriod("Test1", 7, Duration.Unit.day, 10, 4);
        Period b = createPeriod("Test1", 7, Duration.Unit.week, 10, 4);

        assertChildDifferences(a.deepEquals(b), "duration", "unit day does not match week");
    }

    public void testDeepEqualsForDifferentDurationQuantity() throws Exception {
        Period a = createPeriod("Test1", 7, Duration.Unit.day, 10, 4);
        Period b = createPeriod("Test1", 7, Duration.Unit.day, 15, 4);

        assertChildDifferences(a.deepEquals(b), "duration", "quantity does not match: 10 != 15");
    }

    public void testDeepEqualsForDifferentPlannedActivities() throws Exception {
        Activity flight = createActivity("Flight", "F");
        Activity drive = createActivity("Drive", "F");
        PlannedActivity fl1 = setGridId("FL-1", createPlannedActivity(flight, 1));
        PlannedActivity fl5 = setGridId("FL-5", createPlannedActivity(flight, 5));
        PlannedActivity dr1 = setGridId("FL-1", createPlannedActivity(drive, 1));
        PlannedActivity dr2 = setGridId("DR-2", createPlannedActivity(drive, 2));
        Period a = createPeriod("Test1", 7, 28, 4);
        Period b = createPeriod("Test1", 7, 28, 4);
        a.addPlannedActivity(fl1);
        a.addPlannedActivity(fl5);
        b.addPlannedActivity(dr1);
        b.addPlannedActivity(fl5);
        b.addPlannedActivity(dr2);

        Differences actual = a.deepEquals(b);
        assertChildDifferences(actual,
            new String[] { "planned activity FL-1", "activity" },
            "name \"Flight\" does not match \"Drive\"");
        assertDifferences(actual, "extra planned activity DR-2");
    }
}
