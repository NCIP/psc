package edu.northwestern.bioinformatics.studycalendar.utils;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Rhett Sutphin
 */
public class DayRange {
    private Integer startDay, endDay;

    public DayRange(int startDay, int endDay) {
        this.startDay = startDay;
        this.endDay = endDay;
    }

    public int getDayCount() {
        return getEndDay() - getStartDay() + 1;
    }

    public boolean containsDay(int day) {
        return getStartDay() <= day && day <= getEndDay();
    }

    public List<Integer> getDays() {
        List<Integer> days = new ArrayList<Integer>(getDayCount());
        while (days.size() < getDayCount()) {
            days.add(getStartDay() + days.size());
        }
        return days;
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

    ////// BEAN PROPERTIES

    public Integer getStartDay() {
        return startDay;
    }

    public void setStartDay(Integer startDay) {
        this.startDay = startDay;
    }

    public Integer getEndDay() {
        return endDay;
    }

    public void setEndDay(Integer endDay) {
        this.endDay = endDay;
    }

    ////// OBJECT METHODS

    public String toString() {
        return new StringBuilder(getClass().getSimpleName())
            .append("[[").append(getStartDay()).append(", ")
            .append(getEndDay()).append("] count=")
            .append(getDayCount()).append(']').toString();
    }
}
