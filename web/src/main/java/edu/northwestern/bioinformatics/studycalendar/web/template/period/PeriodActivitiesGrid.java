/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.template.period;

import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.DayNumber;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Required;

/**
 * @author Rhett Sutphin
 */
public class PeriodActivitiesGrid {
    private Period period;
    private Integer cycleLength;
    private Collection<Activity> allActivities;
    private ActivityTypeDao activityTypeDao;

    private Map<ActivityType, Collection<PeriodActivitiesGridRow>> rowGroups;
    private int[] columnDayNumbers;

    public PeriodActivitiesGrid(Period period, Integer cycleLength, Collection<Activity> activities, ActivityTypeDao activityTypeDao) {
        this.period = period;
        this.cycleLength = cycleLength;
        this.allActivities = activities;
        this.activityTypeDao = activityTypeDao;
        setActivityTypeDao(activityTypeDao);
        this.rowGroups = createRowGroups();
        this.columnDayNumbers = createColumnDayNumbers();
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

    /**
     * The day numbers that each column in the grid corresponds to.  This is the day value
     * that should be associated with a planned activity added to each column.
     * @return
     */
    public int[] getColumnDayNumbers() {
        return columnDayNumbers;
    }

    private int[] createColumnDayNumbers() {
        int[] dayNumbers = new int[getColumnCount()];
        for (int i = 0; i < dayNumbers.length; i++) {
            dayNumbers[i] = 1 + i * period.getDuration().getUnit().inDays();
        }
        return dayNumbers;
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
        for (ActivityType type : activityTypeDao.getAll()) {
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
        Map<ActivityType, Collection<PeriodActivitiesGridRow>> created
            = new LinkedHashMap<ActivityType, Collection<PeriodActivitiesGridRow>>();
        for (Map.Entry<ActivityType, Collection<PlannedActivity>> entry : partitioned.entrySet()) {
            ActivityType type = entry.getKey();
            Collection<PlannedActivity> plannedActivities = entry.getValue();
            if (plannedActivities.isEmpty() && unusedActivities.get(type).isEmpty()) continue;

            SortedSet<PeriodActivitiesGridRow> rowsForType
                = buildRowsForPlannedActivities(plannedActivities, unusedActivities.get(type));

            // add empty rows for activities from other periods/segments
            for (Activity activity : unusedActivities.get(type)) {
                rowsForType.add(new PeriodActivitiesGridRow(activity, PeriodActivitiesGridRowKey.create(activity), getPeriod().getDuration()));
            }

            created.put(type, rowsForType);
        }
        return created;
    }

    private SortedSet<PeriodActivitiesGridRow> buildRowsForPlannedActivities(
        Collection<PlannedActivity> plannedActivities,
        Collection<Activity> unusedActivities
    ) {
        // collect planned activities into factories based on their eventual rows
        Map<PeriodActivitiesGridRowKey, PeriodActivitiesGridRowFactory> factories = new HashMap<PeriodActivitiesGridRowKey, PeriodActivitiesGridRowFactory>();
        for (PlannedActivity pa : plannedActivities) {
            PeriodActivitiesGridRowKey paKey = PeriodActivitiesGridRowKey.create(pa);
            if (!factories.containsKey(paKey)) {
                factories.put(paKey,
                    new PeriodActivitiesGridRowFactory(pa.getActivity(), paKey, getPeriod().getDuration()));
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

    public Integer getCycleLength() {
        return cycleLength;
    }

    @Required
    public void setActivityTypeDao(ActivityTypeDao activityTypeDao) {
        this.activityTypeDao = activityTypeDao;
    }
}
