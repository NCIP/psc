package edu.northwestern.bioinformatics.studycalendar.web.template.period;

import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.Label;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.utils.ExpandingList;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import org.apache.commons.collections.comparators.ComparableComparator;
import org.apache.commons.collections.comparators.NullComparator;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * @author Rhett Sutphin
*/
public class PeriodActivitiesGridRow implements Comparable<PeriodActivitiesGridRow>, Cloneable {
    @SuppressWarnings({ "RawUseOfParameterizedType" })
    private static Map<String, Comparator> COMPARATORS = new LinkedHashMap<String, Comparator>();
    static {
        COMPARATORS.put("activity.type", ComparableComparator.getInstance());
        COMPARATORS.put("activity.name", new NullComparator(String.CASE_INSENSITIVE_ORDER));
        COMPARATORS.put("details", new NullComparator(String.CASE_INSENSITIVE_ORDER));
        COMPARATORS.put("condition", new NullComparator(String.CASE_INSENSITIVE_ORDER));
        COMPARATORS.put("#labels", new NullComparator(String.CASE_INSENSITIVE_ORDER));
        COMPARATORS.put("firstDay", new NullComparator(ComparableComparator.getInstance(), false));
        COMPARATORS.put("firstDayPopulation", new NullComparator(ComparableComparator.getInstance(), false));
    }

    private Activity activity;
    private String details;
    private String condition;
    private Collection<Label> labels;
    private List<PlannedActivity> plannedActivities;

    public PeriodActivitiesGridRow(Activity activity) {
        if (activity == null) throw new IllegalArgumentException("Activity is required");
        this.activity = activity;
        labels = new TreeSet<Label>();
        plannedActivities = new ExpandingList<PlannedActivity>();
    }

    ////// LOGIC

    public static String key(PlannedActivity plannedActivity) {
        if (plannedActivity.getActivity().getId() == null) {
            throw new StudyCalendarError("Cannot build a useful key if the activity has no ID");
        }
        return new StringBuilder().
            append(plannedActivity.getActivity().getId()).
            append(plannedActivity.getDetails()).
            append(plannedActivity.getCondition()).
            append(comparableLabels(plannedActivity.getLabels())).
            toString();
    }

    /**
     * This must return exactly the same key as {@link #key(PlannedActivity)}, given the
     * same components.
     * @return
     */
    public String key() {
        return new StringBuilder().
            append(getActivity().getId()).
            append(getDetails()).
            append(getCondition()).
            append(comparableLabels(getLabels())).
            toString();
    }

    public void addPlannedActivity(PlannedActivity planned) {
        if (!getActivity().equals(planned.getActivity())) {
            throw new StudyCalendarError("This row is for %s, not %s", getActivity(), planned.getActivity());
        }
        int index = planned.getDay() - 1;
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
        return plannedActivities.get(day - 1);
    }

    @SuppressWarnings({ "RawUseOfParameterizedType", "unchecked" })
    public int compareTo(PeriodActivitiesGridRow other) {
        BeanWrapper first = new BeanWrapperImpl(this);
        BeanWrapper second = new BeanWrapperImpl(other);

        for (Map.Entry<String, Comparator> entry : COMPARATORS.entrySet()) {
            Object value1 = comparableValue(entry.getKey(), first);
            Object value2 = comparableValue(entry.getKey(), second);

            int result = entry.getValue().compare(value1, value2);
            if (result != 0) return result;
        }

        return 0;
    }

    private Object comparableValue(String key, BeanWrapper wrapped) {
        if ("#labels".equals(key)) {
            return comparableLabels(
                ((PeriodActivitiesGridRow) wrapped.getWrappedInstance()).getLabels());
        } else {
            return wrapped.getPropertyValue(key);
        }
    }

    private static String comparableLabels(Collection<Label> labels) {
        if (labels.size() == 0) {
            return null;
        } else {
            List<String> labelNames = new ArrayList<String>(labels.size());
            for (Label label : labels) {
                labelNames.add(label.getName().toLowerCase());
            }
            Collections.sort(labelNames);
            return StringUtils.join(labelNames.iterator(), "|");
        }
    }

    ////// BEAN PROPS

    public Activity getActivity() {
        return activity;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public Collection<Label> getLabels() {
        return labels;
    }

    public List<PlannedActivity> getPlannedActivities() {
        return plannedActivities;
    }

    public static PeriodActivitiesGridRow create(PlannedActivity plannedActivity) {
        PeriodActivitiesGridRow row = new PeriodActivitiesGridRow(plannedActivity.getActivity());
        row.setCondition(plannedActivity.getCondition());
        row.setDetails(plannedActivity.getDetails());
        for (Label label : plannedActivity.getLabels()) {
            row.getLabels().add(label);
        }
        return row;
    }

}
