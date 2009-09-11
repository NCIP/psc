package edu.northwestern.bioinformatics.studycalendar.domain;

import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;

import javax.persistence.MappedSuperclass;
import java.sql.Timestamp;

/**
 * @author Rhett Sutphin
 */
@MappedSuperclass
public abstract class AbstractProvidableDomainObject extends AbstractMutableDomainObject implements Providable {
    private String provider;
    private Timestamp lastRefresh;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public Timestamp getLastRefresh() {
        return lastRefresh;
    }

    public void setLastRefresh(Timestamp lastRefresh) {
        this.lastRefresh = lastRefresh;
    }
}
