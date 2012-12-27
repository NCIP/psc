/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.tools;

/**
 * @author rsutphin
 */
public class MutableRange<T extends Comparable<T>> extends Range<T> {
    public MutableRange(T start, T stop) {
        super(start, stop);
    }

    public MutableRange() {
        this(null, null);
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    /**
     * Adds another range to this one.  This range will be updated to cover all the days between
     * the minimum start day and the maximum end day of the two ranges.
     * @param other
     */
    public void add(Range<T> other) {
        add(other.getStart());
        add(other.getStop());
    }

    /**
     * Expands this range to include the given point if it doesn't already.
     * @param point
     */
    public void add(T point) {
        setStart(min(getStart(), point));
        setStop(max(getStop(), point));
    }

    protected T min(T a, T b) {
        return before(a, b) ? a : b;
    }

    protected T max(T a, T b) {
        return after(a, b) ? a : b;
    }

    public void setFrom(Range<T> src) {
        setStart(src.getStart());
        setStop(src.getStop());
    }

    public Range<T> immutable() {
        return new Range<T>(getStart(), getStop());
    }

    ////// BEAN PROPERTIES

    public void setStart(T start) {
        this.start = start;
    }

    public void setStop(T stop) {
        this.stop = stop;
    }
}
