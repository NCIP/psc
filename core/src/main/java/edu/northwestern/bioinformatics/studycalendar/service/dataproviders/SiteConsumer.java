package edu.northwestern.bioinformatics.studycalendar.service.dataproviders;

import edu.northwestern.bioinformatics.studycalendar.core.osgi.OsgiLayerTools;
import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.RefreshableProvider;
import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.SiteProvider;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import gov.nih.nci.cabig.ctms.lang.NowFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * Consolidates calls into all the {@link SiteProvider}s currently live in the system and
 * provides for refreshing of existing instances from them.
 *
 * @author Rhett Sutphin
 */
public class SiteConsumer {
    private static final Class<SiteProvider> SERVICE = SiteProvider.class;
    private final Logger log = LoggerFactory.getLogger(getClass());

    private OsgiLayerTools osgiLayerTools;
    private NowFactory nowFactory;

    public Site getSite(String assignedIdentifier, String providerToken) {
        SiteProvider siteProvider = getSiteProvider(providerToken);
        if (siteProvider != null) {
            Site provided = siteProvider.getSites(Arrays.asList(assignedIdentifier)).get(0);
            return provisionInstance(provided, siteProvider);
        } else {
            return null;
        }
    }

    private Site provisionInstance(Site provided, SiteProvider siteProvider) {
        if (provided != null) {
            provided.setLastRefresh(nowFactory.getNowTimestamp());
            provided.setProvider(siteProvider.providerToken());
        }
        return provided;
    }

    public List<Site> search(String partialName) {
        List<SiteProvider> providers = osgiLayerTools.getServices(SiteProvider.class);
        List<Site> found = new ArrayList<Site>();
        for (SiteProvider provider : providers) {
            List<Site> providerMatches = provider.search(partialName);
            if (providerMatches != null) {
                for (Site match : providerMatches) {
                    found.add(provisionInstance(match, provider));
                }
            }
        }
        return found;
    }

    public Site refresh(Site site) {
        return refresh(Arrays.asList(site)).get(0);
    }

    public List<Site> refresh(List<Site> sites) {
        return new Refresh().execute(sites);
    }

    /**
     * Locates the provider with the given token and returns it.
     * <p>
     * N.b.: since providers may be registered/unregistered at runtime,
     * the results of this search <b>must not</b> be cached for more than
     * one request.
     */
    private SiteProvider getSiteProvider(String providerToken) {
        List<SiteProvider> providers = osgiLayerTools.getServices(SERVICE);
        for (SiteProvider provider : providers) {
            if (providerToken.equals(provider.providerToken())) {
                return provider;
            }
        }
        log.warn("No SiteProvider with token {} is installed", providerToken);
        return null;
    }

    /**
     * Provides a map of all the available site providers by their providerTokens.
     * <p>
     * N.b.: since providers may be registered/unregistered at runtime,
     * the results of this search <b>must not</b> be cached for more than
     * one request.
     */
    private Map<String, SiteProvider> getSiteProviders() {
        Map<String, SiteProvider> result = new HashMap<String, SiteProvider>();
        List<SiteProvider> providers = osgiLayerTools.getServices(SERVICE);
        for (SiteProvider provider : providers) {
            result.put(provider.providerToken(), provider);
        }
        return result;
    }

    /////// CONFIGURATION

    @Required
    public void setOsgiLayerTools(OsgiLayerTools tools) {
        this.osgiLayerTools = tools;
    }

    @Required
    public void setNowFactory(NowFactory nowFactory) {
        this.nowFactory = nowFactory;
    }

    private class Refresh {
        private Map<String, List<Site>> toUpdate;
        private Map<String, SiteProvider> providers;

        private Refresh() {
            toUpdate = new HashMap<String, List<Site>>();
            providers = getSiteProviders();
        }

        public List<Site> execute(List<Site> sites) {
            Timestamp now = nowFactory.getNowTimestamp();
            for (Site site : sites) {
                if (site.getProvider() == null) continue;
                if (shouldRefresh(site, now)) {
                    addToUpdate(site);
                }
            }

            for (String providerToken : toUpdate.keySet()) {
                SiteProvider provider = providers.get(providerToken);
                List<Site> targetSites = toUpdate.get(providerToken);
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
                            providerToken);
                    } else if (newVersions.size() != idents.size()) {
                        log.error(
                            "Provider {} violated protocol for #getSites by returning the wrong number of results ({} when expecting {}).  Ignoring.",
                            new Object[] { providerToken, newVersions.size(), idents.size() });
                        newVersions = null;
                    }
                } catch (RuntimeException re) {
                    log.error("Error refreshing " + idents + " from provider " + providerToken, re);
                }

                if (newVersions != null) {
                    updateSites(newVersions, targetSites, provider);
                }
            }

            return sites;
        }

        private boolean shouldRefresh(Site site, Timestamp now) {
            SiteProvider siteProvider = providers.get(site.getProvider());
            if (!(siteProvider instanceof RefreshableProvider)) return false;
            Integer interval = ((RefreshableProvider) siteProvider).getRefreshInterval();
            if (interval == null || interval < 0) return false;
            interval = 1000 * interval; // convert to ms
            long lastRefresh = site.getLastRefresh() == null ? 0 : site.getLastRefresh().getTime();
            long timeSince = now.getTime() - lastRefresh;
            log.debug("{}ms since last refresh", timeSince);
            if (timeSince < interval) {
                log.debug(" - within refresh interval {}ms; skipping", interval);
                return false;
            } else {
                log.debug(" - greater than refresh interval {}ms; refreshing", interval);
                return true;
            }
        }

        private void addToUpdate(Site site) {
            if (!toUpdate.containsKey(site.getProvider())) {
                toUpdate.put(site.getProvider(), new ArrayList<Site>());
            }
            toUpdate.get(site.getProvider()).add(site);
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
