package edu.northwestern.bioinformatics.studycalendar.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.utils.DomainObjectTools;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarAuthorizationManager;
import gov.nih.nci.security.authorization.domainobjects.ProtectionElement;
import gov.nih.nci.security.authorization.domainobjects.ProtectionGroup;
import gov.nih.nci.security.authorization.domainobjects.User;

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

   
    public void assignTemplateToSites(Study studyTemplate, List<String> siteIds) throws Exception {
    	authorizationManager.assignProtectionElementToPGs(siteIds, studyTemplate.getClass().getName()+"."+studyTemplate.getId());
    }
    
    public void assignTemplateToParticipantCds(Study studyTemplate, List<String> userIds) throws Exception {
    	authorizationManager.assignProtectionElementsToUsers(userIds, studyTemplate.getClass().getName()+"."+studyTemplate.getId());
    }
    
    public void removeTemplateFromSites(Study studyTemplate, List<String> siteIds) throws Exception {
    	authorizationManager.removeProtectionElementFromPGs(siteIds, studyTemplate.getClass().getName()+"."+studyTemplate.getId());
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
    	return authorizationManager.getUsers(PARTICIPANT_COORDINATOR_GROUP, DomainObjectTools.createExternalObjectId(studyTemplate), site.getName());
    }
    

    public Map getSiteLists(Study studyTemplate) throws Exception {
    	List<ProtectionGroup> allSites = authorizationManager.getSites();
    	return authorizationManager.getProtectionGroups(allSites, studyTemplate.getClass().getName()+"."+studyTemplate.getId());
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
    	return authorizationManager.getSite(siteName);
    }
    
    public List getAllSiteProtectionGroups() throws Exception {
    	return authorizationManager.getSites();
    }
    
    public List checkOwnership(String userName, List<Study> studies) throws Exception {
    	return authorizationManager.checkOwnership(userName, studies);
    }
    
      ////// CONFIGURATION

    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }
    
    public void setSiteDao(SiteDao siteDao) {
        this.siteDao = siteDao;
    }
      
    public void setStudyCalendarAuthorizationManager(StudyCalendarAuthorizationManager authorizationManager) {
        this.authorizationManager = authorizationManager;
    }
}
