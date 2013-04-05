/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain.tools;

import edu.northwestern.bioinformatics.studycalendar.tools.Range;

/**
 * @author Rhett Sutphin
 */
public class DefaultDayRange extends AbstractDayRange {
    public DefaultDayRange(int startDay, int endDay) {
        super(startDay, endDay);
    }

    /**
     * Adds another range to this one.  This range will be updated to cover all the days between
     * the minimum start day and the maximum end day of the two ranges.
     * @param other
     */
    public void addDayRange(DayRange other) {
        add(new Range<Integer>(other.getStartDay(), other.getEndDay()));
    }
}
