package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.domain.tools.Differences;

/**
 * @author Rhett Sutphin
 */
public interface DeepComparable<T> {
    Differences deepEquals(T other);
}
