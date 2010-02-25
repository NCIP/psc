package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.StudyCalendarAuthorizationManager;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.service.dataproviders.StudySiteConsumer;
import org.apache.commons.collections.CollectionUtils;
import static org.apache.commons.collections.CollectionUtils.collect;
import static org.apache.commons.collections.CollectionUtils.subtract;
import static org.apache.commons.collections.CollectionUtils.union;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections15.ListUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.*;

public class StudySiteService {
    private StudyCalendarAuthorizationManager authorizationManager;
    private SiteService siteService;
    private StudySiteDao studySiteDao;
    private StudySiteConsumer studySiteConsumer;

    public static final String SITE_IS_NULL = "Site is null";
    public static final String STUDY_IS_NULL = "Study is null";
    public static final String SITES_LIST_IS_NULL = "Sites List is null";
    private final Log logger = LogFactory.getLog(getClass());

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

    public Map<String, List<Site>> getSiteLists(Study studyTemplate) {
        if (studyTemplate == null) {
            throw new IllegalArgumentException(STUDY_IS_NULL);
        }
        Map<String, List<Site>> siteLists = new HashMap<String, List<Site>>();
        
        List<Site> availableSites = siteService.getAll();

        List<Site> assignedSites = new ArrayList<Site>();
        for (StudySite ss : studyTemplate.getStudySites()) {
            assignedSites.add(ss.getSite());
        }
        availableSites = ListUtils.subtract(availableSites, assignedSites);
        siteLists.put(StudyCalendarAuthorizationManager.ASSIGNED_PGS, assignedSites);
        siteLists.put(StudyCalendarAuthorizationManager.AVAILABLE_PGS, availableSites);

        return siteLists;
    }


    public List<Site> refreshAssociatedSites(Study study) {
        List<StudySite> updated = refreshStudySites(study);
        return collectSites(updated);
    }

    private Set<Site> getAssociatedSitesFromSiteService(Study study) {
        Set<Site> results = new HashSet<Site>();
        for (StudySite ss : study.getStudySites()) {
            results.add(ss.getSite());
        }
        return results;
    }

    @SuppressWarnings({"unchecked"})
    private Set<Site> getAssociatedSitesFromConsumer(Study study) {
        List<StudySite> fromConsumer = studySiteConsumer.refresh(study);

        logger.debug("Found " + fromConsumer.size() + " provided study sites associated with study " + study.getName() + ".");
        for (StudySite s : fromConsumer) {
            logger.debug("- Study: " + s.getStudy().getName() + " Site: " + s.getSite().getAssignedIdentifier());
        }

        List<Site> sitesFromConsumer = collectSites(fromConsumer);
        List<Site> availableSites = siteService.getAll();

        logger.debug("Found " + availableSites.size() + " sites avaialable.");
        for (Site s : availableSites) {
            logger.debug("- Site: " + s.getName());
        }

        logger.debug("Found " + intersection(availableSites, sitesFromConsumer).size() + " intersecting available and provider sites.");
        return new HashSet<Site>(intersection(availableSites, sitesFromConsumer));
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
    protected List<StudySite> refreshStudySites(Study study) {
        List<StudySite> existing = study.getStudySites();
        List<StudySite> provided = studySiteConsumer.refresh(study);


        Collection unsaved = subtract(provided, existing);

        logger.debug("Found " + unsaved.size() + " unsaved sites from the provider.");
        logger.debug("- " + unsaved);
        
        final List<Site> allSites = siteService.getAll();
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

        // Enhance the provided StudySite instance with the already persisted Site instance. We must
        // do this because the StudySite instance returned from the provider will only have assignedIdentifier
        // populated.
        Collection<StudySite> enhanced = CollectionUtils.collect(qualifying, new Transformer(){
            public Object transform(Object o) {
                StudySite s  = (StudySite) o;
                int i = allSites.indexOf(s.getSite());
                s.setSite(allSites.get(i));
                return s;
            }
        });

        for (StudySite s : enhanced) {
            StudySite ss = new StudySite(s.getStudy(), s.getSite());
            ss.setProvider(s.getProvider());
            ss.setLastRefresh(s.getLastRefresh());
            studySiteDao.save(ss);
        }

        return new ArrayList<StudySite>(union(existing, enhanced));
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

    private List<Site> intersection(List<Site> left, List<Site> right) {
        List<Site> results = new ArrayList<Site>();
        for (Site l : left) {
            for (Site r : right) {
                if (l != null && r != null) {
                    if (l.getAssignedIdentifier().equals(r.getAssignedIdentifier())) {
                        l.setProvider(r.getProvider());
                        l.setLastRefresh(r.getLastRefresh());
                        results.add(l);
                    }
                }

            }
        }
        return results;
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
}
