package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.nwu.bioinformatics.commons.ComparisonUtils;

import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.Version;
import java.util.Comparator;

/**
 * @author Rhett Sutphin
 */
public interface DomainObject {
    /**
     * @return the internal database identifier for this object
     */
    Integer getId();

    /**
     * Set the internal database identifier for this object.  In practice this should not be
     * called by application code -- just the persistence mechanism.
     * @param id
     */
    void setId(Integer id);

    /**
     * @return the optimistic locking version value for this object.
     */
    Integer getVersion();

    /**
     * Set the optimistic locking version value for this object.  In practice this should not be
     * called by application code -- just the persistence mechanism.
     * @param version
     */
    void setVersion(Integer version);

    class ById<T extends DomainObject> implements Comparator<T> {
        public int compare(T o1, T o2) {
            return ComparisonUtils.nullSafeCompare(o1.getId(), o2.getId());
        }
    }
}
