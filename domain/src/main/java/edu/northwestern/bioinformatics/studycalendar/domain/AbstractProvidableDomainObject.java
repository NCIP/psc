/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain;

import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.sql.Timestamp;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

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

    @Transient
    public boolean isProviderExist() {
        return isNotEmpty(getProvider());
    }
}
