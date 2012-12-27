/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain.tools;

import edu.northwestern.bioinformatics.studycalendar.tools.MutableRange;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class AbstractDayRange extends MutableRange<Integer> implements DayRange {
    public AbstractDayRange(int startDay, int endDay) {
        super(startDay, endDay);
    }

    public int getDayCount() {
        return getEndDay() - getStartDay() + 1;
    }

    public boolean containsDay(int day) {
        return includes(day);
    }

    public List<Integer> getDays() {
        List<Integer> days = new ArrayList<Integer>(getDayCount());
        while (days.size() < getDayCount()) {
            days.add(getStartDay() + days.size());
        }
        return days;
    }

    public Integer getStartDay() {
        return getStart();
    }

    protected void setStartDay(Integer startDay) {
        setStart(startDay);
    }

    public Integer getEndDay() {
        return getStop();
    }

    protected void setEndDay(Integer endDay) {
        setStop(endDay);
    }

    public String toString() {
        return new StringBuilder(getClass().getSimpleName())
            .append("[[").append(getStartDay()).append(", ")
            .append(getEndDay()).append("] count=")
            .append(getDayCount()).append(']').toString();
    }
}
