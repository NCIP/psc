package edu.northwestern.bioinformatics.studycalendar.domain.tools;

import java.util.Collections;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class EmptyDayRange implements DayRange {
    public static final DayRange INSTANCE = new EmptyDayRange();

    protected EmptyDayRange() { }

    public int getDayCount() {
        return 0;
    }

    public boolean containsDay(int day) {
        return false;
    }

    public List<Integer> getDays() {
        return Collections.emptyList();
    }

    public Integer getStartDay() {
        return 0;
    }

    public Integer getEndDay() {
        return 0;
    }
}
