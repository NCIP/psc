package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.StudyCalendarAuthorizationManager;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.service.dataproviders.StudySiteConsumer;
import org.apache.commons.collections.CollectionUtils;
import static org.apache.commons.collections.CollectionUtils.*;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import static java.util.Arrays.asList;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;

public class StudySiteService {
    private StudyCalendarAuthorizationManager authorizationManager;
    private SiteService siteService;
    private StudySiteDao studySiteDao;
    private StudySiteConsumer studySiteConsumer;

    public static final String SITE_IS_NULL = "Site is null";
    public static final String STUDY_IS_NULL = "Study is null";
    public static final String SITES_LIST_IS_NULL = "Sites List is null";
    private final Log logger = LogFactory.getLog(getClass());
    private StudyDao studyDao;
    private StudyService studyService;

    public List<StudySite> getAllStudySitesForSubjectCoordinator(User user) {
        List<StudySite> studySites = new ArrayList<StudySite>();
        if (user != null) {
            UserRole userRole = user.getUserRole(Role.SUBJECT_COORDINATOR);
            if (userRole != null) {
                studySites.addAll(userRole.getStudySites());
            }
        }
        return studySites;
    }

    public List<StudySite> getStudySitesForSubjectCoordinator(User user, Site site) {
        List<StudySite> allStudySites = getAllStudySitesForSubjectCoordinator(user);
        List<StudySite> availableStudySites = new ArrayList<StudySite>();

        for (StudySite studySite : allStudySites) {
            if (studySite.getSite().equals(site)) {
                availableStudySites.add(studySite);
            }
        }
        return availableStudySites;
    }

    public List<StudySite> getStudySitesForSubjectCoordinator(User user, Study study) {
        List<StudySite> allStudySites = getAllStudySitesForSubjectCoordinator(user);
        List<StudySite> availableStudySites = new ArrayList<StudySite>();

        for (StudySite studySite : allStudySites) {
            if (studySite.getStudy().equals(study)) {
                availableStudySites.add(studySite);
            }
        }
        return availableStudySites;
    }


    public List<Site> refreshAssociatedSites(Study study) {
        List<StudySite> updated = refreshStudySites(study);
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
        try {
            authorizationManager.removeProtectionGroup(DomainObjectTools.createExternalObjectId(removing));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new StudyCalendarSystemException(e);
        }

        Site site = removing.getSite();
        Study study = removing.getStudy();
        site.getStudySites().remove(removing);
        study.getStudySites().remove(removing);
        if (removing.getUserRoles() != null) {
            for (UserRole r : removing.getUserRoles()) {
                r.getStudySites().remove(removing);
            }
        }
        studySiteDao.delete(removing);
    }

    @SuppressWarnings({"unchecked"})
    public List<StudySite> refreshStudySites(final Study study) {
        if (study == null) { throw new IllegalArgumentException(STUDY_IS_NULL);}
        return refreshStudySites(asList(study)).get(0);
    }

    @SuppressWarnings({"unchecked"})
    public List<List<StudySite>> refreshStudySites(final List<Study> studies) {
        if (studies == null) { throw new IllegalArgumentException(STUDY_IS_NULL);}

        List<List<StudySite>> refreshed = new ArrayList<List<StudySite>>();

        final List<Site> allSites = siteService.getAll();
        final List<List<StudySite>> allProvided = studySiteConsumer.refresh(studies);
        
        for (int i = 0; i < studies.size(); i++) {
            final Study study = studies.get(i);
            List<StudySite> provided = allProvided.get(i);

            List<StudySite> existing = study.getStudySites();


            Collection unsaved = subtract(provided, existing);

            logger.debug("Found " + unsaved.size() + " unsaved sites from the provider.");
            logger.debug("- " + unsaved);

            Collection qualifying = CollectionUtils.select(unsaved, new Predicate(){
                public boolean evaluate(Object o) {
                    StudySite s  = (StudySite) o;

                    return allSites.contains(s.getSite());
                }
            });

            logger.debug("There are" + allSites.size() + " sites total.");
            logger.debug("- " + allSites);


            logger.debug("Found " + qualifying.size() + " qualifying sites from the provider.");
            logger.debug("- " + qualifying);

            // StudySites returned from provider are proxied by CGLIB.  This causes problems when saving,
            // so we want to create a fresh StudySite instance. Also, we want to populate the site with a
            // valid Site from SiteService.
            Collection<StudySite> enhanced = CollectionUtils.collect(qualifying, new Transformer(){
                public Object transform(Object o) {
                    StudySite s  = (StudySite) o;
                    Site site = allSites.get(allSites.indexOf(s.getSite()));

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

            refreshed.add(new ArrayList<StudySite>(union(existing, enhanced)));
        }

        return refreshed;
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

        final List<Study> allStudies = studyDao.getAll();
        final List<List<StudySite>> allProvided = studySiteConsumer.refresh(sites);

        for (int i = 0; i < sites.size(); i++) {
            final Site site = sites.get(i);
            List<StudySite> provided = allProvided.get(i);

            List<StudySite> existing = site.getStudySites();

            Collection<StudySite> unsaved = CollectionUtilsPlus.nonmatching(provided, existing, StudySecondaryIdentifierMatcher.instance());

            logger.debug("Found " + unsaved.size() + " unsaved sites from the provider.");
            logger.debug("- " + unsaved);

            Collection<StudySite> qualifying = CollectionUtilsPlus.matching(unsaved, allStudies, StudySecondaryIdentifierMatcher.instance());

            logger.debug("Found " + qualifying.size() + " qualifying sites from the provider.");
            logger.debug("- " + qualifying);


            // StudySites returned from provider are proxied by CGLIB.  This causes problems when saving,
            // so we want to create a fresh StudySite instance. Also, we want to populate the site with a
            // valid Site from SiteService.
            Collection<StudySite> enhanced = CollectionUtils.collect(qualifying, new Transformer(){
                public Object transform(Object o) {
                    StudySite s  = (StudySite) o;
                    Study study = (Study) CollectionUtilsPlus.matching(allStudies, asList(s.getStudy()), StudySecondaryIdentifierMatcher.instance()).iterator().next();

                    StudySite e = new StudySite(study, site);
                    e.getStudy().addStudySite(e);
                    e.getSite().addStudySite(e);
                    e.setProvider(s.getProvider());
                    e.setLastRefresh(s.getLastRefresh());
                    return e;
                }
            });

            logger.debug("Found " + enhanced.size() + " enhanced sites from the provider.");
            logger.debug("- " + qualifying);

            for (StudySite s : enhanced) {
                studySiteDao.save(s);
            }

            refreshed.add(new ArrayList<StudySite>(union(existing, enhanced)));
        }

        return refreshed;
    }

    private static class CollectionUtilsPlus {
        public static Collection matching(Collection lefts, Collection rights, CollectionMatcher matcher) {
            Collection matching = new ArrayList();
            if (lefts == null || rights == null) {
                return matching;
            }

            for (Object l : lefts) {
                boolean match = false;
                for (Object r : rights) {
                    if (l != null && r != null && matcher.compare(l, r)) {
                        match = true;
                    }
                }
                if (match) {
                    matching.add(l);
                }
            }
            return matching;
        }

        public static Collection nonmatching(Collection lefts, Collection rights, CollectionMatcher matcher) {
            Collection matching = new ArrayList();
            if (lefts == null || rights == null) {
                return matching;
            }

            for (Object l : lefts) {
                boolean match = false;
                for (Object r : rights) {
                    if (l != null && r != null && matcher.compare(l, r)) {
                        match = true;
                    }
                }
                if (!match) {
                    matching.add(l);
                }
            }
            return matching;
        }
    }

    private interface CollectionMatcher {
        public boolean compare(Object o1, Object o2);
    }

    private static class StudySecondaryIdentifierMatcher implements CollectionMatcher {

        public static StudySecondaryIdentifierMatcher instance() {
            return new StudySecondaryIdentifierMatcher();
        }

        public boolean compare(Object o1, Object o2) {
            SortedSet<StudySecondaryIdentifier> s1 = resolveStudySecondaryIdentifier(o1);
            SortedSet<StudySecondaryIdentifier> s2 = resolveStudySecondaryIdentifier(o2);

            if (s1 == null || s2 == null) return false;

            return s1 == null ? s2 == null : CollectionUtils.intersection(s1,s2).size() > 0;
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
            List<StudySite> studySites = refreshStudySites(study);
            for (StudySite studySite : studySites) {
                if (studySite.getSite().getAssignedIdentifier().equals(siteAssignedId)) {
                    return studySite;
                }
            }
        }
        return null;
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
    public void setStudyCalendarAuthorizationManager(StudyCalendarAuthorizationManager authorizationManager) {
        this.authorizationManager = authorizationManager;
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
