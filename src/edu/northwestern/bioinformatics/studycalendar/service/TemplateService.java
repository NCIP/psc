package edu.northwestern.bioinformatics.studycalendar.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarAuthorizationManager;
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
    		studyPEs.add(template.getClass().getName()+"."+template.getId());
		}
    	authorizationManager.assignMultipleProtectionElements(userId, studyPEs);
		
    }
    
    public Map getParticipantCoordinators(Study studyTemplate) throws Exception {
    	return authorizationManager.getUsers(PARTICIPANT_COORDINATOR_GROUP, studyTemplate.getClass().getName()+"."+studyTemplate.getId());
    }
    

    public Map getSiteLists(Study studyTemplate) throws Exception {
    	List<ProtectionGroup> allSites = authorizationManager.getSites();
    	return authorizationManager.getProtectionGroups(allSites, studyTemplate.getClass().getName()+"."+studyTemplate.getId());
    }
    
    public Map getTemplatesLists(String siteId, String participantCdId) throws Exception {
    	return authorizationManager.getPEForUserProtectionGroup(siteId, participantCdId);
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

   
     public void setStudyCalendarAuthorizationManager(StudyCalendarAuthorizationManager authorizationManager) {
        this.authorizationManager = authorizationManager;
    }
}
