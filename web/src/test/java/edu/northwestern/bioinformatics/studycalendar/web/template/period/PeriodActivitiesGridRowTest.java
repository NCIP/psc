/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.template.period;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.Duration;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import static gov.nih.nci.cabig.ctms.testing.MoreJUnitAssertions.assertOrder;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author Rhett Sutphin
 */
public class PeriodActivitiesGridRowTest extends StudyCalendarTestCase {
    private PeriodActivitiesGridRow rowA, rowB;
    private TestKey keyA, keyB;
    private Activity a11, a12, a20;
    private Population p0, p1;
    private PlannedActivity pa0, pa1;
    private Duration duration;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        duration = new Duration(7, Duration.Unit.day);

        a11 = setId(11, createActivity("Bingo", "11", null, Fixtures.createActivityType("DISEASE_MEASURE")));
        a12 = setId(12, createActivity("aleph", "12", null, Fixtures.createActivityType("DISEASE_MEASURE")));
        a20 = setId(20, createActivity("iota", "20", null, Fixtures.createActivityType("INTERVENTION")));

        p0 = createPopulation("P", "People");
        p1 = createPopulation("Pp", "Persons");

        keyA = new TestKey(20);
        keyB = new TestKey(20);

        rowA = new PeriodActivitiesGridRow(a20, keyA, duration);
        rowB = new PeriodActivitiesGridRow(a20, keyB, duration);

        pa0 = createPlannedActivity(a20, 4);
        pa1 = createPlannedActivity(a20, 2);
    }

    public void testPlannedActivityForDayWhenNull() throws Exception {
        assertNull(rowA.getPlannedActivityForDay(4));
    }

    public void testPlannedActivityForDayWhenPresent() throws Exception {
        rowA.addPlannedActivity(pa0);
        assertSame(pa0, rowA.getPlannedActivityForDay(pa0.getDay()));
    }

    public void testIsUsedWhenUnused() throws Exception {
        assertFalse(rowA.isUsed());
    }

    public void testIsUsedWhenUsed() throws Exception {
        rowA.addPlannedActivity(pa1);
        assertTrue(rowA.isUsed());
    }

    public void testGridRowPreventsAdditionOfMultiplePlannedActivitiesForTheSameDay() throws Exception {
        pa0.setDay(3);
        pa1.setDay(3);

        rowA.addPlannedActivity(pa0);
        try {
            rowA.addPlannedActivity(pa1);
            fail("Exception not thrown");
        } catch (StudyCalendarError e) {
            assertEquals("There is already a planned activity on day 3 in this row", e.getMessage());
        }
    }

    public void testGridRowPreventsAdditionOfPlannedActivitiesForOtherActivities() throws Exception {
        Activity b = Fixtures.createActivity("B");
        pa0.setActivity(b);

        try {
            rowA.addPlannedActivity(pa0);
            fail("Exception not thrown");
        } catch (StudyCalendarError e) {
            assertEquals(String.format("This row is for %s, not %s", rowA.getActivity().toString(), b.toString()),
                e.getMessage());
        }
    }

    public void testAddingPlannedActivitiesOutOfOrder() throws Exception {
        pa0.setDay(3);
        pa1.setDay(1);
        rowA.addPlannedActivity(pa0);
        rowA.addPlannedActivity(pa1);
        assertSame(pa0, rowA.getPlannedActivityForDay(3));
        assertSame(pa1, rowA.getPlannedActivityForDay(1));
    }

    public void testPlannedActivitiesAlwaysHasTheCorrectNumberOfCells() throws Exception {
        assertEquals((int) duration.getQuantity(), rowA.getPlannedActivities().size());
        rowA.addPlannedActivity(pa0);
        assertEquals((int) duration.getQuantity(), rowA.getPlannedActivities().size());
    }

    public void testPlannedActivitiesAlwaysHasTheCorrectNumberOfCellsForNonDayUnits() throws Exception {
        duration.setUnit(Duration.Unit.quarter);
        PeriodActivitiesGridRow rowC = new PeriodActivitiesGridRow(a20, keyA, duration);
        pa0.setDay(92);
        
        assertEquals((int) duration.getQuantity(), rowC.getPlannedActivities().size());
        rowC.addPlannedActivity(pa0);
        assertEquals((int) duration.getQuantity(), rowC.getPlannedActivities().size());
    }

    public void testAddingPlannedActivityForConcealedDayIsNoop() throws Exception {
        duration.setUnit(Duration.Unit.month);
        pa0.setDay(14);
        PeriodActivitiesGridRow rowC = new PeriodActivitiesGridRow(a20, keyA, duration);
        rowC.addPlannedActivity(pa0);
        assertFalse(rowC.isUsed());
    }

    public void testAddingPlannedActivityForUnconcealedDayWorks() throws Exception {
        duration.setUnit(Duration.Unit.month);
        pa0.setDay(57);
        PeriodActivitiesGridRow rowC = new PeriodActivitiesGridRow(a20, keyA, duration);
        rowC.addPlannedActivity(pa0);
        assertTrue(rowC.isUsed());
        assertSame(pa0, rowC.getPlannedActivityForDay(57));
    }
    
    public void testGetPlannedActivityForConcealedDayIsNull() throws Exception {
        duration.setUnit(Duration.Unit.month);
        PeriodActivitiesGridRow rowC = new PeriodActivitiesGridRow(a20, keyA, duration);
        assertNull(rowC.getPlannedActivityForDay(14));
    }

    ////// COMPARABLE TESTS

    public void testOrdersByActivityTypeFirst() throws Exception {
        rowA = new PeriodActivitiesGridRow(a11, keyA, duration);
        rowB = new PeriodActivitiesGridRow(a20, keyB, duration);
        assertOrder(rowA, rowB);
    }

    public void testOrdersByActivityNameSecond() throws Exception {
        rowA = new PeriodActivitiesGridRow(a11, keyA, duration);
        rowB = new PeriodActivitiesGridRow(a12, keyB, duration);

        assertOrder(rowB, rowA);
    }

    public void testOrdersByDetailsThird() throws Exception {
        keyA.setDetails("Some details");
        keyB.setDetails("Some other details");

        assertOrder(rowA, rowB);
    }

    public void testNullDetailsComesLast() throws Exception {
        keyB.setDetails("etc");

        assertOrder(rowB, rowA);
    }

    public void testOrdersByConditionsFourth() throws Exception {
        keyA.setCondition("frob < 14");
        keyB.setCondition("frob < 10");

        assertOrder(rowB, rowA);
    }

    public void testNullConditionComesLast() throws Exception {
        keyA.setCondition("something");

        assertOrder(rowA, rowB);
    }

    public void testOrdersByLabelsFifth() throws Exception {
        addLabels(rowA, "from", "to", "bcc");
        addLabels(rowB, "from", "to", "cc", "bcc");

        assertOrder(rowB, rowA);
    }

    public void testNullLabelsComesLast() throws Exception {
        addLabels(rowB, "foom");

        assertOrder(rowB, rowA);
    }

    public void testOrdersByFirstPlannedActivityDaySixth() throws Exception {
        pa0.setDay(4);
        pa1.setDay(2);

        rowA.addPlannedActivity(pa0);
        rowB.addPlannedActivity(pa1);

        assertOrder(rowB, rowA);
    }

    public void testOrdersByFirstPlannedActivityPopulationSeventh() throws Exception {
        pa0.setDay(2); pa0.setPopulation(p0);
        pa1.setDay(2); pa1.setPopulation(p1);

        rowA.addPlannedActivity(pa0);
        rowB.addPlannedActivity(pa1);

        assertOrder(rowA, rowB);
    }

    public void testOrdersByFirstPlannedActivityPopulationPutsNullPopulationFirst() throws Exception {
        pa0.setDay(2); pa0.setPopulation(p0);
        pa1.setDay(2); pa1.setPopulation(null);

        rowA.addPlannedActivity(pa0);
        rowB.addPlannedActivity(pa1);

        assertOrder(rowB, rowA);
    }

    private void addLabels(PeriodActivitiesGridRow row, String... labels) {
        row.getLabels().addAll(Arrays.asList(labels));
    }

    private static class TestKey extends PeriodActivitiesGridRowKey {
        public TestKey(Integer activityId) {
            super(activityId, null, null, Collections.<String>emptySet(), 0);
        }

        public void setDetails(String details) {
            this.details = details;
        }

        public void setCondition(String condition) {
            this.condition = condition;
        }
    }
}
