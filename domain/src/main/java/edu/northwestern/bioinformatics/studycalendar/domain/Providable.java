/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain;

import java.sql.Timestamp;

/**
 * An interface for domain objects which may be sourced from outside systems
 * via PSC's pluggable provider scheme.
 * <p>
 * The methods defined here <strong>do not</strong> form part of PSC's domain analysis model.
 * <p>
 * It is not the responsibility of the provider to set these values -- the code in PSC which
 * uses the provider will take care of it.
 *
 * @author Rhett Sutphin
 */
public interface Providable {
    /**
     * The name token for the provider was the source for this instance.  If null, this instance
     * was locally created.
     */
    public String getProvider();

    public void setProvider(String providerToken);

    /**
     * The time when this instance was last syncd with the provider, if any.
     */
    public Timestamp getLastRefresh();

    public void setLastRefresh(Timestamp timestamp);
}
