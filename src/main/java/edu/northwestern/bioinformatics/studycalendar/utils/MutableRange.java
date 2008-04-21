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

    public void setStart(T start) {
        this.start = start;
    }

    public void setStop(T stop) {
        this.stop = stop;
    }
}
