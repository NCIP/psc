package edu.northwestern.bioinformatics.studycalendar.dao.reporting;

import edu.northwestern.bioinformatics.studycalendar.domain.AbstractControlledVocabularyObject;
import edu.northwestern.bioinformatics.studycalendar.tools.MutableRange;
import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
import org.hibernate.Filter;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author John Dzak
 */
public abstract class ReportFilters {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    private List<FilterLimit<?>> filters = new LinkedList<FilterLimit<?>>();
    
    ////// BUSINESS LOGIC

    public void apply(Session session) throws HibernateException {
        for (FilterLimit<?> filter : getFilters()) {
            if (filter.isSet()) {
                filter.apply(session);
            }
        }
    }

    public boolean isEmpty() {
        for (Limit<?> limit : getLimits()) {
            if (limit.isSet()) return false;
        }
        return true;
    }

    private Collection<FilterLimit<?>> getFilters() {
        return filters;
    }

    @SuppressWarnings("unchecked")
    protected Collection<FilterLimit<?>> getLimits() {
        return filters;
    }

    protected abstract String getHibernateFilterPrefix();
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getSimpleName()).append('[');
        if (isEmpty()) {
            sb.append("no filters set");
        } else {
            sb.append('\n');
            for (Limit<?> limit : getLimits()) {
                if (limit.isSet()) {
                    sb.append("    ");
                    limit.appendDescription(sb);
                    sb.append('\n');
                }
            }
        }
        sb.append(']');
        return sb.toString();
    }

    protected String substringParameterize(String value) {
        return new StringBuilder().append('%').append(value.toUpperCase()).append('%').toString();
    }

    protected abstract class Limit<V> {
        private V value;

        public void setValue(V value) { this.value = value; }
        public V getValue() { return this.value; }
        public boolean isSet() { return this.value != null; }

        public abstract void appendDescription(StringBuilder builder);
    }

    ////// FILTER-BASED LIMITS

    protected abstract class FilterLimit<V> extends Limit<V> {
        protected FilterLimit() {
            filters.add(this);
        }

        public abstract void apply(Session session) throws HibernateException;

        protected String qualifyFilterName(String name) {
            return getHibernateFilterPrefix() + name;
        }
    }

    protected abstract class SingleFilterFilterLimit<F, V> extends FilterLimit<V> {
        private String baseName;

        public SingleFilterFilterLimit(String baseName) {
            this.baseName = baseName;
        }

        public void apply(Session session) {
            session.enableFilter(getFilterName()).setParameter(getParameterName(), getValueForFilter());
        }

        protected String getBaseName() {
            return baseName;
        }

        protected String getParameterName() {
            return baseName;
        }

        protected String getFilterName() {
            return qualifyFilterName(baseName);
        }

        protected abstract F getValueForFilter();

        @Override
        public void appendDescription(StringBuilder builder) {
            builder.append(baseName).append('=').append(getValue());
        }
    }

    protected class SubstringFilterLimit extends SingleFilterFilterLimit<String, String> {
        public SubstringFilterLimit(String filterName) {
            super(filterName);
        }

        @Override
        protected String getValueForFilter() {
            return substringParameterize(getValue());
        }
    }

    protected class StringFilter extends SingleFilterFilterLimit<String, String> {
        public StringFilter(String filterName) {
            super(filterName);
        }

        protected String stringParametrize(String value) {
            return new StringBuilder().append(value.toUpperCase()).toString();
        }

        @Override
        protected String getValueForFilter() {
            return stringParametrize(getValue());
        }

    }

    protected class ControlledVocabularyObjectFilterLimit<V extends AbstractControlledVocabularyObject> extends SingleFilterFilterLimit<Integer, V> {
        public ControlledVocabularyObjectFilterLimit(String filterName) {
            super(filterName);
        }

        @Override
        protected String getParameterName() {
            return getBaseName() + "_id";
        }

        @Override
        protected Integer getValueForFilter() {
            return getValue().getId();
        }
    }

    protected class DomainObjectFilterLimit<V extends AbstractMutableDomainObject> extends SingleFilterFilterLimit<Integer, V> {
        public DomainObjectFilterLimit(String filterName) {
            super(filterName);
        }

        @Override
        protected String getParameterName() {
            return getBaseName() + "_id";
        }

        @Override
        protected Integer getValueForFilter() {
            return getValue().getId();
        }
    }

    protected class RangeFilterLimit<B extends Comparable<B>> extends FilterLimit<MutableRange<B>> {
        private String baseName;

        public RangeFilterLimit(String baseName) {
            setValue(new MutableRange<B>());
            this.baseName = baseName;
        }

        @Override
        public boolean isSet() {
            return getValue() != null
                    && getValue().hasBound();
        }

        @Override
        public void apply(Session session) throws HibernateException {
            enable(session, getValue().getStart(), "start");
            enable(session, getValue().getStop(), "stop");
        }

        protected Filter enable(Session session, B bound, String boundName) {
            if (bound != null) {
                return session.enableFilter(qualifyFilterName(baseName) + '_' + boundName)
                        .setParameter(boundName, bound);
            } else {
                return null;
            }
        }

        @Override
        public void appendDescription(StringBuilder builder) {
            builder.append(baseName).append(" in [")
                    .append(getValue().getStart()).append(", ").append(getValue().getStop()).append(']');
        }
    }
}