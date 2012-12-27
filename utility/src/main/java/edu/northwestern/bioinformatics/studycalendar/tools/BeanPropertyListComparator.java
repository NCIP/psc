/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.tools;

import org.apache.commons.collections.comparators.NullComparator;
import org.apache.commons.collections.comparators.ComparableComparator;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class BeanPropertyListComparator<T> implements Comparator<T> {
    private static final Comparator<?> DEFAULT_COMPARATOR
        = new NullComparator(ComparableComparator.getInstance(), false);

    private Map<String, Comparator> comparators = new LinkedHashMap<String, Comparator>();

    public BeanPropertyListComparator<T> addProperty(String property, Comparator<?> comparator) {
        comparators.put(property, comparator);
        return this;
    }

    public BeanPropertyListComparator<T> addProperty(String property) {
        return addProperty(property, DEFAULT_COMPARATOR);
    }

    public int compare(T o1, T o2) {
        BeanWrapper first = new BeanWrapperImpl(o1);
        BeanWrapper second = new BeanWrapperImpl(o2);

        for (Map.Entry<String, Comparator> entry : comparators.entrySet()) {
            Object value1 = first.getPropertyValue(entry.getKey());
            Object value2 = second.getPropertyValue(entry.getKey());

            int result = entry.getValue().compare(value1, value2);
            if (result != 0) return result;
        }

        return 0;
    }
}
