package edu.northwestern.bioinformatics.studycalendar.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Rhett Sutphin
 */
public class AbstractDayRange implements DayRange {
    private Integer startDay;
    private Integer endDay;
    private final Logger log = LoggerFactory.getLogger(getClass());
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

    public List<Integer> getWeeks() {
        return getListOfDays(7);
    }

    public List<Integer> getMonths() {
        return getListOfDays(28);
    }    

    public List<Integer> getFortnights() {
        return getListOfDays(14);
    }

    public List<Integer> getQuarters() {
        return getListOfDays(91);
    }

    private List<Integer> getListOfDays(int interval) {
        List<Integer> days = new ArrayList<Integer>();
        int startDay = getStartDay();
        days.add(startDay);
        while (startDay < getEndDay()){
            days.add(startDay + interval);
            startDay = startDay + interval;
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
