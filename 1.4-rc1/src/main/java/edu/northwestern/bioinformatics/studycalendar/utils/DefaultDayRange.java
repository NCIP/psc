package edu.northwestern.bioinformatics.studycalendar.utils;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Rhett Sutphin
 */
public class DefaultDayRange extends AbstractDayRange {
    public DefaultDayRange(int startDay, int endDay) {
        super(endDay, startDay);
    }

    /**
     * Adds another range to this one.  This range will be updated to cover all the days between
     * the minimum start day and the maximum end day of the two ranges.
     * @param other
     */
    public void add(DayRange other) {
        setStartDay(Math.min(getStartDay(), other.getStartDay()));
        setEndDay(Math.max(getEndDay(), other.getEndDay()));
    }
}
