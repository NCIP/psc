package edu.northwestern.bioinformatics.studycalendar.dataproviders.api;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;

import java.util.List;

/**
 * @author Rhett Sutphin
 */
public interface SiteProvider {
    /**
     * Retrieve and return a {@link Site} instance using the given identity.
     * @return a new instance, or null if the provider doesn't know about the identity.
     */
    Site getSite(String assignedIdentifier);

    /**
     * Perform a search of the {@link Site}s available from this provider.
     * @param partialName A substring of the desired name
     */
    List<Site> search(String partialName);

    /**
     * A unique string that will be used to distinguish instances obtained from this provider
     * from instances created locally or obtained from other providers.  Must be less than
     * 250 characters.
     */
    String providerToken();
}
