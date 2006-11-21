package edu.northwestern.bioinformatics.studycalendar.domain;

import javax.persistence.MappedSuperclass;

/**
 * @author Rhett Sutphin
 */
@MappedSuperclass
public class AbstractDomainObjectWithBigId extends AbstractDomainObject implements WithBigId {
    private String bigId;

    public String getBigId() {
        return bigId;
    }

    public void setBigId(String bigId) {
        this.bigId = bigId;
    }
}
