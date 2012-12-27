/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.dataproviders;

import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.DataProvider;
import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.RefreshableProvider;
import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.StudySiteProvider;
import edu.northwestern.bioinformatics.studycalendar.domain.Providable;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import org.apache.commons.collections.CollectionUtils;
import static org.apache.commons.collections.CollectionUtils.intersection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Timestamp;
import java.util.ArrayList;
import static java.util.Arrays.asList;
import static java.util.Collections.EMPTY_LIST;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@Transactional(readOnly = true)
public class StudySiteConsumer extends AbstractConsumer {
    @Override protected Class<StudySiteProvider> providerType() { return StudySiteProvider.class; }
    private final Log logger = LogFactory.getLog(getClass());

    public List<StudySite> refresh(Study in) {
        return refreshSites(asList(in)).get(0);
    }

    public List<List<StudySite>> refreshSites(List<Study> in) {
        return new StudyBasedStudySiteRefresh().execute(in);
    }

    public List<StudySite> refresh(Site in) {
        return refreshStudies(asList(in)).get(0);
    }

    public List<List<StudySite>> refreshStudies(List<Site> in) {
        return new SiteBasedStudySiteRefresh().execute(in);
    }

    private class SiteBasedStudySiteRefresh extends AssociationRefresh<Site, StudySite, StudySiteProvider> {
        protected List<List<StudySite>> loadNewVersions(StudySiteProvider provider, List<Site> targetSites) {
            return provider.getAssociatedStudies(targetSites);
        }

        protected List<StudySite> getAssociated(Site base) {
            return base.getStudySites();
        }

        protected void enhanceInstances(List<StudySite> newInstances, Site site) {
            for (StudySite ss : newInstances) {
                ss.setSite(site);
            }
        }

        protected List<StudySite> merge(List<StudySite> existing, List<StudySite> provided) {
            List<StudySite> merged = new ArrayList<StudySite>(provided);

            for (StudySite e : existing) {
                boolean contains = false;
                for (StudySite p : provided) {
                    if (e.getStudy().getSecondaryIdentifiers() == null ? p.getStudy().getSecondaryIdentifiers() == null : !intersection(e.getStudy().getSecondaryIdentifiers(), p.getStudy().getSecondaryIdentifiers()).isEmpty()) {
                        contains = true;
                    }
                }
                if (!contains) {
                    merged.add(e);
                }
            }

            return merged;
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

        protected List<StudySite> merge(List<StudySite> existing, List<StudySite> provided) {
            return union(existing, provided);
        }
    }

    public abstract class AssociationRefresh<B extends Providable, A extends Providable, P extends DataProvider> {
        protected abstract List<List<A>> loadNewVersions(P provider, List<B> base);
        protected abstract List<A> getAssociated(B base);
        protected abstract void enhanceInstances(List<A> associations, B base);
        protected abstract List<A> merge(List<A> existing, List<A> provided);

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
            filloutWithNulls(results, in.size());
            logger.debug("in.size: " + in.size());
            logger.debug("providers.size: " + providers.size());

            Map<String, List<B>> toUpdate = findInstancesToUpdate(in);

            for (String providerName : toUpdate.keySet()) {
                P provider = (P) getProvider(providerName);

                List<List<A>> allFromProvider;
                try {
                    allFromProvider = loadNewVersions(provider, toUpdate.get(providerName));
                } catch (RuntimeException re) {
                    log.error("Refreshing " + toUpdate.get(providerName).size() + ' ' +
                        providerType().getSimpleName() + " instance(s) from " + provider.providerToken() +
                        "failed", re);
                    log.debug("Specifically, the provider was trying to refresh {}", toUpdate.get(providerName));
                    allFromProvider = EMPTY_LIST;
                }

                List<B> associationsToUpdate = toUpdate.get(providerName);
                for (int i = 0; i < associationsToUpdate.size(); i++) {

                    B base = associationsToUpdate.get(i);
                    List<A> existing = getAssociated(base);

                    List<A> fromProvider;
                    if (i < allFromProvider.size()) {
                        fromProvider = allFromProvider.get(i);
                    } else {
                        fromProvider = EMPTY_LIST;
                    }

                    if (fromProvider == null) {fromProvider = new ArrayList<A>();}

                    provisionInstances(fromProvider, provider);
                    enhanceInstances(fromProvider, base);
                    updateTimestamps(existing, providerName);

                    logger.debug("Found " + fromProvider.size() + " study sites instances from the provider.");
                    for(A a : fromProvider) {
                        logger.debug("- " + a.toString());
                    }

                    List<A> merged = merge(existing, fromProvider);
                    
                    results.set(in.indexOf(base), merged);
                }

            }

            List<B> toUpdateflat = flatten(new ArrayList<List<B>>(toUpdate.values()));
            List<B> notUpdated = (List<B>) CollectionUtils.subtract(in, toUpdateflat);

            for (B base : notUpdated) {
                results.set(in.indexOf(base), getAssociated(base));
            }

            return results;
        }

        private void filloutWithNulls(List<List<A>> results, int i) {
            if (results.size() < i) {
                results.add(null);
                filloutWithNulls(results, i);
            }
        }

        private void updateTimestamps(List<A> in, String providerName) {
            for (A s : in) {
                logger.debug("- Updating timestamp for " + ((StudySite) s).getName());
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
            if (!(dataProvider instanceof RefreshableProvider)) return false;

            boolean result = false;

            if (providables.size() == 0) {
                result = true;
            } else {
                for (A providable : providables) {
                   result = result || shouldRefresh(providable, dataProvider, now);
                }
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
                    if (providerTokensMatch(base.getProvider(), provider.providerToken())) {
                        Timestamp now = getNowFactory().getNowTimestamp();
                        if (shouldRefresh(getAssociated(base), provider, now)) {
                            if (result.get(provider.providerToken()) == null) {
                                result.put(provider.providerToken(), new ArrayList<B>());
                            }
                            result.get(provider.providerToken()).add(base);
                        }
                    }
                }
            }

            return result;
        }

        protected boolean providerTokensMatch(String a, String b) {
            return a == null ? b == null : a.equals(b); 
        }


        ///// Collection Helpers
        protected <T> List<T> union(List<T> first, List<T> second) {
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