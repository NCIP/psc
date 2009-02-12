package edu.northwestern.bioinformatics.studycalendar.utils;

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
