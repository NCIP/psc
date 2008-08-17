package edu.northwestern.bioinformatics.studycalendar.web.template.period;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Label;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

/**
 * @author Rhett Sutphin
 */
public class PeriodActivitiesGridRowTest extends StudyCalendarTestCase {
    private PeriodActivitiesGridRow rowA, rowB;
    private Activity a11, a12, a20;
    private Population p0, p1;
    private PlannedActivity pa0, pa1;
    private static final int CELL_COUNT = 7;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        a11 = setId(11, createActivity("Bingo", "11", null, ActivityType.DISEASE_MEASURE));
        a12 = setId(12, createActivity("aleph", "12", null, ActivityType.DISEASE_MEASURE));
        a20 = setId(20, createActivity("iota", "20", null, ActivityType.INTERVENTION));

        p0 = createPopulation("P", "People");
        p1 = createPopulation("Pp", "Persons");

        rowA = new PeriodActivitiesGridRow(a20, CELL_COUNT);
        rowB = new PeriodActivitiesGridRow(a20, CELL_COUNT);

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
        assertEquals(CELL_COUNT, rowA.getPlannedActivities().size());
        rowA.addPlannedActivity(pa0);
        assertEquals(CELL_COUNT, rowA.getPlannedActivities().size());
    }

    ////// COMPARABLE TESTS

    public void testOrdersByActivityTypeFirst() throws Exception {
        rowA = new PeriodActivitiesGridRow(a11, CELL_COUNT);
        rowB = new PeriodActivitiesGridRow(a20, CELL_COUNT);

        assertOrder(rowA, rowB);
    }

    public void testOrdersByActivityNameSecond() throws Exception {
        rowA = new PeriodActivitiesGridRow(a11, CELL_COUNT);
        rowB = new PeriodActivitiesGridRow(a12, CELL_COUNT);

        assertOrder(rowB, rowA);
    }

    public void testOrdersByDetailsThird() throws Exception {
        rowA.setDetails("Some details");
        rowB.setDetails("Some other details");

        assertOrder(rowA, rowB);
    }

    public void testNullDetailsComesLast() throws Exception {
        rowB.setDetails("etc");

        assertOrder(rowB, rowA);
    }

    public void testOrdersByConditionsFourth() throws Exception {
        rowA.setCondition("frob < 14");
        rowB.setCondition("frob < 10");

        assertOrder(rowB, rowA);
    }

    public void testNullConditionComesLast() throws Exception {
        rowA.setCondition("something");

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

    private void assertOrder(PeriodActivitiesGridRow first, PeriodActivitiesGridRow second) {
        assertNegative(first.compareTo(second));
        assertPositive(second.compareTo(first));
    }

    private void addLabels(PeriodActivitiesGridRow row, String... labels) {
        for (String label : labels) {
            row.getLabels().add(createNamedInstance(label, Label.class));
        }
    }
}
