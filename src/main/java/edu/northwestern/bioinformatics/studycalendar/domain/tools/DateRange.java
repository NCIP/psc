package edu.northwestern.bioinformatics.studycalendar.domain.tools;

import java.util.Date;

/**
 * Provides {@link Date}-specific logic for {@Range}s.
 *
 * @author Rhett Sutphin
 */
public class DateRange extends Range<Date> {
    public DateRange(Date start, Date stop) {
        super(start, stop);
    }

    public int getDayCount() {
        long msDiff = getStop().getTime() - getStart().getTime();
        return (int) (msDiff / 1000 / 60 / 60 / 24 + 1);
    }
}
