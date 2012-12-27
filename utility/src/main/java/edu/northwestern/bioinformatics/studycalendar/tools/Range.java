/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.tools;

import org.apache.commons.collections.comparators.NullComparator;

import java.io.Serializable;
import java.util.Comparator;

/**
 * @author rsutphin
 */
public class Range<T extends Comparable<T>> implements Comparable<Range<T>>, Serializable {
    private static final NullComparator NULLS_HIGH_COMPARATOR = new NullComparator(true);

    // Note that Ranges are by default immutable.  If a sublclass changes the value of
    // start or stop after the constructor (or allows other code to do so), it should
    // override isMutable to return true.
    protected T start;
    protected T stop;

    public Range(T start, T stop) {
        this.start = start;
        this.stop = stop;
    }

    ////// LOGIC

    public boolean isMutable() { return false; }

    ///// COMPARISON

    @SuppressWarnings({"unchecked"})
    protected Comparator<T> endPointComparator() {
        return NULLS_HIGH_COMPARATOR;
    }

    public int compareTo(Range<T> range) {
        int stopDateResult = NULLS_HIGH_COMPARATOR.compare(this.stop, range.stop);
        if (stopDateResult != 0) {
            return stopDateResult;
        } else {
            return NULLS_HIGH_COMPARATOR.compare(this.start, range.start);
        }
    }

    public boolean intersects(Range<T> other) {
        boolean otherIncludesStart = other.includes(start);
        boolean otherIncludesStop = stop == null ? other.getStop() == null : other.includes(stop);
        boolean thisSurroundsOther = this.includes(other);
        return otherIncludesStart || otherIncludesStop || thisSurroundsOther;
    }

    public boolean includes(Range<T> other) {
        boolean thisSurroundsOther = beforeOrEqual(getStart(), other.getStart());
        if (getStop() != null) {
            thisSurroundsOther &= other.getStop() != null && afterOrEqual(getStop(), other.getStop());
        }
        return thisSurroundsOther;
    }

    ////// QUERIES

    public boolean hasBound() {
        return getStop() != null || getStart()!= null;
    }

    public boolean includes(T v) {
        return !before(v, start)
                && (stop == null || beforeOrEqual(v, stop));
    }

    protected boolean before(T a, T b) {
        return a.compareTo(b) < 0;
    }

    protected boolean beforeOrEqual(T a, T b) {
        return !after(a, b);
    }

    protected boolean after(T a, T b) {
        return a.compareTo(b) > 0;
    }

    protected boolean afterOrEqual(T a, T b) {
        return !before(a, b);
    }

    ////// PROPERTIES

    public T getStart() {
        return start;
    }

    public T getStop() {
        return stop;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Range range = (Range) o;

        if (start != null ? !start.equals(range.start) : range.start != null) return false;
        if (stop != null ? !stop.equals(range.stop) : range.stop != null) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = (start != null ? start.hashCode() : 0);
        result = 29 * result + (stop != null ? stop.hashCode() : 0);
        return result;
    }

    public String toString() {
        return new StringBuffer(getClass().getSimpleName())
            .append('[').append(getStart()).append(", ").append(getStop()).append(']')
            .toString();
    }
}
