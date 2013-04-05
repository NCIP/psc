/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.tools;

import java.util.Comparator;

/**
 * @author Rhett Sutphin
 */
public class StringTools {
    public static Comparator<String> CASE_INSENSITIVE_NULL_SAFE_ORDER = new Comparator<String>() {
        public int compare(String o1, String o2) {
            if (o1 == null) {
                return o2 == null ? 0 : -1;
            } else if (o2 == null) {
                return 1;
            } else {
                if (o1.equalsIgnoreCase(o2)) {
                    return o1.compareTo(o2);
                } else {
                    return o1.compareToIgnoreCase(o2);
                }
            }
        }
    };

    public static String humanizeClassName(String className) {
        if (className == null) return null;
        return className.replaceAll("([A-Z])", " $1").trim().toLowerCase();
    }

    public static String valueOf(Object obj) {
        return obj != null ? String.valueOf(obj) : null;
    }

    // static class
    private StringTools() {
    }
}
