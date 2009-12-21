package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.StudyCalendarAuthorizationManager;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.service.dataproviders.StudySiteConsumer;
import static org.apache.commons.collections.CollectionUtils.collect;
import static org.apache.commons.collections.CollectionUtils.intersection;
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
        Set<Site> combined = new LinkedHashSet<Site>();

        combined.addAll(getAssociatedSitesFromConsumer(study));
        combined.addAll(getAssociatedSitesFromSiteService(study));

        // Automatically save new StudySite associations
        List<StudySite> saved = assignStudyToSites(study, new ArrayList<Site>(combined));

        return collectSites(saved);
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
        List<Site> sitesFromConsumer = collectSites(fromConsumer);
        List<Site> availableSites = siteService.getAll();
        return new HashSet<Site>(intersection(sitesFromConsumer, availableSites));
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


    ///// Collect Helpers
    @SuppressWarnings({"unchecked"})
    private List<Site> collectSites(List<StudySite> fromConsumer) {
        return new ArrayList<Site>( collect(fromConsumer, new Transformer() {
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
}
