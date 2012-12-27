/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.security.authorization;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import gov.nih.nci.cabig.ctms.suite.authorization.SiteMapping;

import java.util.List;

/**
 * Implements the pieces of {@link SiteMapping} which can be implemented without
 * depending on the core module.
 *
 * @author Rhett Sutphin
 */
public abstract class BasePscSiteMapping implements SiteMapping<Site> {
    public String getSharedIdentity(Site site) {
        return site.getAssignedIdentifier();
    }

    public abstract List<Site> getApplicationInstances(List<String> identifiers);

    public boolean isInstance(Object o) {
        return o instanceof Site;
    }
}
