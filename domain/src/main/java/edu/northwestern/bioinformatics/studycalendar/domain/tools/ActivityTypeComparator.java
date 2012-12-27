/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain.tools;

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

