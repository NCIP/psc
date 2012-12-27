/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.template.period;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.Duration;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import org.apache.commons.collections.comparators.ComparableComparator;
import org.apache.commons.collections.comparators.NullComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Rhett Sutphin
*/
public class PeriodActivitiesGridRowFactory {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @SuppressWarnings({ "unchecked" })
    private static final Comparator<Population> POPULATION_COMPARATOR =
        new NullComparator(ComparableComparator.getInstance(), false);
    private static final Comparator<PlannedActivity> PA_BY_POPULATION_COMPARATOR = new Comparator<PlannedActivity>() {
        public int compare(PlannedActivity o1, PlannedActivity o2) {
            int popCompare = POPULATION_COMPARATOR.compare(o1.getPopulation(), o2.getPopulation());
            if (popCompare != 0) return popCompare;
            else return o1.compareTo(o2);
        }
    };

    private Activity activity;
    private PeriodActivitiesGridRowKey key;
    private Duration duration;
    private List<PlannedActivity> plannedActivities;

    public PeriodActivitiesGridRowFactory(Activity activity, PeriodActivitiesGridRowKey key, Duration duration) {
        this.activity = activity;
        this.key = key;
        this.duration = duration;
        plannedActivities = new ArrayList<PlannedActivity>();
    }

    public void addPlannedActivities(PlannedActivity... pas) {
        for (PlannedActivity pa : pas) {
            addPlannedActivity(pa);
        }
    }

    public void addPlannedActivity(PlannedActivity plannedActivity) {
        if (plannedActivity.getDay() < 1 || plannedActivity.getDay() > duration.getDays()) {
            log.error("{} is invalid.  It's day does not fall in [1, {}].", plannedActivity, duration.getDays());
            return;
        }

        PeriodActivitiesGridRowKey candidateKey = PeriodActivitiesGridRowKey.create(plannedActivity);
        if (getKey().equals(candidateKey)) {
            this.plannedActivities.add(plannedActivity);
        } else {
            throw new StudyCalendarError(
                "This factory is for planned activities matching %s.  The supplied planned activity has key %s.",
                getKey(), candidateKey);
        }
    }

    public List<PeriodActivitiesGridRow> createRows() {
        Collections.sort(plannedActivities, PA_BY_POPULATION_COMPARATOR);
        Map<Integer, List<PlannedActivity>> byDay = splitPlannedActivitiesByDay();
        int rowCount = determineRowCount(byDay);
        if (rowCount < 1) return Collections.emptyList();

        // The preferred row is the lowest row where each population appears first for a day.
        Map<Population, Integer> preferredRow = determinePopulationPreferredRows(byDay);
        List<PeriodActivitiesGridRow> rows = new ArrayList<PeriodActivitiesGridRow>(rowCount);
        while (rows.size() < rowCount) rows.add(new PeriodActivitiesGridRow(activity, key, duration));

        for (PlannedActivity pa : plannedActivities) {
            int preferred = preferredRow.get(pa.getPopulation());
            int row = preferredRow.get(pa.getPopulation());
            boolean placed = false;
            do {
                if (rows.get(row).getPlannedActivityForDay(pa.getDay()) == null) {
                    rows.get(row).addPlannedActivity(pa);
                    placed = true;
                }
                row = (row + 1) % rowCount;
            } while (!placed && row != preferred); // if row == preferred, we've looped all the way around
            if (!placed) {
                throw new StudyCalendarError(
                    "There were more activities for day " + pa.getDay() + " (" + byDay.get(pa.getDay()).size() +
                    ") than rows (" + rowCount + ").  This should not be possible.");
            }
        }
        return rows;
    }

    private Map<Population, Integer> determinePopulationPreferredRows(Map<Integer, List<PlannedActivity>> byDay) {
        Map<Population, Integer> preferredRow = new HashMap<Population, Integer>();
        for (List<PlannedActivity> activities : byDay.values()) {
            Set<Population> visited = new HashSet<Population>();
            for (int i = 0; i < activities.size(); i++) {
                Population pop = activities.get(i).getPopulation();
                if (visited.contains(pop)) continue;
                if (preferredRow.containsKey(pop)) {
                    preferredRow.put(pop, Math.max(i, preferredRow.get(pop)));
                } else {
                    preferredRow.put(pop, i);
                }
                visited.add(pop);
            }
        }
        return preferredRow;
    }

    private Map<Integer, List<PlannedActivity>> splitPlannedActivitiesByDay() {
        Map<Integer, List<PlannedActivity>> byDay = new HashMap<Integer, List<PlannedActivity>>();
        for (PlannedActivity plannedActivity : plannedActivities) {
            Integer day = plannedActivity.getDay();
            if (!byDay.containsKey(day)) byDay.put(day, new LinkedList<PlannedActivity>());
            byDay.get(day).add(plannedActivity);
        }
        return byDay;
    }

    private int determineRowCount(Map<Integer, List<PlannedActivity>> byDay) {
        int rowCount = 0;
        for (List<PlannedActivity> forDay : byDay.values()) {
            rowCount = Math.max(rowCount, forDay.size());
        }
        return rowCount;
    }

    ////// BEAN PROPERTIES

    public Activity getActivity() {
        return activity;
    }

    public PeriodActivitiesGridRowKey getKey() {
        return key;
    }
}
