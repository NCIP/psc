package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.nwu.bioinformatics.commons.ComparisonUtils;

import javax.persistence.Id;
import javax.persistence.Version;
import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import java.util.Comparator;

/**
 * @author Rhett Sutphin
 */
@MappedSuperclass
public abstract class AbstractDomainObject {
    private Integer id;
    private Integer version;

    @Id @GeneratedValue(generator = "id-generator")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Version
    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public static class ById<T extends AbstractDomainObject> implements Comparator<T> {
        public int compare(T o1, T o2) {
            return ComparisonUtils.nullSafeCompare(o1.getId(), o2.getId());
        }
    }
}
