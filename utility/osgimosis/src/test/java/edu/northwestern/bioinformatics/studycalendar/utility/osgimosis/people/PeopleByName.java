/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people;

import java.util.Comparator;

/**
 * @author Rhett Sutphin
 */
public class PeopleByName implements Comparator<Person> {
    public static final Comparator<Person> INSTANCE = new PeopleByName();

    public int compare(Person o1, Person o2) {
        if (o1 == null && o2 == null) {
            return 0;
        } else if (o1 == null) {
            return 1;
        } else if (o2 == null) {
            return -1;
        } else {
            return nullSafeCompareNames(o1.getName(), o2.getName());
        }
    }

    private int nullSafeCompareNames(String n1, String n2) {
        if (n1 == null && n2 == null) {
            return 0;
        } else if (n1 == null) {
            return 1;
        } else if (n2 == null) {
            return -1;
        } else {
            return n1.compareTo(n2);
        }
    }
}
