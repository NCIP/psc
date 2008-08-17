package edu.northwestern.bioinformatics.studycalendar.web.template.period;

import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.DayNumber;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author Rhett Sutphin
 */
public class PeriodActivitiesGrid {
    private Period period;
    private Integer cycleLength;
    private Map<ActivityType, Collection<PeriodActivitiesGridRow>> rowGroups;
    private Collection<Activity> allActivities;

    public PeriodActivitiesGrid(Period period, Integer cycleLength, Collection<Activity> activities) {
        this.period = period;
        this.cycleLength = cycleLength;
        this.allActivities = activities;

        this.rowGroups = createRowGroups();
    }

    /**
     * The headings for the days portion of the grid.  The indexes are [col][row] because that's the
     * more convenient iteration order for generating the HTML.
     * @return
     */
    public DayNumber[][] getDayHeadings() {
        int rowCount = Math.min(period.getRepetitions(), 4);
        DayNumber[][] dayHeadings = new DayNumber[period.getDuration().getQuantity()][rowCount];
        addFirstRepHeadings(dayHeadings);
        if (rowCount > 1) addSecondRepHeadings(dayHeadings);
        if (rowCount > 2) addLastRepHeadings(dayHeadings);
        return dayHeadings;
    }

    private void addFirstRepHeadings(DayNumber[][] dayHeadings) {
        addHeadingsWithOffset(dayHeadings, 0, 0);
    }

    private void addSecondRepHeadings(DayNumber[][] dayHeadings) {
        int firstRepDays = period.getDuration().getUnit().inDays() * period.getDuration().getQuantity();
        addHeadingsWithOffset(dayHeadings, 1, firstRepDays);
    }

    private void addLastRepHeadings(DayNumber[][] dayNumbers) {
        int allButLastRepDays = 
            period.getDuration().getUnit().inDays() * period.getDuration().getQuantity() * (period.getRepetitions() - 1);
        addHeadingsWithOffset(dayNumbers, dayNumbers[0].length - 1, allButLastRepDays);
    }

    private void addHeadingsWithOffset(DayNumber[][] dayHeadings, int rowIndex, int dayOffset) {
        for (int d = 0; d < dayHeadings.length; d++) {
            dayHeadings[d][rowIndex] = DayNumber.createCycleDayNumber(
                dayOffset + d * period.getDuration().getUnit().inDays() + period.getStartDay(), cycleLength);
        }
    }

    public int getColumnCount() {
        return period.getDuration().getQuantity();
    }
    
    public Map<ActivityType, Collection<PeriodActivitiesGridRow>> getRowGroups() {
        return rowGroups;
    }

    private Map<ActivityType, Collection<PeriodActivitiesGridRow>> createRowGroups() {
        Map<ActivityType, Collection<PlannedActivity>> partitioned
            = new LinkedHashMap<ActivityType, Collection<PlannedActivity>>();
        Map<ActivityType, Collection<Activity>> unusedActivities
            = new LinkedHashMap<ActivityType, Collection<Activity>>();
        // pre-fill the keys to determine their order
        for (ActivityType type : ActivityType.values()) {
            partitioned.put(type, new LinkedList<PlannedActivity>());
            unusedActivities.put(type, new HashSet<Activity>());
        }
        // partition planned
        for (PlannedActivity pa : period.getPlannedActivities()) {
            partitioned.get(pa.getActivity().getType()).add(pa);
        }
        // partition activities
        for (Activity activity : allActivities) {
            unusedActivities.get(activity.getType()).add(activity);
        }
        Map<ActivityType, Collection<PeriodActivitiesGridRow>> rowGroups
            = new LinkedHashMap<ActivityType, Collection<PeriodActivitiesGridRow>>();
        for (Map.Entry<ActivityType, Collection<PlannedActivity>> entry : partitioned.entrySet()) {
            ActivityType type = entry.getKey();
            Collection<PlannedActivity> plannedActivities = entry.getValue();
            if (plannedActivities.isEmpty() && unusedActivities.get(type).isEmpty()) continue;

            SortedSet<PeriodActivitiesGridRow> rowsForType
                = buildRowsForPlannedActivities(plannedActivities, unusedActivities.get(type));

            // add empty rows for activities from other periods/segments
            for (Activity activity : unusedActivities.get(type)) {
                rowsForType.add(new PeriodActivitiesGridRow(activity, getColumnCount()));
            }

            rowGroups.put(type, rowsForType);
        }
        return rowGroups;
    }

    private SortedSet<PeriodActivitiesGridRow> buildRowsForPlannedActivities(
        Collection<PlannedActivity> plannedActivities,
        Collection<Activity> unusedActivities
    ) {
        // collect planned activities into factories based on their eventual rows
        Map<String, PeriodActivitiesGridRowFactory> factories = new HashMap<String, PeriodActivitiesGridRowFactory>();
        for (PlannedActivity pa : plannedActivities) {
            String paKey = PeriodActivitiesGridRow.key(pa);
            if (!factories.containsKey(paKey)) {
                factories.put(paKey, new PeriodActivitiesGridRowFactory(pa.getActivity(), paKey, getColumnCount()));
            }
            factories.get(paKey).addPlannedActivity(pa);
            unusedActivities.remove(pa.getActivity());
        }

        // build rows out of factories
        SortedSet<PeriodActivitiesGridRow> rowsForType = new TreeSet<PeriodActivitiesGridRow>();
        for (PeriodActivitiesGridRowFactory factory : factories.values()) {
            rowsForType.addAll(factory.createRows());
        }
        return rowsForType;
    }

    ////// BEAN PROPERTIES

    public Period getPeriod() {
        return period;
    }
}
