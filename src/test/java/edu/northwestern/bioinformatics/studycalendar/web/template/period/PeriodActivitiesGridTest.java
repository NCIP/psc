package edu.northwestern.bioinformatics.studycalendar.web.template.period;

import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import static edu.northwestern.bioinformatics.studycalendar.domain.ActivityType.DISEASE_MEASURE;
import edu.northwestern.bioinformatics.studycalendar.domain.DayNumber;
import edu.northwestern.bioinformatics.studycalendar.domain.Duration;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Rhett Sutphin
 */
public class PeriodActivitiesGridTest extends StudyCalendarTestCase {
    private static final int DEFAULT_DURATION_QUANTITY = 14;

    private PeriodActivitiesGrid grid;
    private Period period;
    private Integer cycleLength;
    private Set<Activity> activities;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        period = new Period();
        period.setStartDay(1);
        period.getDuration().setQuantity(DEFAULT_DURATION_QUANTITY);
        period.getDuration().setUnit(Duration.Unit.day);
        period.setRepetitions(3);
        cycleLength = 21;
        activities = new HashSet<Activity>();
    }

    ////// HEADINGS TESTS

    public void testHeadingsForOneRep() throws Exception {
        period.setRepetitions(1);
        DayNumber[][] actual = getGrid().getDayHeadings();
        assertHeadingSize(actual, 1, DEFAULT_DURATION_QUANTITY);
        // spot checks
        assertEquals("C1D1", actual[0][0].toString());
        assertEquals("C1D14", actual[13][0].toString());
    }

    public void testHeadingsForTwoReps() throws Exception {
        period.setRepetitions(2);
        DayNumber[][] actual = getGrid().getDayHeadings();
        assertHeadingSize(actual, 2, DEFAULT_DURATION_QUANTITY);
        // spot checks
        assertEquals("C1D1", actual[0][0].toString());
        assertEquals("C1D14", actual[13][0].toString());
        assertEquals("C1D21", actual[6][1].toString());
        assertEquals("C2D1", actual[7][1].toString());
    }

    public void testHeadingsForFiveReps() throws Exception {
        period.setRepetitions(5);
        DayNumber[][] actual = getGrid().getDayHeadings();
        assertHeadingSize(actual, 4, DEFAULT_DURATION_QUANTITY);
        // spot checks
        assertEquals("C1D1", actual[0][0].toString());
        assertEquals("C1D15", actual[0][1].toString());
        assertEquals("Placeholder for elided rows is missing", null, actual[0][2]);
        assertEquals("Placeholder for elided rows is missing", null, actual[7][2]);
        assertEquals("C3D15", actual[0][3].toString());
        assertEquals("C1D14", actual[13][0].toString());
        assertEquals("C4D7", actual[13][3].toString());
    }

    public void testHeadingsWithPositiveNonOneStartDay() throws Exception {
        period.setStartDay(23);
        DayNumber[][] actual = getGrid().getDayHeadings();
        assertHeadingSize(actual, 3, DEFAULT_DURATION_QUANTITY);
        // spot checks
        assertEquals("C2D2", actual[0][0].toString());
        assertEquals("C2D15", actual[13][0].toString());
    }

    private void assertHeadingSize(DayNumber[][] actual, int expectedHeadingRowCount, int expectedHeadingColCount) {
        assertEquals("Wrong number of headings", expectedHeadingColCount, actual.length);
        assertEquals("Wrong number of heading entries", expectedHeadingRowCount, actual[0].length);
    }

    public void testHeadingsWithNegativeStartDay() throws Exception {
        period.setStartDay(-10);
        DayNumber[][] actual = getGrid().getDayHeadings();
        assertHeadingSize(actual, 3, DEFAULT_DURATION_QUANTITY);
        // spot checks
        assertEquals("-10",  actual[0][0].toString());
        assertEquals("0",    actual[10][0].toString());
        assertEquals("C1D1", actual[11][0].toString());
        assertEquals("C1D3", actual[13][0].toString());
    }

    public void testHeadingsForPeriodOfWeeks() throws Exception {
        period.getDuration().setUnit(Duration.Unit.week);
        DayNumber[][] actual = getGrid().getDayHeadings();
        assertHeadingSize(actual, 3, DEFAULT_DURATION_QUANTITY);
        // spot checks
        assertEquals("C1D1", actual[0][0].toString());
        assertEquals("C1D8", actual[1][0].toString());
        assertEquals("C1D15", actual[2][0].toString());
        assertEquals("C2D1", actual[3][0].toString());
        assertEquals("C5D8", actual[13][0].toString());
        assertEquals("C5D15", actual[0][1].toString());
    }

    public void testHeadingsWithoutCycleLength() throws Exception {
        cycleLength = null;
        DayNumber[][] actual = getGrid().getDayHeadings();
        assertHeadingSize(actual, 3, DEFAULT_DURATION_QUANTITY);
        // spot checks
        assertEquals("1", actual[0][0].toString());
        assertEquals("2", actual[1][0].toString());
        assertEquals("3", actual[2][0].toString());
        assertEquals("14", actual[13][0].toString());
        assertEquals("15", actual[0][1].toString());
        assertEquals("29", actual[0][2].toString());
    }
    
    public void testGridIncludesRowsForPlannedActivities() throws Exception {
        period.addPlannedActivity(createPlannedActivity("A", 12));
        period.addPlannedActivity(createPlannedActivity("A", 10));
        period.addPlannedActivity(createPlannedActivity("F",  2));
        period.addPlannedActivity(
            createPlannedActivity(createActivity("E", DISEASE_MEASURE), 2));

        Map<ActivityType, Collection<PeriodActivitiesGridRow>> actualGroups = getGrid().getRowGroups();
        assertEquals("Wrong number of groups: " + actualGroups.keySet(), 2, actualGroups.size());
        assertEquals("Wrong number of rows for disease measure", 1,
            actualGroups.get(DISEASE_MEASURE).size());
        assertEquals("Wrong number of rows for lab test", 2,
            actualGroups.get(DEFAULT_ACTIVITY_TYPE).size());

        List<ActivityType> types = new ArrayList<ActivityType>(actualGroups.keySet());
        assertEquals("Keys are in the wrong order", DISEASE_MEASURE, types.get(0));
        assertEquals("Keys are in the wrong order", DEFAULT_ACTIVITY_TYPE, types.get(1));
    }

    public void testGridIncludesAllActivities() throws Exception {
        activities.add(createActivity("B"));
        activities.add(createActivity("A"));
        activities.add(createActivity("C"));

        Map<ActivityType,Collection<PeriodActivitiesGridRow>> actualGroups = getGrid().getRowGroups();
        assertEquals("Should be one group", 1, actualGroups.size());
        Collection<PeriodActivitiesGridRow> actualRows = actualGroups.get(DEFAULT_ACTIVITY_TYPE);
        assertNotNull("Rows not under expected type", actualRows);
        assertEquals("Wrong number of rows", 3, actualRows.size());

        Iterator<PeriodActivitiesGridRow> rowIt = actualRows.iterator();
        assertUnusedActivityGridRow("A", rowIt.next());
        assertUnusedActivityGridRow("B", rowIt.next());
        assertUnusedActivityGridRow("C", rowIt.next());
    }
    
    public void testGridIncludesBlankRowsOnlyForActivitiesNotOtherwiseMentioned() throws Exception {
        Activity a = createActivity("A");
        Activity b = createActivity("B");
        Activity c = createActivity("C");

        activities.add(b);
        activities.add(a);
        activities.add(c);

        period.addPlannedActivity(createPlannedActivity(b, 12));
        period.addPlannedActivity(createPlannedActivity(b, 10));

        Map<ActivityType,Collection<PeriodActivitiesGridRow>> actualGroups = getGrid().getRowGroups();
        assertEquals("Should be one group", 1, actualGroups.size());
        Collection<PeriodActivitiesGridRow> actualRows = actualGroups.get(DEFAULT_ACTIVITY_TYPE);
        assertNotNull("Rows not under expected type", actualRows);
        assertEquals("Wrong number of rows", 3, actualRows.size());

        Iterator<PeriodActivitiesGridRow> rowIt = actualRows.iterator();
        PeriodActivitiesGridRow firstRow = rowIt.next();
        PeriodActivitiesGridRow secondRow = rowIt.next();
        PeriodActivitiesGridRow thirdRow = rowIt.next();

        assertUnusedActivityGridRow("A", firstRow);
        assertEquals("Wrong activity second", "B", secondRow.getActivity().getName());
        assertNotNull("Should have activity at day 10 in row 1", secondRow.getPlannedActivityForDay(10));
        assertNotNull("Should have activity at day 12 in row 1", secondRow.getPlannedActivityForDay(12));
        assertUnusedActivityGridRow("C", thirdRow);
    }

    private void assertUnusedActivityGridRow(String expectedActivityName, PeriodActivitiesGridRow actualRow) {
        assertFalse("Expected row to be unused", actualRow.isUsed());
        assertEquals("Wrong activity", expectedActivityName, actualRow.getActivity().getName());
    }

    private PeriodActivitiesGrid getGrid() {
        if (grid == null) initGrid();
        return grid;
    }

    private void initGrid() {
        this.grid = new PeriodActivitiesGrid(period, cycleLength, activities);
    }
}
