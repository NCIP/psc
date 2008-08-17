package edu.northwestern.bioinformatics.studycalendar.web.template.period;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;

import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class PeriodActivitiesGridRowFactoryTest extends StudyCalendarTestCase {
    private PlannedActivity pa0;
    private PeriodActivitiesGridRowFactory factory;
    private Population p1;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        pa0 = createPlannedActivity("Something", 1, "With vim");
        pa0.getActivity().setId(123);
        factory = new PeriodActivitiesGridRowFactory(pa0.getActivity(), PeriodActivitiesGridRow.key(pa0), 21);
        p1 = createPopulation("P", "People");
    }

    public void testAllActivitiesMustHaveMatchingKeys() throws Exception {
        PlannedActivity pa1 = pa0.clone(); pa1.setActivity(setId(14, createActivity("Else")));
        try {
            factory.addPlannedActivity(pa1);
            fail("Exception not thrown");
        } catch (StudyCalendarError e) {
            assertEquals(
                String.format("This factory is for planned activities matching %s.  The supplied planned activity has key %s.",
                    factory.getKey(), PeriodActivitiesGridRow.key(pa1)), e.getMessage());
        }
    }

    public void testCreatesOneRowForOneActivity() throws Exception {
        factory.addPlannedActivity(pa0);
        List<PeriodActivitiesGridRow> actualRows = createRowsAndAssertCount(1);
        assertSame(pa0, actualRows.get(0).getPlannedActivityForDay(1));
    }

    public void testCreatesOneRowForMultipleActivitiesOnDifferentDays() throws Exception {
        PlannedActivity pa1 = pa0.clone(); pa1.setDay(2);
        PlannedActivity pa2 = pa0.clone(); pa2.setDay(4);
        factory.addPlannedActivities(pa0, pa1, pa2);

        List<PeriodActivitiesGridRow> actualRows = createRowsAndAssertCount(1);
        assertSame(pa0, actualRows.get(0).getPlannedActivityForDay(1));
        assertSame(pa1, actualRows.get(0).getPlannedActivityForDay(2));
        assertSame(pa2, actualRows.get(0).getPlannedActivityForDay(4));
    }

    public void testCreatesOneRowForMultipleActivitiesOnDifferentDaysWithDifferentPopulations() throws Exception {
        PlannedActivity pa1 = pa0.clone(); pa1.setDay(2); pa1.setPopulation(p1);
        PlannedActivity pa2 = pa0.clone(); pa2.setDay(4);
        factory.addPlannedActivities(pa0, pa1, pa2);

        List<PeriodActivitiesGridRow> actualRows = createRowsAndAssertCount(1);
        assertSame(pa0, actualRows.get(0).getPlannedActivityForDay(1));
        assertSame(pa1, actualRows.get(0).getPlannedActivityForDay(2));
        assertSame(pa2, actualRows.get(0).getPlannedActivityForDay(4));
    }

    public void testCreatesTwoRowsForTwoActivitiesOnSameDay() throws Exception {
        PlannedActivity pa1 = pa0.clone();
        factory.addPlannedActivities(pa0, pa1);

        List<PeriodActivitiesGridRow> actualRows = createRowsAndAssertCount(2);
        assertSame(pa0, actualRows.get(0).getPlannedActivityForDay(1));
        assertSame(pa1, actualRows.get(1).getPlannedActivityForDay(1));
    }

    public void testKeepsPopulationsTogetherWhenSplitIntoMultipleRows() throws Exception {
        PlannedActivity pa1 = pa0.clone(); pa1.setPopulation(p1);
        PlannedActivity pa2 = pa0.clone(); pa2.setPopulation(p1); pa2.setDay(4);
        factory.addPlannedActivities(pa0, pa1, pa2);

        List<PeriodActivitiesGridRow> actualRows = createRowsAndAssertCount(2);
        assertSame(pa0, actualRows.get(0).getPlannedActivityForDay(1));
        assertSame(pa1, actualRows.get(1).getPlannedActivityForDay(1));
        assertSame(pa2, actualRows.get(1).getPlannedActivityForDay(4));
    }

    public void testKeepsNoPopulationTogetherWhenSplitIntoMultipleRows() throws Exception {
        PlannedActivity pa1 = pa0.clone(); pa1.setPopulation(p1);
        PlannedActivity pa2 = pa0.clone(); pa2.setPopulation(null); pa2.setDay(4);
        factory.addPlannedActivities(pa0, pa1, pa2);

        List<PeriodActivitiesGridRow> actualRows = createRowsAndAssertCount(2);
        assertSame(pa0, actualRows.get(0).getPlannedActivityForDay(1));
        assertSame(pa1, actualRows.get(1).getPlannedActivityForDay(1));
        assertSame(pa2, actualRows.get(0).getPlannedActivityForDay(4));
    }

    public void testDoesNotUseAThirdRowWhenThereAreTwoOfTheSamePopulationOnTheSameDay() throws Exception {
        PlannedActivity pa1 = pa0.clone(); pa1.setPopulation(p1);
        PlannedActivity pa2 = pa0.clone(); pa2.setPopulation(p1); pa2.setDay(4);
        PlannedActivity pa3 = pa0.clone(); pa3.setPopulation(p1); pa3.setDay(4);
        factory.addPlannedActivities(pa0, pa1, pa2, pa3);

        List<PeriodActivitiesGridRow> actualRows = createRowsAndAssertCount(2);
        assertSame(pa0, actualRows.get(0).getPlannedActivityForDay(1));
        assertSame(pa1, actualRows.get(1).getPlannedActivityForDay(1));
        assertSame(pa2, actualRows.get(1).getPlannedActivityForDay(4));
        assertSame(pa3, actualRows.get(0).getPlannedActivityForDay(4));
    }

    private List<PeriodActivitiesGridRow> createRowsAndAssertCount(int expectedSize) {
        List<PeriodActivitiesGridRow> actualRows = factory.createRows();
        assertEquals("Wrong number of rows", expectedSize, actualRows.size());
        for (int i = 0; i < actualRows.size(); i++) {
            assertTrue("Row " + i + " has no PAs", actualRows.get(i).isUsed());
        }
        return actualRows;
    }
}
