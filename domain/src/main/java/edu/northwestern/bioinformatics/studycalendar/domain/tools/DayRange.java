/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain.tools;

import java.util.List;

/**
 * @author Rhett Sutphin
 */
public interface DayRange {
    int getDayCount();

    boolean containsDay(int day);

    List<Integer> getDays();

    Integer getStartDay();

    Integer getEndDay();
}
