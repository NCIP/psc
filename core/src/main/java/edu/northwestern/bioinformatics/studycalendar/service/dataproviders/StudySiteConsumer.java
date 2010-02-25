package edu.northwestern.bioinformatics.studycalendar.service.dataproviders;

import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.DataProvider;
import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.RefreshableProvider;
import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.StudySiteProvider;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Timestamp;
import java.util.ArrayList;
import static java.util.Arrays.asList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudySiteConsumer extends AbstractConsumer {
    @Override protected Class<StudySiteProvider> providerType() { return StudySiteProvider.class; }
    private final Log logger = LogFactory.getLog(getClass());

    public List<StudySite> refresh(Study in) {
        return refresh(asList(in)).get(0);
    }

    public List<List<StudySite>> refresh(List<Study> in, Object... o) {     // Variable Object argument is a hack to fix my overloading problem with generics
        return new StudyBasedStudySiteRefresh().execute(in);
    }

    public List<StudySite> refresh(Site in) {
        return refresh(asList(in)).get(0);
    }

    public List<List<StudySite>> refresh(List<Site> in) {
        return new SiteBasedStudySiteRefresh().execute(in);
    }

    private class SiteBasedStudySiteRefresh extends AssociationRefresh<Site, StudySite, StudySiteProvider> {
        protected List<List<StudySite>> loadNewVersions(StudySiteProvider provider, List<Site> targetedStudy) {
            return provider.getAssociatedStudies(targetedStudy);
        }

        protected List<StudySite> getAssociated(Site base) {
            return base.getStudySites();
        }

        protected void enhanceInstances(List<StudySite> newInstances, Site site) {
            for (StudySite ss : newInstances) {
                ss.setSite(site);
            }
        }
    }

    private class StudyBasedStudySiteRefresh extends AssociationRefresh<Study, StudySite, StudySiteProvider> {
        protected List<List<StudySite>> loadNewVersions(StudySiteProvider provider, List<Study> targetedStudy) {
            return provider.getAssociatedSites(targetedStudy);
        }

        protected List<StudySite> getAssociated(Study base) {
            return base.getStudySites();
        }

        protected void enhanceInstances(List<StudySite> newInstances, Study study) {
            for (StudySite ss : newInstances) {
                ss.setStudy(study);
            }
        }
    }

    private abstract class AssociationRefresh<B extends Providable, A extends Providable, P extends DataProvider> {
        protected abstract List<List<A>> loadNewVersions(P provider, List<B> base);
        protected abstract List<A> getAssociated(B base);
        protected abstract void enhanceInstances(List<A> associations, B base);

        protected void updateInstanceInPlace(A current, A newVersion) {
            current.setLastRefresh(newVersion.getLastRefresh());
        }

        // TODO: Refactor Me, everything below this line is a duplication of AbstractConsumer
        private Map<String, P> providers;

        protected AssociationRefresh() {
            providers = getProviders();
        }

        @SuppressWarnings({ "unchecked" })
        public List<List<A>> execute(List<B> in) {
            List<List<A>> results = new ArrayList<List<A>>();

            Map<String, List<B>> toUpdate = findInstancesToUpdate(in);

            for (String providerName : toUpdate.keySet()) {
                P provider = (P) getProvider(providerName);
                
                List<List<A>> allFromProvider = loadNewVersions(provider, toUpdate.get(providerName));

                List<B> associationsToUpdate = toUpdate.get(providerName);
                for (int i = 0; i < associationsToUpdate .size(); i++) {
                    B base = associationsToUpdate.get(i);
                    List<A> existing = getAssociated(base);
                    List<A> fromProvider = allFromProvider.get(i);


                    provisionInstances(fromProvider, provider);
                    enhanceInstances(fromProvider, base);
                    updateTimestamps(existing, providerName);

                    logger.debug("Found " + fromProvider.size() + " study sites instances from the provider.");
                    for(A a : fromProvider) {
                        logger.debug("- " + a.toString());
                    }

                    List<A> merged = union(existing, fromProvider);
                    logger.debug("Found " + merged.size() + " study sites instances total.");
                    results.add(in.indexOf(base), merged);
                }

            }

            List<B> toUpdateflat = flatten(new ArrayList<List<B>>(toUpdate.values()));
            List<B> notUpdated = (List<B>) CollectionUtils.subtract(in, toUpdateflat);

            for (B base : notUpdated) {
                results.add(in.indexOf(base), getAssociated(base));
            }

            return results;
        }

        private void updateTimestamps(List<A> in, String providerName) {
            for (A s : in) {
                updateTimestamp(providerName, s);
            }
        }

        private void updateTimestamp(String providerName, A a) {
            if (a.getProvider() != null && a.getProvider().equals(providerName)) {
                a.setLastRefresh(getNowFactory().getNowTimestamp());
            }
        }



        protected void provisionInstances(List<A> in, P provider) {
            for (A association : in) {
                log.debug("- provisioning study site {}", association.toString());
                provisionInstance(association, provider);
            }
        }

        private boolean shouldRefresh(List<A> providables, P dataProvider, Timestamp now) {
            boolean result = false;
            for (A providable : providables) {
                result = result || shouldRefresh(providable, dataProvider, now);
            }
            return result;
        }

        private boolean shouldRefresh(A providable, P dataProvider, Timestamp now) {
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

        /**
         * Returns a Map with the keys being the provider token and the
         * values being a list of object for which we need to refresh the associations.
         *
         * @param   in  the list of objects to check if they have been refreshed
         * @return      the image at the specified URL
         */
        private Map<String, List<B>> findInstancesToUpdate(List <B> in) {
            Map<String, List<B>> result = new HashMap<String, List<B>>();

            for (P provider : providers.values()) {
                for (B base : in) {
                    Timestamp now = getNowFactory().getNowTimestamp();
                    if (getAssociated(base).size() == 0 || shouldRefresh(getAssociated(base), provider, now)) {
                        if (result.get(provider.providerToken()) == null) {
                            result.put(provider.providerToken(), new ArrayList<B>());
                        }
                        result.get(provider.providerToken()).add(base);
                    }
                }
            }

            return result;
        }


        ///// Collection Helpers
        private <T> List<T> union(List<T> first, List<T> second) {
            List<T> merged = new ArrayList<T>(first);
            for (T ss : second) {
                if (!merged.contains(ss)) {
                    merged.add(ss);
                }
            }
            return merged;
        }

        private <T> List<T> flatten(List<List<T>> in) {
            List<T> result = new ArrayList<T>();
            for (List<T> studies : in) {
                result = union(result, studies);
            }
            return result;
        }
    }
}