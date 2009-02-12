package edu.northwestern.bioinformatics.studycalendar.domain;

import java.util.Comparator;

/**
 * @author Rhett Sutphin
 */
// Hibernate needs to refer to this as a class (not an instance), so:
public class LabelComparator implements Comparator<String> {
    public static final LabelComparator INSTANCE = new LabelComparator();

    public int compare(String o1, String o2) {
        return String.CASE_INSENSITIVE_ORDER.compare(o1, o2);
    }
}
