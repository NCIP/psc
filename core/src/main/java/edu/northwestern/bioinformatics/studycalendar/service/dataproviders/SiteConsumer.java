package edu.northwestern.bioinformatics.studycalendar.service.dataproviders;

import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.SiteProvider;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

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
        protected void refreshFromProvider(SiteProvider provider, List<Site> targetSites) {
            List<String> idents = new ArrayList<String>(targetSites.size());
            for (Site siteToUpdate : targetSites) {
                idents.add(siteToUpdate.getAssignedIdentifier());
            }

            List<Site> newVersions = null;
            try {
                newVersions = provider.getSites(idents);
                if (newVersions == null) {
                    log.error(
                        "Provider {} violated protcol for #getSites by returning null.  Ignoring.",
                        provider.providerToken());
                } else if (newVersions.size() != idents.size()) {
                    log.error(
                        "Provider {} violated protocol for #getSites by returning the wrong number of results ({} when expecting {}).  Ignoring.",
                        new Object[] { provider.providerToken(), newVersions.size(), idents.size() });
                    newVersions = null;
                }
            } catch (RuntimeException re) {
                log.error("Error refreshing " + idents + " from provider " +
                    provider.providerToken(), re);
            }

            if (newVersions != null) {
                updateSites(newVersions, targetSites, provider);
            }
        }

        private void updateSites(List<Site> sources, List<Site> targets, SiteProvider provider) {
            for (ListIterator<Site> lit = targets.listIterator(); lit.hasNext();) {
                Site target = lit.next();
                Site source = sources.get(lit.previousIndex());
                if (source != null) {
                    provisionInstance(source, provider);
                    updateSite(source, target);
                }
            }
        }

        private void updateSite(Site source, Site target) {
            target.setName(source.getName());
            target.setLastRefresh(source.getLastRefresh());
        }
    }
}
