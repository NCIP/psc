package edu.northwestern.bioinformatics.studycalendar.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarAuthorizationManager;

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
    
    public void assignMultipleTemplates(List<Study> studyTemplates, String userId) throws Exception {
    	List<String> studyPEs = new ArrayList<String>();
    	for (Study template : studyTemplates)
		{
    		studyPEs.add(template.getClass().getName()+"."+template.getId());
		}
    	authorizationManager.assignMultipleProtectionElements(userId, studyPEs);
		
    }
    
    ////// CONFIGURATION

   
     public void setStudyCalendarAuthorizationManager(StudyCalendarAuthorizationManager authorizationManager) {
        this.authorizationManager = authorizationManager;
    }
}
