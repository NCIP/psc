package edu.northwestern.bioinformatics.studycalendar.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.utils.DomainObjectTools;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarAuthorizationManager;
import gov.nih.nci.security.authorization.domainobjects.ProtectionElement;
import gov.nih.nci.security.authorization.domainobjects.ProtectionGroup;
import gov.nih.nci.security.authorization.domainobjects.User;
import gov.nih.nci.security.util.ObjectSetUtil;

/**
 * @author Padmaja Vedula
 */

@Transactional
public class TemplateService {
	public static final String PARTICIPANT_COORDINATOR_ACCESS_ROLE = "PARTICIPANT_COORDINATOR";
	public static final String PARTICIPANT_COORDINATOR_GROUP = "PARTICIPANT_COORDINATOR";
    private StudyCalendarAuthorizationManager authorizationManager;
    private StudyDao studyDao;
    private SiteDao siteDao;
    private StudySiteDao studySiteDao;

   
    public void assignTemplateToSites(Study studyTemplate, List<Site> sites) throws Exception {
    	for (Site site : sites) {
    		StudySite ss = new StudySite();
            ss.setStudy(studyTemplate);
            ss.setSite(site);
            studySiteDao.save(ss);
    	}
     
    	//authorizationManager.assignProtectionElementToPGs(, studyTemplate.getClass().getName()+"."+studyTemplate.getId());
    }
    
    /*public void assignTemplateToSitesPGs(Study studyTemplate, List<Sites> sites) throws Exception {
    	authorizationManager.assignProtectionElementToPGs(siteIds, studyTemplate.getClass().getName()+"."+studyTemplate.getId());
    }*/
    
    public void assignTemplateToParticipantCds(Study studyTemplate, Site site, List<String> assignedUserIds, List<String> availableUserIds) throws Exception {
    	List<StudySite> studySites = studyTemplate.getStudySites();
    	Integer requiredStudySite;
    	for (StudySite studySite : studySites) {
			if (studySite.getSite().getId().intValue() == site.getId().intValue()) {
				String studySitePGName = DomainObjectTools.createExternalObjectId(studySite);
				authorizationManager.createAndAssignPGToUser(assignedUserIds, studySitePGName, PARTICIPANT_COORDINATOR_ACCESS_ROLE);
				ProtectionGroup studySitePG = authorizationManager.getPGByName(studySitePGName);
				authorizationManager.removeProtectionGroupUsers(availableUserIds, studySitePG);
			}
		}
    	
    }
    
    public void removeTemplateFromSites(Study studyTemplate, List<Site> sites) throws Exception {
    	List<StudySite> studySites = studyTemplate.getStudySites();
    	List<StudySite> removeStudySiteList = new ArrayList<StudySite>();
    	for (Site site : sites) {
    		for (StudySite studySite : studySites) {
    			if (studySite.getSite().getId() == site.getId()) {
    				authorizationManager.removeProtectionGroup(DomainObjectTools.createExternalObjectId(studySite));
    				removeStudySiteList.add(studySite);
    			}
    		}
    		for (StudySite studySite : removeStudySiteList) {
    			Site siteAssoc = studySite.getSite();
    			siteAssoc.getStudySites().remove(studySite);
				siteDao.save(siteAssoc);
				Study studyAssoc = studySite.getStudy();
				studyAssoc.getStudySites().remove(studySite);
				studyDao.save(studyAssoc);
    		}
    		
    	}
    	
    }
    
    public void assignMultipleTemplates(List<Study> studyTemplates, String userId) throws Exception {
    	List<String> studyPEs = new ArrayList<String>();
    	for (Study template : studyTemplates)
		{
    		//studyPEs.add(template.getClass().getName()+"."+template.getId());
    		studyPEs.add(DomainObjectTools.createExternalObjectId(template));
    		
		}
    	authorizationManager.assignMultipleProtectionElements(userId, studyPEs);
		
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
    	siteLists.put(authorizationManager.ASSIGNED_PGS, assignedSites);
    	siteLists.put(authorizationManager.AVAILABLE_PGS, availableSites);
    	
    	return siteLists;
    }
    
    public Map getTemplatesLists(Site site, User participantCdUser) throws Exception {
    	List<Study> availableTemplates = new ArrayList<Study>();
    	List<Study> assignedTemplates = new ArrayList<Study>();
    	Map<String, List> templatesMap = authorizationManager.getPEForUserProtectionGroup(site.getName(), participantCdUser.getUserId().toString());
    	List<ProtectionElement> availablePEs = (List) templatesMap.get(authorizationManager.AVAILABLE_PES);
    	List<ProtectionElement> assignedPEs = (List) templatesMap.get(authorizationManager.ASSIGNED_PES);
    	for (ProtectionElement available : availablePEs) {
    		int id = DomainObjectTools.parseExternalObjectId(available.getObjectId());
    		availableTemplates.add(studyDao.getById(id));
    	}
    	for (ProtectionElement assigned : assignedPEs) {
    		int id = DomainObjectTools.parseExternalObjectId(assigned.getObjectId());
    		assignedTemplates.add(studyDao.getById(id));
    	}
    	templatesMap.put(authorizationManager.ASSIGNED_PES, assignedTemplates);
    	templatesMap.put(authorizationManager.AVAILABLE_PES, availableTemplates);
    	
    	return templatesMap;
    }
    
    public ProtectionGroup getSiteProtectionGroup(String siteName) throws Exception {
    	return authorizationManager.getPGByName(siteName);
    }
    
    public List getAllSiteProtectionGroups() throws Exception {
    	return authorizationManager.getSites();
    }
    
    public List checkOwnership(String userName, List<Study> studies) throws Exception {
    	return authorizationManager.checkOwnership(userName, studies);
    }
    
    public List getSitesForTemplateSiteCd(String userName, Study study) throws Exception {
    	List<Site> sites = new ArrayList<Site>();
    	List<StudySite> allStudySites = study.getStudySites();
    	List<Site> templateSites = new ArrayList<Site>();
    	
    	List<ProtectionGroup> sitePGs = authorizationManager.getSitePGsForUser(userName);

    	for (ProtectionGroup sitePG : sitePGs) {
    		sites.add(siteDao.getByName(sitePG.getProtectionGroupName()));
    	}
    	for (Site site : sites) {
    		for (StudySite studySite : allStudySites) {
    			if (studySite.getSite().getId() == site.getId()) {
    				templateSites.add(site);
    			}
    		}
    	}
    	
    	return templateSites;
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
}
