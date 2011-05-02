package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySecondaryIdentifier;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.service.dataproviders.StudySiteConsumer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import static java.util.Arrays.asList;
import static java.util.Collections.EMPTY_LIST;
import static org.apache.commons.collections.CollectionUtils.collect;
import static org.apache.commons.lang.StringUtils.isNotBlank;

public class StudySiteService {
    private SiteService siteService;
    private StudySiteDao studySiteDao;
    private StudySiteConsumer studySiteConsumer;

    public static final String SITE_IS_NULL = "Site is null";
    public static final String STUDY_IS_NULL = "Study is null";
    public static final String SITES_LIST_IS_NULL = "Sites List is null";
    private final Log logger = LogFactory.getLog(getClass());
    private StudyDao studyDao;

    public List<Site> refreshAssociatedSites(Study study) {
        List<StudySite> updated = refreshStudySitesForStudy(study);
        return collectSites(updated);
    }

    public List<StudySite> assignStudyToSites(Study study, List<Site> sites) {
        if (study == null) { throw new IllegalArgumentException(STUDY_IS_NULL); }
        if (sites == null) { throw new IllegalArgumentException(SITES_LIST_IS_NULL); }

        List<StudySite> results = new ArrayList<StudySite>();
        for (Site site : sites) {
            StudySite studySite = StudySite.findStudySite(study, site);
            if (studySite == null) {
                studySite = new StudySite(study, site);
                studySiteDao.save(studySite);
            }
            results.add(studySite);
        }
        return results;
    }

    public void removeStudyFromSites(Study study, List<Site> sites) {
        for (Site site : sites) {
            StudySite found = StudySite.findStudySite(study, site);
            if (found != null) {
                if (!found.isUsed()) {
                    removeStudySite(found);
                } else {
                    logger.debug("Cannot remove the study site with id " + found.getId() + " because it is currently being used.");
                }
            }
        }
    }

    private void removeStudySite(StudySite removing) {
        Site site = removing.getSite();
        Study study = removing.getStudy();
        site.getStudySites().remove(removing);
        study.getStudySites().remove(removing);
        studySiteDao.delete(removing);
    }

    @SuppressWarnings({"unchecked"})
    public List<StudySite> refreshStudySitesForStudy(final Study study) {
        if (study == null) { throw new IllegalArgumentException(STUDY_IS_NULL);}
        return refreshStudySitesForStudies(asList(study)).get(0);
    }

    @SuppressWarnings({"unchecked"})
    public List<List<StudySite>> refreshStudySitesForStudies(final List<Study> studies) {
        if (studies == null) { throw new IllegalArgumentException(STUDY_IS_NULL);}

        List<List<StudySite>> refreshed = new ArrayList<List<StudySite>>();

        final Map<String, List<Site>> sites = buildProvidedSiteMap();
        List<List<StudySite>> allProvided = studySiteConsumer.refreshSites(studies);
        
        for (int i = 0; i < studies.size(); i++) {
            final Study study = studies.get(i);
            List<StudySite> provided = allProvided.get(i);
            if (provided == null) {
                provided = EMPTY_LIST;
            }

            Collection<StudySite> qualifying = CollectionUtils.select(provided, new Predicate(){
                public boolean evaluate(Object o) {
                    StudySite potential  = (StudySite) o;

                    // Verify Study Provider and StudySite Provider Are Equal
                    if (study.getProvider() == null || !study.getProvider().equals(potential.getProvider())) {
                        return false;
                    }

                    // Verify Site Provider and StudySite Provider Are Equal (And Site Exists)
                    List<Site> providerSpecific = sites.get(potential.getProvider());
                    if (providerSpecific == null || !providerSpecific.contains(potential.getSite())) {
                        return false;
                    }

                    // Verify new study site
                    Site site = providerSpecific.get(providerSpecific.indexOf(potential.getSite()));
                    if (StudySite.findStudySite(study, site) != null) {
                        return false;
                    }

                    return true;
                }
            });

            logger.debug("Found " + qualifying.size() + " new study sites from the provider.");
            for (StudySite u : qualifying) {
                logger.debug("- " + u);
            }

            // StudySites returned from provider are proxied by CGLIB.  This causes problems when saving,
            // so we want to create a fresh StudySite instance. Also, we want to populate the site with a
            // valid Site from SiteService.
            Collection<StudySite> enhanced = CollectionUtils.collect(qualifying, new Transformer(){
                public Object transform(Object o) {
                    StudySite s  = (StudySite) o;
                    List<Site> providerSpecific = sites.get(s.getProvider());
                    Site site = providerSpecific.get(providerSpecific.indexOf(s.getSite()));

                    StudySite e = new StudySite(study, site);
                    e.getStudy().addStudySite(e);
                    e.getSite().addStudySite(e);
                    e.setProvider(s.getProvider());
                    e.setLastRefresh(s.getLastRefresh());
                    return e;
                }
            });

            for (StudySite s : enhanced) {
                studySiteDao.save(s);
            }

            refreshed.add(study.getStudySites());
        }

        return refreshed;
    }

    private Map<String, List<Site>> buildProvidedSiteMap() {
        Map<String, List<Site>> results = new HashMap<String, List<Site>>();
        for (Site s : siteService.getAll()) {
            String p = s.getProvider();
            if (isNotBlank(p)) {
                if (results.get(p) == null) {
                    results.put(p, new ArrayList<Site>());
                }
                results.get(p).add(s);
            }
        }
        return results;
    }

    @SuppressWarnings({"unchecked"})
    public List<StudySite> refreshStudySitesForSite(final Site site) {
        if (site == null) { throw new IllegalArgumentException(STUDY_IS_NULL);}
        return refreshStudySitesForSites(asList(site)).get(0);
    }

    @SuppressWarnings({"unchecked"})
    public List<List<StudySite>> refreshStudySitesForSites(final List<Site> sites) {
        if (sites == null) { throw new IllegalArgumentException(SITE_IS_NULL);}

        List<List<StudySite>> refreshed = new ArrayList<List<StudySite>>();

        final Map<String, List<Study>> studies = buildProvidedStudyMap();
        final List<List<StudySite>> allProvided = studySiteConsumer.refreshStudies(sites);

        for (int i = 0; i < sites.size(); i++) {
            final Site site = sites.get(i);
            List<StudySite> provided = allProvided.get(i);
            if (provided == null) {
                provided = EMPTY_LIST;
            }

            Collection<StudySite> qualifying = CollectionUtils.select(provided, new Predicate(){
                public boolean evaluate(Object o) {
                    StudySite potential  = (StudySite) o;

                    // Verify Provider for existing Site is equal to StudySite Provider
                    if (site.getProvider() == null || !site.getProvider().equals(potential.getProvider())) {
                        return false;
                    }

                    // Verify Provider for existing Study is equal to StudySite Provider (And Study Exists)
                    List<Study> providerSpecific = studies.get(potential.getProvider());
                    if (providerSpecific == null || CollectionUtilsPlus.matching(asList(potential.getStudy()), providerSpecific, StudySecondaryIdentifierMatcher.instance()).size() == 0) {
                        return false;
                    }

                    // Verify new study site
                    Study study = (Study) CollectionUtilsPlus.matching(asList(potential.getStudy()), providerSpecific, StudySecondaryIdentifierMatcher.instance()).iterator().next();
                    if (StudySite.findStudySite(study, site) != null) {
                        return false;
                    }

                    return true;
                }
            });

            logger.debug("Found " + qualifying.size() + " new study sites from the provider.");
            for (StudySite u : qualifying) {
                logger.debug("- " + u);
            }

            // StudySites returned from provider are proxied by CGLIB.  This causes problems when saving,
            // so we want to create a fresh StudySite instance. Also, we want to populate the site with a
            // valid Site from SiteService.
            Collection<StudySite> enhanced = CollectionUtils.collect(qualifying, new Transformer(){
                public Object transform(Object o) {
                    StudySite s  = (StudySite) o;

                    List<Study> providerSpecific = studies.get(s.getProvider());
                    Study study = (Study) CollectionUtilsPlus.matching(asList(s.getStudy()), providerSpecific, StudySecondaryIdentifierMatcher.instance()).iterator().next();

                    StudySite e = new StudySite(study, site);
                    e.getStudy().addStudySite(e);
                    e.getSite().addStudySite(e);
                    e.setProvider(s.getProvider());
                    e.setLastRefresh(s.getLastRefresh());
                    return e;
                }
            });

            for (StudySite s : enhanced) {
                studySiteDao.save(s);
            }

            refreshed.add(site.getStudySites());
        }

        return refreshed;
    }

    private Map<String, List<Study>> buildProvidedStudyMap() {
        Map<String, List<Study>> results = new HashMap<String, List<Study>>();
        for (Study s : studyDao.getAll()) {
            String p = s.getProvider();
            if (isNotBlank(p)) {
                if (results.get(p) == null) {
                    results.put(p, new ArrayList<Study>());
                }
                results.get(p).add(s);
            }
        }
        return results;
    }

    protected static class CollectionUtilsPlus {
        public static Collection matching(Collection lefts, Collection rights, CollectionMatcher matcher) {
            return toggleMatching(lefts, rights, matcher, true);
        }

        public static Collection nonmatching(Collection lefts, Collection rights, CollectionMatcher matcher) {
            return toggleMatching(lefts, rights, matcher, false);
        }

        private static Collection toggleMatching(Collection lefts, Collection rights, CollectionMatcher matcher, boolean toggle) {
            Collection matching = new ArrayList();
            if (lefts == null || rights == null) {
                return matching;
            }

            for (Object l : lefts) {
                boolean match = false;
                for (Object r : rights) {
                    if (l != null && r != null && matcher.match(l, r)) {
                        match = true;
                    }
                }
                if (match == toggle) {
                    matching.add(l);
                }
            }
            return matching;
        }
    }

    protected interface CollectionMatcher {
        public boolean match(Object o1, Object o2);
    }

    protected static class StudySecondaryIdentifierMatcher implements CollectionMatcher {

        public static StudySecondaryIdentifierMatcher instance() {
            return new StudySecondaryIdentifierMatcher();
        }

        public boolean match(Object o1, Object o2) {
            SortedSet<StudySecondaryIdentifier> s1 = resolveStudySecondaryIdentifier(o1);
            SortedSet<StudySecondaryIdentifier> s2 = resolveStudySecondaryIdentifier(o2);

            if (s1 == null || s2 == null) return false;

            return CollectionUtils.intersection(s1,s2).size() > 0;
        }

        private static SortedSet<StudySecondaryIdentifier> resolveStudySecondaryIdentifier(Object o) {
            if (o instanceof Study) {
                return ((Study) o).getSecondaryIdentifiers();
            } else if (o instanceof StudySite) {
                return resolveStudySecondaryIdentifier(((StudySite) o).getStudy());
            }
            return null;
        }
    }


    public StudySite getStudySite(String studyAssignedId, String siteAssignedId) {
        Study study = studyDao.getByAssignedIdentifier(studyAssignedId);
        if (study != null) {
            List<StudySite> studySites = refreshStudySitesForStudy(study);
            for (StudySite studySite : studySites) {
                if (studySite.getSite().getAssignedIdentifier().equals(siteAssignedId)) {
                    return studySite;
                }
            }
        }
        return null;
    }

    public StudySite resolveStudySite(StudySite studySite) {
        String studyIdent = studySite.getStudy().getAssignedIdentifier();
        if (studyIdent == null) {
            throw new StudyCalendarValidationException("No study identifier specified.");
        }
        Study study = studyDao.getByAssignedIdentifier(studyIdent);
        if (study == null) {
            throw new StudyCalendarValidationException("Study %s not found.", studyIdent);
        }

        String siteIdent = studySite.getSite().getAssignedIdentifier();
        if (siteIdent == null) {
            throw new StudyCalendarValidationException("No site identifier specified");
        }
        Site site  = siteService.getByAssignedIdentifier(siteIdent);
        if (site == null) {
            throw new StudyCalendarValidationException("Site %s not found.", siteIdent);
        }

        StudySite existingStudySite = study.getStudySite(site);
        if (existingStudySite != null) {
            return existingStudySite;
        } else {
            studySite.setStudy(study);
            studySite.setSite(site);
            return studySite;
        }
    }

    ///// Collect Helpers
    @SuppressWarnings({"unchecked"})
    private List<Site> collectSites(List<StudySite> in) {
        return new ArrayList<Site>( collect(in, new Transformer() {
            public Object transform(Object o) {
                if (o instanceof StudySite) {
                    return ((StudySite) o).getSite();
                }
                return null;
            }
        })
        );
    }


    @Required
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    @Required
    public void setStudySiteConsumer(StudySiteConsumer studySiteConsumer) {
        this.studySiteConsumer = studySiteConsumer;
    }

    @Required
    public void setStudySiteDao(StudySiteDao studySiteDao) {
        this.studySiteDao = studySiteDao;
    }

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }


}
