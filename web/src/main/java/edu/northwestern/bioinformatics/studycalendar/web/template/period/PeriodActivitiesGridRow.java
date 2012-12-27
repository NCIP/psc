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
import edu.northwestern.bioinformatics.studycalendar.tools.BeanPropertyListComparator;
import org.apache.commons.collections.comparators.ComparableComparator;
import org.apache.commons.collections.comparators.NullComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Rhett Sutphin
*/
public class PeriodActivitiesGridRow implements Comparable<PeriodActivitiesGridRow>, Cloneable {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @SuppressWarnings({ "RawUseOfParameterizedType" })
    private static BeanPropertyListComparator COMPARATOR = new BeanPropertyListComparator().
        addProperty("activity.type", ComparableComparator.getInstance()).
        addProperty("activity.name", new NullComparator(String.CASE_INSENSITIVE_ORDER)).
        addProperty("key", ComparableComparator.getInstance()).
        addProperty("firstDay", new NullComparator(ComparableComparator.getInstance(), false)).
        addProperty("firstDayPopulation", new NullComparator(ComparableComparator.getInstance(), false));

    private Duration.Unit unit;
    private Activity activity;
    private PeriodActivitiesGridRowKey key;
    private List<PlannedActivity> plannedActivities;

    public PeriodActivitiesGridRow(Activity activity, PeriodActivitiesGridRowKey key, Duration duration) {
        if (activity == null) throw new IllegalArgumentException("Activity is required");
        if (key == null) throw new IllegalArgumentException("Key is required");
        this.activity = activity;
        this.key = key;
        int cellCount = duration.getQuantity();
        plannedActivities = new ArrayList<PlannedActivity>(cellCount);
        while (plannedActivities.size() < cellCount) plannedActivities.add(null);
        this.unit = duration.getUnit();
    }

    ////// LOGIC

    public void addPlannedActivity(PlannedActivity planned) {
        if (!getActivity().equals(planned.getActivity())) {
            throw new StudyCalendarError("This row is for %s, not %s", getActivity(), planned.getActivity());
        }
        Integer index = dayToColumn(planned.getDay());
        if (index == null) {
            log.warn("Attempted to add a planned activity to the grid which would not have been visible.  Operation cancelled.");
            return;
        }
        if (plannedActivities.get(index) != null) {
            throw new StudyCalendarError("There is already a planned activity on day %d in this row", index + 1);
        }
        plannedActivities.set(index, planned);
    }

    public PlannedActivity getFirstPlannedActivity() {
        for (PlannedActivity pa : plannedActivities) {
            if (pa != null) return pa;
        }
        return null;
    }

    public Integer getFirstDay() {
        PlannedActivity pa = getFirstPlannedActivity();
        return pa == null ? null : pa.getDay();
    }

    public Population getFirstDayPopulation() {
        PlannedActivity first = getFirstPlannedActivity();
        return first == null ? null : first.getPopulation();
    }

    public boolean isUsed() {
        return getFirstPlannedActivity() != null;
    }

    public PlannedActivity getPlannedActivityForDay(int day) {
        Integer col = dayToColumn(day);
        if (col == null) {
            return null;
        } else {
            return plannedActivities.get(col);
        }
    }

    private Integer dayToColumn(int day) {
        int mod = (day - 1) % unit.inDays();
        if (mod != 0) return null;
        return (day - 1) / unit.inDays();
    }

    @SuppressWarnings({ "RawUseOfParameterizedType", "unchecked" })
    public int compareTo(PeriodActivitiesGridRow other) {
        return COMPARATOR.compare(this, other);
    }

    ////// BEAN PROPS

    public PeriodActivitiesGridRowKey getKey() {
        return key;
    }

    public Activity getActivity() {
        return activity;
    }

    public String getDetails() {
        return getKey().getDetails();
    }

    public String getCondition() {
        return getKey().getCondition();
    }

    public Collection<String> getLabels() {
        return getKey().getLabels();
    }

    public List<PlannedActivity> getPlannedActivities() {
        return plannedActivities;
    }

    public Integer getWeight() {
        return getKey().getWeight();
    }
}
