package edu.northwestern.bioinformatics.studycalendar.domain;

import javax.persistence.Id;
import javax.persistence.Version;
import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;

/**
 * @author Rhett Sutphin
 */
@MappedSuperclass
public abstract class AbstractDomainObject {
    private Integer id;
    private Integer version;

    @Id @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ")
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
}
