/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.Comparator;
import java.util.Date;

/**
 * A comparator that can compare {@link java.util.Date} with its subclasses.
 *
 * @author rsutphin
 */
public class JavaDateComparator implements Comparator<Date> {
    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Compare two {@link Date} instances according to the epoch dates provided by the
     * {@link java.util.Date#getTime()} method.  If they are equivalent on that score
     * and at least one of them is a {@link Timestamp}, compare the number of nanoseconds.
     * Non-{@link Timestamp} instances are treated as though they have 0ns.
     *
     * @param d1 the first date to compare
     * @param d2 the second
     * @return an integer according to the contract for {@link Comparator#compare}
     */
    public int compare(Date d1, Date d2) {
        if (log.isDebugEnabled()) log.debug("Comparing " + d1 + " to " + d2);
        long epochDiff = d1.getTime() - d2.getTime();
        if (epochDiff > 0) { return 1; }
        else if (epochDiff < 0) { return -1; }
        else { return compareNanos(d1, d2); }
    }

    private int getEffectiveNanos(Date date) {
        if (date instanceof Timestamp) {
            return ((Timestamp) date).getNanos();
        } else {
            return 0;
        }
    }

    private int compareNanos(Date d1, Date d2) {
        return getEffectiveNanos(d1) - getEffectiveNanos(d2);
    }
}
