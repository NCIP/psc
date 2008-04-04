package edu.northwestern.bioinformatics.studycalendar.dao.reporting;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author John Dzak
 */
public abstract class ReportFilters {
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
}