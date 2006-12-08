package edu.northwestern.bioinformatics.studycalendar.service;

import static edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarAuthorizationManager.AVAILABLE_PES;

import edu.nwu.bioinformatics.commons.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedList;
import java.util.Iterator;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Required;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.utils.DomainObjectTools;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarAuthorizationManager;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import gov.nih.nci.security.authorization.domainobjects.ProtectionElement;
import gov.nih.nci.security.authorization.domainobjects.ProtectionGroup;
import gov.nih.nci.security.authorization.domainobjects.User;
import gov.nih.nci.security.util.ObjectSetUtil;

/**
 * @author Padmaja Vedula
 * @author Rhett Sutphin
 */
@Transactional
public class TemplateService {
    public static final String PARTICIPANT_COORDINATOR_ACCESS_ROLE = "PARTICIPANT_COORDINATOR";
    public static final String PARTICIPANT_COORDINATOR_GROUP = "PARTICIPANT_COORDINATOR";
    private StudyCalendarAuthorizationManager authorizationManager;
    private StudyDao studyDao;
    private SiteDao siteDao;
    private StudySiteDao studySiteDao;
    private SiteService siteService;

    public void assignTemplateToSites(Study studyTemplate, List<Site> sites) throws Exception {
        for (Site site : sites) {
            StudySite ss = new StudySite();
            ss.setStudy(studyTemplate);
            ss.setSite(site);
            studySiteDao.save(ss);
        }
    }
    
    public void assignTemplateToParticipantCds(Study studyTemplate, Site site, List<String> assignedUserIds, List<String> availableUserIds) throws Exception {
        List<StudySite> studySites = studyTemplate.getStudySites();
        for (StudySite studySite : studySites) {
            if (studySite.getSite().getId().intValue() == site.getId().intValue()) {
                String studySitePGName = DomainObjectTools.createExternalObjectId(studySite);
                authorizationManager.createAndAssignPGToUser(assignedUserIds, studySitePGName, PARTICIPANT_COORDINATOR_ACCESS_ROLE);
                ProtectionGroup studySitePG = authorizationManager.getPGByName(studySitePGName);
                authorizationManager.removeProtectionGroupUsers(availableUserIds, studySitePG);
            }
        }
    }
    
    public void removeTemplateFromSites(Study studyTemplate, List<Site> sites) {
        List<StudySite> studySites = studyTemplate.getStudySites();
        List<StudySite> toRemove = new LinkedList<StudySite>();
        List<Site> cannotRemove = new LinkedList<Site>();
        for (Site site : sites) {
            for (StudySite studySite : studySites) {
                if (studySite.getSite().equals(site)) {
                    if (studySite.isUsed()) {
                        cannotRemove.add(studySite.getSite());
                    } else {
                        try {
                            authorizationManager.removeProtectionGroup(DomainObjectTools.createExternalObjectId(studySite));
                        } catch (RuntimeException e) {
                            throw e;
                        } catch (Exception e) {
                            throw new StudyCalendarSystemException(e);
                        }
                        toRemove.add(studySite);
                    }
                }
            }
        }
        for (StudySite studySite : toRemove) {
            Site siteAssoc = studySite.getSite();
            siteAssoc.getStudySites().remove(studySite);
            siteDao.save(siteAssoc);
            Study studyAssoc = studySite.getStudy();
            studyAssoc.getStudySites().remove(studySite);
            studyDao.save(studyAssoc);
        }
        if (cannotRemove.size() > 0) {
            StringBuilder msg = new StringBuilder("Cannot remove ")
                .append(StringUtils.pluralize(cannotRemove.size(), "site"))
                .append(" (");
            for (Iterator<Site> it = cannotRemove.iterator(); it.hasNext();) {
                Site site = it.next();
                msg.append(site.getName());
                if (it.hasNext()) msg.append(", ");
            }
            msg.append(") from study ").append(studyTemplate.getName())
                .append(" because there are participant(s) assigned");
            throw new StudyCalendarValidationException(msg.toString());
        }
    }
    
    public void assignMultipleTemplates(List<Study> studyTemplates, Site site, String userId) throws Exception {
        List<String> assignedUserIds = new ArrayList<String>();

        assignedUserIds.add(userId);

        for (Study template : studyTemplates) {
            List<StudySite> studySites = template.getStudySites();
            for (StudySite studySite : studySites) {
                if (studySite.getSite().getId().intValue() == site.getId().intValue()) {
                    String studySitePGName = DomainObjectTools.createExternalObjectId(studySite);
                    authorizationManager.createAndAssignPGToUser(assignedUserIds, studySitePGName, PARTICIPANT_COORDINATOR_ACCESS_ROLE);
                }
            }
        }
    }
    
    public Map getParticipantCoordinators(Study studyTemplate, Site site) throws Exception {
        Map<String, List> pcdMap = new HashMap<String, List>();
        List<StudySite> studySites = studyTemplate.getStudySites();
        for (StudySite studySite : studySites) {
            if (studySite.getSite().getId().intValue() == site.getId().intValue()) {
                pcdMap = authorizationManager.getUsers(PARTICIPANT_COORDINATOR_GROUP, DomainObjectTools.createExternalObjectId(studySite), site.getName());
            }
        }
        return pcdMap;
    }
    

    public Map getSiteLists(Study studyTemplate) throws Exception {
        Map<String, List> siteLists = new HashMap<String, List>();
        List<Site> availableSites = new ArrayList<Site>();
        List<Site> assignedSites = new ArrayList<Site>();
        List<ProtectionGroup> allSitePGs = authorizationManager.getSites();
        for (ProtectionGroup site : allSitePGs) {
            availableSites.add(siteDao.getByName(site.getProtectionGroupName()));
        }
        for (StudySite ss : studyTemplate.getStudySites()) {
            assignedSites.add(ss.getSite());
        }
        availableSites = (List) ObjectSetUtil.minus(availableSites, assignedSites);
        siteLists.put(StudyCalendarAuthorizationManager.ASSIGNED_PGS, assignedSites);
        siteLists.put(StudyCalendarAuthorizationManager.AVAILABLE_PGS, availableSites);

        return siteLists;
    }
    
    public Map getTemplatesLists(Site site, User participantCdUser) throws Exception {
        Map<String, List> templatesMap = new HashMap<String, List>();
        List<Study> assignedTemplates = new ArrayList<Study>();
        List<Study> availableTemplates = new ArrayList<Study>();
        List<Study> allTemplates = new ArrayList<Study>();

        List<StudySite> studySites = site.getStudySites();
        for (StudySite studySite : studySites) {
            allTemplates.add(studySite.getStudy());
            if (authorizationManager.isUserPGAssigned(DomainObjectTools.createExternalObjectId(studySite), participantCdUser.getUserId().toString())) {
                assignedTemplates.add(studySite.getStudy());
            }
        }

        availableTemplates = (List) ObjectSetUtil.minus(allTemplates, assignedTemplates);
        templatesMap.put(StudyCalendarAuthorizationManager.ASSIGNED_PES, assignedTemplates);
        templatesMap.put(StudyCalendarAuthorizationManager.AVAILABLE_PES, availableTemplates);
        return templatesMap;
    }
    
    public ProtectionGroup getSiteProtectionGroup(String siteName) throws Exception {
        return authorizationManager.getPGByName(siteName);
    }
    
    public List checkOwnership(String userName, List<Study> studies) throws Exception {
        return authorizationManager.checkOwnership(userName, studies);
    }
    
    public List getSitesForTemplateSiteCd(String userName, Study study) {
        List<Site> sites = siteService.getSitesForSiteCd(userName);
        List<StudySite> allStudySites = study.getStudySites();
        List<Site> templateSites = new ArrayList<Site>();

        for (Site site : sites) {
            for (StudySite studySite : allStudySites) {
                if (studySite.getSite().getId() == site.getId()) {
                    templateSites.add(site);
                }
            }
        }

        return templateSites;
    }
    
    
    public void removeMultipleTemplates(List<Study> studyTemplates, Site site, String userId) throws Exception {
        List<String> userIds = new ArrayList<String>();

        userIds.add(userId);

        for (Study template : studyTemplates) {
            List<StudySite> studySites = template.getStudySites();
            for (StudySite studySite : studySites) {
                if (studySite.getSite().getId().intValue() == site.getId().intValue()) {
                    String studySitePGName = DomainObjectTools.createExternalObjectId(studySite);
                    ProtectionGroup studySitePG = authorizationManager.getPGByName(studySitePGName);
                    authorizationManager.removeProtectionGroupUsers(userIds, studySitePG);
                }
            }
        }
    }
    
    ////// CONFIGURATION

    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }
    
    public void setSiteDao(SiteDao siteDao) {
        this.siteDao = siteDao;
    }

    public void setStudySiteDao(StudySiteDao studySiteDao) {
        this.studySiteDao = studySiteDao;
    }
    
    public void setStudyCalendarAuthorizationManager(StudyCalendarAuthorizationManager authorizationManager) {
        this.authorizationManager = authorizationManager;
    }

    @Required
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }
}
