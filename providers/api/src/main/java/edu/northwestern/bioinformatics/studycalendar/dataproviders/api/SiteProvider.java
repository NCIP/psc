/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dataproviders.api;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;

import java.util.List;

/**
 * @author Rhett Sutphin
 */
public interface SiteProvider extends DataProvider, SearchingProvider<Site> {
    /**
     * Retrieve and return {@link Site} instances using the given identity.
     * The instances must be returned in the same order as the input identities.
     * If the provider doesn't know about one, it must return null in its position
     * in the list.
     * <p>
     * Implementors may never return null from this method and must always return
     * a list of the same length as the input list.
     */
    List<Site> getSites(List<String> assignedIdentifier);

    /**
     * Perform a search of the {@link Site}s available from this provider.
     * @param partialName A substring of the desired name
     */
    List<Site> search(String partialName);
}
