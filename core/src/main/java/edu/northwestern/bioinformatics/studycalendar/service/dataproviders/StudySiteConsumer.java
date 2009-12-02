package edu.northwestern.bioinformatics.studycalendar.service.dataproviders;

import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.StudySiteProvider;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;

import java.util.ArrayList;
import static java.util.Arrays.asList;
import java.util.List;
import java.util.Map;

public class StudySiteConsumer extends AbstractConsumer {
    @Override protected Class<StudySiteProvider> providerType() { return StudySiteProvider.class; }

    public List<StudySite> refresh(Study in) {
        return refresh(asList(in)).get(0);
    }

    public List<List<StudySite>> refresh(List<Study> in) {
        return new StudySpecificRefresh().execute(in);
    }

    private class StudySpecificRefresh {    
        protected List<List<StudySite>> loadNewVersions(StudySiteProvider provider, List<Study> targetedStudy) {
            return provider.getAssociatedSites(targetedStudy);
        }
        
        protected void updateInstanceInPlace(StudySite current, StudySite newVersion) {
            current.setLastRefresh(newVersion.getLastRefresh());
        }


        // TODO: Refactor Me, everything below this line is a duplication of AbstractConsumer
        private Map<String, StudySiteProvider> providers;

        protected StudySpecificRefresh() {
            providers = getProviders();
        }

        @SuppressWarnings({ "unchecked" })
        public List<List<StudySite>> execute(List<Study> in) {
            List<List<StudySite>> results = new ArrayList<List<StudySite>>();
            for (StudySiteProvider provider : providers.values()) {
                List<List<StudySite>> fromProvider = loadNewVersions(provider, in);

                for (int i = 0; i < in.size(); i++) {
                    Study study = in.get(i);
                    List<StudySite> currentStudySites = in.get(i).getStudySites();
                    List<StudySite> fromProviderStudySites = fromProvider.get(i);
                    provisionInstances(fromProviderStudySites, provider);
                    associateStudy(fromProviderStudySites, study);
                    List<StudySite> merged = union(currentStudySites, fromProviderStudySites);
                    results.add(merged);
                }

            }

            return results;
        }

        private List<StudySite> union(List<StudySite> first, List<StudySite> second) {
            List<StudySite> merged = new ArrayList<StudySite>(first);
            for (StudySite ss : second) {
                if (!merged.contains(ss)) {
                    merged.add(ss);
                }
            }
            return merged;
        }

        private void associateStudy(List<StudySite> fromProviderStudySites, Study study) {
            for (StudySite ss : fromProviderStudySites) {
                ss.setStudy(study);
            }
        }


        protected void provisionInstances(List<StudySite> in, StudySiteProvider provider) {
         for (StudySite studySite : in) {
             provisionInstance(studySite, provider);
         }
     }
            /**
     * Generic search implementation which subclasses may expose if appropriate.
     * @see edu.northwestern.bioinformatics.studycalendar.dataproviders.api.SearchingProvider
     */
//    @SuppressWarnings({ "unchecked" })
//    protected List<D> doSearch(String partialName) {
//        if (!SearchingProvider.class.isAssignableFrom(providerType())) {
//            throw new StudyCalendarError("%s is not a %s.  Search won't work.",
//                providerType().getSimpleName(), SearchingProvider.class.getSimpleName());
//        }
//
//        List<P> providers = getOsgiLayerTools().getServices(providerType());
//        List<D> found = new ArrayList<D>();
//        for (P provider : providers) {
//            List<D> providerMatches = ((SearchingProvider<D>) provider).search(partialName);
//            if (providerMatches != null) {
//                for (D match : providerMatches) {
//                    found.add(provisionInstance(match, provider));
//                }
//            }
//        }
//        return found;
//    }
//        private void updateAssociated(List<Study> in) {
//            Map<String, List<Study>> toUpdate = findToUpdate(in);
//            for (String providerToken : toUpdate.keySet()) {
//                refreshFromProvider(providers.get(providerToken), toUpdate.get(providerToken));
//            }
//        }

//        private Map<String, List<Study>> findToUpdate(List<Study> source) {
//            Map<String, List<Study>> toUpdate = new HashMap<String, List<Study>>();
//
//            Timestamp now = getNowFactory().getNowTimestamp();
//            for (Study item : source) {
//                String providerName = item.getProvider();
//                if (providerName == null) continue;
//                if (shouldRefresh(item, now)) {
//                    if (!toUpdate.containsKey(providerName)) {
//                        toUpdate.put(providerName, new ArrayList<Study>());
//                    }
//                    toUpdate.get(providerName).add(item);
//                }
//            }
//
//            return toUpdate;
//        }
//
//        private boolean shouldRefresh(Study providable, Timestamp now) {
//            StudySiteProvider dataProvider = providers.get(providable.getProvider());
//            if (!(dataProvider instanceof RefreshableProvider)) return false;
//            Integer interval = ((RefreshableProvider) dataProvider).getRefreshInterval();
//            if (interval == null || interval < 0) return false;
//            interval = 1000 * interval; // convert to ms
//            long lastRefresh = providable.getLastRefresh() == null ? 0 : providable.getLastRefresh().getTime();
//            long timeSince = now.getTime() - lastRefresh;
//            log.debug("{}ms since last refresh", timeSince);
//            if (timeSince < interval) {
//                log.debug(" - within refresh interval {}ms; skipping", interval);
//                return false;
//            } else {
//                log.debug(" - greater than refresh interval {}ms; refreshing", interval);
//                return true;
//            }
//        }

//        protected void refreshFromProvider(StudySiteProvider provider, List<Study> currentVersions) {
//            List<StudySite> newVersions;
//            try {
//                newVersions = loadNewVersions(provider, currentVersions);
//            } catch (RuntimeException re) {
//                log.error("Refreshing " + currentVersions.size() + ' ' +
//                    providerType().getSimpleName() + " instance(s) from " + provider.providerToken() +
//                    "failed", re);
//                log.debug("Specifically, the provider was trying to refresh {}", currentVersions);
//                return;
//            }
//
//            if (newVersions == null) {
//                log.error(
//                    "Provider {} violated protocol by returning null.  Ignoring.",
//                    provider.providerToken());
//            } else if (newVersions.size() != currentVersions.size()) {
//                log.error(
//                    "Provider {} violated protocol returning the wrong number of results ({} when expecting {}).  Ignoring.",
//                    new Object[] { provider.providerToken(), newVersions.size(), currentVersions.size() });
//                newVersions = null;
//            }
//
//            if (newVersions != null) {
//                updateInstances(currentVersions, newVersions, provider);
//            }
//        }
//
//        private void updateInstances(List<Study> currentVersions, List<D> newVersions, StudySiteProvider provider) {
//            for (ListIterator<D> lit = currentVersions.listIterator(); lit.hasNext();) {
//                D current = lit.next();
//                D newVersion = newVersions.get(lit.previousIndex());
//                if (newVersion != null) {
//                    provisionInstance(newVersion, provider);
//                    updateInstanceInPlace(current, newVersion);
//                }
//            }
//        }
    }
}