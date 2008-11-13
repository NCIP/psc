package edu.northwestern.bioinformatics.studycalendar.utils;

import edu.northwestern.bioinformatics.studycalendar.domain.Activity;

import java.util.Comparator;

/**
 * @author Nataliya Shurupova
 */
public class ActivityTypeComparator implements Comparator<Activity> {
    public static final ActivityTypeComparator INSTANCE = new ActivityTypeComparator();

    public int compare(Activity activity, Activity activity1) {
        return activity.getType().compareTo(activity1.getType());
    }
}

