/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain.tools;

import edu.northwestern.bioinformatics.studycalendar.tools.JavaDateComparator;
import edu.northwestern.bioinformatics.studycalendar.tools.Range;
import org.apache.commons.collections.comparators.NullComparator;

import java.util.Comparator;
import java.util.Date;

/**
 * Provides {@link Date}-specific logic for {@Range}s.
 *
 * @author Rhett Sutphin
 */
public class DateRange extends Range<Date> {
    private static final NullComparator NULLS_HIGH_COMPARATOR = new NullComparator(new JavaDateComparator(), true);

    public DateRange(Date start, Date stop) {
        super(start, stop);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    protected Comparator<Date> endPointComparator() {
        return NULLS_HIGH_COMPARATOR;
    }

    public int getDayCount() {
        long msDiff = getStop().getTime() - getStart().getTime();
        return (int) (msDiff / 1000 / 60 / 60 / 24 + 1);
    }
}
