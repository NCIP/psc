package edu.northwestern.bioinformatics.studycalendar.service.dataproviders;

import edu.northwestern.bioinformatics.studycalendar.core.osgi.OsgiLayerTools;
import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.DataProvider;
import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.RefreshableProvider;
import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.SearchingProvider;
import edu.northwestern.bioinformatics.studycalendar.domain.Providable;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import gov.nih.nci.cabig.ctms.lang.NowFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.sql.Timestamp;

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
        }

        public List<D> execute(List<D> in) {
            Map<String, List<D>> toUpdate = findToUpdate(in);
            for (String providerToken : toUpdate.keySet()) {
                refreshFromProvider(providers.get(providerToken), toUpdate.get(providerToken));
            }
            return in;
        }

        protected abstract void refreshFromProvider(P provider, List<D> source);

        private Map<String, List<D>> findToUpdate(List<D> source) {
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
    }
}
