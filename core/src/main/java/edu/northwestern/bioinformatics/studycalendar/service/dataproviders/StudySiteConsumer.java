package edu.northwestern.bioinformatics.studycalendar.service.dataproviders;

import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.RefreshableProvider;
import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.StudySiteProvider;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import org.apache.commons.collections.CollectionUtils;

import java.sql.Timestamp;
import java.util.*;
import static java.util.Arrays.asList;

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

            Map<String, List<Study>> toUpdate = findInstancesToUpdate(in);

            for (String providerName : toUpdate.keySet()) {
                StudySiteProvider provider = (StudySiteProvider) getProvider(providerName);
                
                List<List<StudySite>> allFromProvider = loadNewVersions(provider, toUpdate.get(providerName));

                List<Study> studiesToUpdate = toUpdate.get(providerName);
                for (int i = 0; i < studiesToUpdate .size(); i++) {
                    Study study = studiesToUpdate.get(i);
                    List<StudySite> existing = studiesToUpdate.get(i).getStudySites();
                    List<StudySite> fromProvider = allFromProvider.get(i);

                    provisionInstances(fromProvider, provider);
                    associateWithStudy(fromProvider, study);
                    updateTimestamps(existing, providerName);

                    List<StudySite> merged = union(existing, fromProvider);
                    results.add(in.indexOf(study), merged);
                }

            }

            List<Study> toUpdateflat = flatten(new ArrayList(toUpdate.values()));
            List<Study> notUpdated = (List<Study>) CollectionUtils.subtract(in, toUpdateflat);

            for (Study study : notUpdated) {
                results.add(in.indexOf(study), study.getStudySites());
            }

            return results;
        }

        private void updateTimestamps(List<StudySite> in, String providerName) {
            for (StudySite s : in) {
                updateTimestamp(providerName, s);
            }
        }

        private void updateTimestamp(String providerName, StudySite s) {
            if (s.getProvider().equals(providerName)) {
                s.setLastRefresh(getNowFactory().getNowTimestamp());
            }
        }

        private void associateWithStudy(List<StudySite> fromProviderStudySites, Study study) {
            for (StudySite ss : fromProviderStudySites) {
                ss.setStudy(study);
            }
        }

        protected void provisionInstances(List<StudySite> in, StudySiteProvider provider) {
            for (StudySite studySite : in) {
                provisionInstance(studySite, provider);
            }
        }

        private boolean shouldRefresh(List<StudySite> providables, StudySiteProvider dataProvider, Timestamp now) {
            boolean result = false;
            for (StudySite providable : providables) {
                result = result || shouldRefresh(providable, dataProvider, now);
            }
            return result;
        }

        private boolean shouldRefresh(StudySite providable, StudySiteProvider dataProvider, Timestamp now) {
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

        private Map<String, List<Study>> findInstancesToUpdate(List <Study> in) {
            Map<String, List<Study>> result = new HashMap<String, List<Study>>();

            for (StudySiteProvider provider : providers.values()) {
                for (Study study : in) {
                    Timestamp now = getNowFactory().getNowTimestamp();
                    if (study.getStudySites().size() == 0 || shouldRefresh(study.getStudySites(), provider, now)) {
                        if (result.get(provider.providerToken()) == null) {
                            result.put(provider.providerToken(), new ArrayList<Study>());
                        }
                        result.get(provider.providerToken()).add(study);
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