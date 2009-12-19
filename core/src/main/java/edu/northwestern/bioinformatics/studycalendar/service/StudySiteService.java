package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.StudyCalendarAuthorizationManager;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.DomainObjectTools.parseExternalObjectId;
import edu.northwestern.bioinformatics.studycalendar.service.dataproviders.StudySiteConsumer;
import gov.nih.nci.security.authorization.domainobjects.ProtectionGroup;
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
        List<Site> availableSites = new ArrayList<Site>();
        List<Site> assignedSites = new ArrayList<Site>();
        List<ProtectionGroup> allSitePGs = authorizationManager.getSites();
        for (ProtectionGroup sitePG : allSitePGs) {
            String pgName = sitePG.getProtectionGroupName();
            Integer id = parseExternalObjectId(pgName);
            Site site = siteService.getById(id);
            if (site == null) throw new StudyCalendarSystemException("%s does not map to a PSC site", pgName);
            availableSites.add(site);
        }
        for (StudySite ss : studyTemplate.getStudySites()) {
            assignedSites.add(ss.getSite());
        }
        availableSites = ListUtils.subtract(availableSites, assignedSites);
        siteLists.put(StudyCalendarAuthorizationManager.ASSIGNED_PGS, assignedSites);
        siteLists.put(StudyCalendarAuthorizationManager.AVAILABLE_PGS, availableSites);

        return siteLists;
    }


    public List<Site> refreshAssociatedSites(Study study) {
        Set<Site> updated = new LinkedHashSet<Site>();

        updated.addAll(getAssociatedSitesFromConsumer(study));
        updated.addAll(getAssociatedSitesFromAuthorizationManager());

        return new ArrayList<Site>(updated);
    }

    private Set<Site> getAssociatedSitesFromAuthorizationManager() {
        Set<Site> results = new HashSet<Site>();
        List<ProtectionGroup> allSitePGs = authorizationManager.getSites();
        for (ProtectionGroup sitePG : allSitePGs) {
            String pgName = sitePG.getProtectionGroupName();
            Integer id = parseExternalObjectId(pgName);
            Site site = siteService.getById(id);
            if (site == null) throw new StudyCalendarSystemException("%s does not map to a PSC site", pgName);
            results.add(site);
        }
        return results;
    }

    private Set<Site> getAssociatedSitesFromConsumer(Study study) {
        Set<Site> results = new HashSet<Site>();
        List<StudySite> fromConsumer = studySiteConsumer.refresh(study);
        for (StudySite studySite : fromConsumer) {
            results.add(studySite.getSite());
        }
        return results;
    }

    public void assignStudyToSites(Study study, List<Site> sites) {
        if (study == null) {
            throw new IllegalArgumentException(STUDY_IS_NULL);
        }
        if (sites == null) {
            throw new IllegalArgumentException(SITES_LIST_IS_NULL);
        }
        for (Site site : sites) {
            createStudySite(study, site);
        }
    }

    private StudySite createStudySite(Study study, Site site) {
        StudySite result = null;
        if (study != null && site != null) {
            result = new StudySite();
            result.setStudy(study);
            result.setSite(site);
            studySiteDao.save(result);
        }
        return result;
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


    @Required
    public void setStudyCalendarAuthorizationManager(StudyCalendarAuthorizationManager authorizationManager) {
        this.authorizationManager = authorizationManager;
    }

    @Required
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setStudySiteConsumer(StudySiteConsumer studySiteConsumer) {
        this.studySiteConsumer = studySiteConsumer;
    }

    public void setStudySiteDao(StudySiteDao studySiteDao) {
        this.studySiteDao = studySiteDao;
    }
}
