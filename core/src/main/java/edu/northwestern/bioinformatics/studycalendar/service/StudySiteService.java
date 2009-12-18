package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySiteDao;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.StudyCalendarAuthorizationManager;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.DomainObjectTools.parseExternalObjectId;
import edu.northwestern.bioinformatics.studycalendar.service.dataproviders.StudySiteConsumer;
import gov.nih.nci.security.authorization.domainobjects.ProtectionGroup;
import org.apache.commons.collections15.ListUtils;
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

    public void assignTemplateToSites(Study studyTemplate, List<Site> sites) {
        if (studyTemplate == null) {
            throw new IllegalArgumentException(STUDY_IS_NULL);
        }
        if (sites == null) {
            throw new IllegalArgumentException(SITES_LIST_IS_NULL);
        }
        for (Site site : sites) {
            StudySite ss = new StudySite();
            ss.setStudy(studyTemplate);
            ss.setSite(site);
            studySiteDao.save(ss);
        }
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
