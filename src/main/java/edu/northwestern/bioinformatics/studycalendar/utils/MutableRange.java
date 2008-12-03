package edu.northwestern.bioinformatics.studycalendar.utils;

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

    public boolean isMutable() {
        return true;
    }

    /**
     * Adds another range to this one.  This range will be updated to cover all the days between
     * the minimum start day and the maximum end day of the two ranges.
     * @param other
     */
    public void add(Range<T> other) {
        setStart(min(getStart(), other.getStart()));
        setStop(max(getStop(), other.getStop()));
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

    ////// BEAN PROPERTIES

    public void setStart(T start) {
        this.start = start;
    }

    public void setStop(T stop) {
        this.stop = stop;
    }
}
