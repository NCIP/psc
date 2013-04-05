/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.template.period;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.domain.Duration;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;

import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class PeriodActivitiesGridRowFactoryTest extends StudyCalendarTestCase {
    private PlannedActivity pa0;
    private PeriodActivitiesGridRowFactory factory;
    private Population p1;
    private Duration duration;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        pa0 = createPlannedActivity("Something", 1, "With vim");
        pa0.getActivity().setId(123);
        duration = new Duration(21, Duration.Unit.day);
        recreateFactory();
        p1 = createPopulation("P", "People");
    }

    private void recreateFactory() {
        factory = new PeriodActivitiesGridRowFactory(pa0.getActivity(), PeriodActivitiesGridRowKey.create(pa0), duration);
    }

    public void testAllActivitiesMustHaveMatchingKeys() throws Exception {
        PlannedActivity pa1 = pa0.clone(); pa1.setActivity(setId(14, createActivity("Else")));
        try {
            factory.addPlannedActivity(pa1);
            fail("Exception not thrown");
        } catch (StudyCalendarError e) {
            assertEquals(
                String.format("This factory is for planned activities matching %s.  The supplied planned activity has key %s.",
                    factory.getKey(), PeriodActivitiesGridRowKey.create(pa1)), e.getMessage());
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

    public void testIncludesDetailsInCreatedRows() throws Exception {
        pa0.setDetails("foom");
        recreateFactory();
        factory.addPlannedActivity(pa0);
        List<PeriodActivitiesGridRow> actualRows = createRowsAndAssertCount(1);
        assertEquals("foom", actualRows.get(0).getDetails());
    }
    
    public void testIncludesConditionInCreatedRows() throws Exception {
        pa0.setCondition("etc");
        recreateFactory();
        factory.addPlannedActivity(pa0);
        List<PeriodActivitiesGridRow> actualRows = createRowsAndAssertCount(1);
        assertEquals("etc", actualRows.get(0).getCondition());
    }
    
    public void testIgnoresOutOfRangeActivities() throws Exception {
        pa0.setDay(22);
        factory.addPlannedActivity(pa0);

        createRowsAndAssertCount(0);
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
