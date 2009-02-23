package edu.northwestern.bioinformatics.studycalendar.tools;

import edu.nwu.bioinformatics.commons.DelegatingList;

import java.util.List;
import java.util.LinkedList;
import java.util.Collection;

/**
 * @author Rhett Sutphin
 */
public class ExpandingList<E> extends DelegatingList<E> {
    private List<E> delegate;
    private Filler<E> filler;

    public ExpandingList() {
        this(null, null);
    }

    public ExpandingList(Filler<E> filler) {
        this(filler, null);
    }

    public ExpandingList(List<E> delegate) {
        this(null, delegate);
    }

    public ExpandingList(Filler<E> filler, List<E> delegate) {
        this.delegate = delegate == null ? new LinkedList<E>() : delegate;
        this.filler = filler == null ? new NullFiller<E>() : filler;
    }

    protected List<E> getDelegateList() {
        return delegate;
    }

    ////// EXPANDING BEHAVIOR

    public E get(int i) {
        fillTo(i);
        return getDelegateList().get(i);
    }

    public E set(int i, E e) {
        fillTo(i);
        return getDelegateList().set(i, e);
    }

    public boolean addAll(int i, Collection<? extends E> es) {
        fillTo(i - 1);
        return super.addAll(i,es);
    }

    public void add(int i, E e) {
        fillTo(i - 1);
        super.add(i, e);
    }

    private void fillTo(int index) {
        while (getDelegateList().size() - 1 < index) {
            getDelegateList().add(filler.createNew(getDelegateList().size()));
        }
    }

    public static interface Filler<L> {
        L createNew(int index);
    }

    public static class StaticFiller<L> implements Filler<L> {
        private L fill;

        public StaticFiller(L fill) {
            this.fill = fill;
        }

        public L createNew(int index) {
            return fill;
        }
    }

    public static final class NullFiller<L> extends StaticFiller<L> {
        public NullFiller() { super(null); }
    }
}
