/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.dataproviders;

import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.SiteProvider;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Consolidates calls into all the {@link SiteProvider}s currently live in the system and
 * provides for refreshing of existing instances from them.
 *
 * @author Rhett Sutphin
 */
public class SiteConsumer extends AbstractConsumer<Site, SiteProvider> {
    @Override protected Class<SiteProvider> providerType() { return SiteProvider.class; }

    public Site getSite(String assignedIdentifier, String providerToken) {
        SiteProvider siteProvider = getProvider(providerToken);
        if (siteProvider != null) {
            Site provided = siteProvider.getSites(Arrays.asList(assignedIdentifier)).get(0);
            return provisionInstance(provided, siteProvider);
        } else {
            return null;
        }
    }

    public List<Site> search(String partialName) {
        return doSearch(partialName);
    }

    public Site refresh(Site site) {
        return refresh(Arrays.asList(site)).get(0);
    }

    public List<Site> refresh(List<Site> sites) {
        return new Refresh().execute(sites);
    }

    private class Refresh extends AbstractRefresh {
        @Override
        protected List<Site> loadNewVersions(SiteProvider provider, List<Site> targetSites) {
            List<Site> newVersions = null;
            List<String> idents = new ArrayList<String>(targetSites.size());
            for (Site siteToUpdate : targetSites) {
                idents.add(siteToUpdate.getAssignedIdentifier());
            }
            try {
                newVersions = provider.getSites(idents);
            } catch (RuntimeException re) {
                log.error("Error refreshing " + idents + " from provider " +
                    provider.providerToken(), re);
            }
            return newVersions;
        }

        @Override
        protected void updateInstanceInPlace(Site current, Site newVersion) {
            current.setName(newVersion.getName());
            current.setLastRefresh(newVersion.getLastRefresh());
        }
    }
}
