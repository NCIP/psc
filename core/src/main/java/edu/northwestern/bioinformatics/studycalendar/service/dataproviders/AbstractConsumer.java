/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.dataproviders;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.core.osgi.OsgiLayerTools;
import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.DataProvider;
import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.RefreshableProvider;
import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.SearchingProvider;
import edu.northwestern.bioinformatics.studycalendar.domain.Providable;
import gov.nih.nci.cabig.ctms.lang.NowFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public abstract class AbstractConsumer<D extends Providable, P extends DataProvider> {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private OsgiLayerTools osgiLayerTools;
    private NowFactory nowFactory;

    protected abstract Class<P> providerType();

    protected <T extends Providable> T provisionInstance(T provided, P provider) {
        if (provided != null) {
            provided.setLastRefresh(nowFactory.getNowTimestamp());
            provided.setProvider(provider.providerToken());
        }
        return provided;
    }

    /**
     * Locates the provider with the given token and returns it.
     * <p>
     * N.b.: since providers may be registered/unregistered at runtime,
     * the results of this search <b>must not</b> be cached for more than
     * one request.
     */
    protected P getProvider(String providerToken) {
        List<P> providers = osgiLayerTools.getServices(providerType());
        for (P provider : providers) {
            if (providerToken.equals(provider.providerToken())) {
                return provider;
            }
        }
        log.warn("No {} with token {} is installed", providerType().getSimpleName(), providerToken);
        return null;
    }

    /**
     * Provides a map of all the available site providers by their providerTokens.
     * <p>
     * N.b.: since providers may be registered/unregistered at runtime,
     * the results of this search <b>must not</b> be cached for more than
     * one request.
     */
    protected Map<String, P> getProviders() {
        Map<String, P> result = new HashMap<String, P>();
        List<P> providers = getOsgiLayerTools().getServices(providerType());
        for (P provider : providers) {
            result.put(provider.providerToken(), provider);
        }
        return result;
    }

    /**
     * Generic search implementation which subclasses may expose if appropriate.
     * @see SearchingProvider
     */
    @SuppressWarnings({ "unchecked" })
    protected List<D> doSearch(String partialName) {
        if (!SearchingProvider.class.isAssignableFrom(providerType())) {
            throw new StudyCalendarError("%s is not a %s.  Search won't work.",
                providerType().getSimpleName(), SearchingProvider.class.getSimpleName());
        }

        List<P> providers = getOsgiLayerTools().getServices(providerType());
        List<D> found = new ArrayList<D>();
        for (P provider : providers) {
            List<D> providerMatches = ((SearchingProvider<D>) provider).search(partialName);
            if (providerMatches != null) {
                for (D match : providerMatches) {
                    found.add(provisionInstance(match, provider));
                }
            }
        }
        return found;
    }

    ////// CONFIGURATION

    public OsgiLayerTools getOsgiLayerTools() {
        return osgiLayerTools;
    }

    @Required
    public void setOsgiLayerTools(OsgiLayerTools tools) {
        this.osgiLayerTools = tools;
    }

    public NowFactory getNowFactory() {
        return nowFactory;
    }

    @Required
    public void setNowFactory(NowFactory nowFactory) {
        this.nowFactory = nowFactory;
    }

    /**
     * Template base class for a single refresh request over a list of objects.
     * Breaks up the object list to refresh into groups by provider, determines which ones
     * need to be refreshed, then hands off the subgroups to the subclass to perform
     * the actual refresh.
     */
    protected abstract class AbstractRefresh {
        private Map<String, P> providers;

        protected AbstractRefresh() {
            providers = getProviders();
            log.debug("{} provider(s) live", providers.size());
        }

        public List<D> execute(List<D> in) {
            Map<String, List<D>> toUpdate = findToUpdate(in);
            for (String providerToken : toUpdate.keySet()) {
                refreshFromProvider(providers.get(providerToken), toUpdate.get(providerToken));
            }
            return in;
        }

        private Map<String, List<D>> findToUpdate(List<D> source) {
            log.debug("Looking for instances to update from a list of {} source item(s)", source.size());

            Map<String, List<D>> toUpdate = new HashMap<String, List<D>>();

            Timestamp now = getNowFactory().getNowTimestamp();
            for (D item : source) {
                String providerName = item.getProvider();
                if (providerName == null) continue;
                if (shouldRefresh(item, now)) {
                    if (!toUpdate.containsKey(providerName)) {
                        toUpdate.put(providerName, new ArrayList<D>());
                    }
                    toUpdate.get(providerName).add(item);
                }
            }

            return toUpdate;
        }

        private boolean shouldRefresh(D providable, Timestamp now) {
            P dataProvider = providers.get(providable.getProvider());
            if (!(dataProvider instanceof RefreshableProvider)) return false;
            Integer interval = ((RefreshableProvider) dataProvider).getRefreshInterval();
            if (interval == null || interval < 0) return false;
            interval = 1000 * interval; // convert to ms
            long lastRefresh = providable.getLastRefresh() == null ? 0 : providable.getLastRefresh().getTime();
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

        protected void refreshFromProvider(P provider, List<D> currentVersions) {
            log.debug("Attempting to update {} instances from provider '{}'", currentVersions.size(), provider.providerToken());

            List<D> newVersions;
            try {
                newVersions = loadNewVersions(provider, currentVersions);
            } catch (RuntimeException re) {
                log.error("Refreshing " + currentVersions.size() + ' ' +
                    providerType().getSimpleName() + " instance(s) from " + provider.providerToken() +
                    "failed", re);
                log.debug("Specifically, the provider was trying to refresh {}", currentVersions);
                return;
            }

            if (newVersions == null) {
                log.error(
                    "Provider {} violated protocol by returning null.  Ignoring.",
                    provider.providerToken());
            } else if (newVersions.size() != currentVersions.size()) {
                log.error(
                    "Provider {} violated protocol returning the wrong number of results ({} when expecting {}).  Ignoring.",
                    new Object[] { provider.providerToken(), newVersions.size(), currentVersions.size() });
                newVersions = null;
            }

            if (newVersions != null) {
                updateInstances(currentVersions, newVersions, provider);
            }
        }

        protected abstract List<D> loadNewVersions(P provider, List<D> targetSites);

        private void updateInstances(List<D> currentVersions, List<D> newVersions, P provider) {
            for (ListIterator<D> lit = currentVersions.listIterator(); lit.hasNext();) {
                D current = lit.next();
                D newVersion = newVersions.get(lit.previousIndex());
                if (newVersion != null) {
                    provisionInstance(newVersion, provider);
                    updateInstanceInPlace(current, newVersion);
                }
            }
        }

        protected abstract void updateInstanceInPlace(D current, D newVersion);
    }
}
