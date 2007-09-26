package edu.northwestern.bioinformatics.studycalendar.utils;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Rhett Sutphin
 */
public class AbstractDayRange implements DayRange {
    private Integer startDay;
    private Integer endDay;

    public AbstractDayRange(int endDay, int startDay) {
        this.endDay = endDay;
        this.startDay = startDay;
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

    public Integer getStartDay() {
        return startDay;
    }

    protected void setStartDay(Integer startDay) {
        this.startDay = startDay;
    }

    public Integer getEndDay() {
        return endDay;
    }

    protected void setEndDay(Integer endDay) {
        this.endDay = endDay;
    }

    public String toString() {
        return new StringBuilder(getClass().getSimpleName())
            .append("[[").append(getStartDay()).append(", ")
            .append(getEndDay()).append("] count=")
            .append(getDayCount()).append(']').toString();
    }
}
