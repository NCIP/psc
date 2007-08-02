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
    Integer getId();
    void setId(Integer id);

    Integer getVersion();
    void setVersion(Integer version);

    class ById<T extends DomainObject> implements Comparator<T> {
        public int compare(T o1, T o2) {
            return ComparisonUtils.nullSafeCompare(o1.getId(), o2.getId());
        }
    }
}
