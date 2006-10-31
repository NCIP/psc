package edu.northwestern.bioinformatics.studycalendar.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.propertyeditors.CustomCollectionEditor;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarAuthorizationManager;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;
import gov.nih.nci.security.AuthenticationManager;
import gov.nih.nci.security.authorization.domainobjects.User;
import gov.nih.nci.security.UserProvisioningManager;
import gov.nih.nci.security.authorization.domainobjects.ProtectionGroup;
import org.apache.log4j.Logger;

/**
 * @author Yufang Wang
 */
@AccessControl(protectionGroups = StudyCalendarProtectionGroup.STUDY_ADMINISTRATOR)
public class AssignTemplatesToOneParticipantCoordinatorController extends SimpleFormController {
	private SiteDao siteDao;
	private SiteService siteService;
	private TemplateService templateService;
	private StudyCalendarAuthorizationManager authorizationManager;
	private static final Logger log = Logger.getLogger(AssignTemplatesToOneParticipantCoordinatorController.class.getName());
	

    public AssignTemplatesToOneParticipantCoordinatorController() {
        setCommandClass(AssignTemplatesToOneParticipantCoordinatorCommand.class);
        setFormView("assignTemplatesToOneParticipantCoordinator");
        setSuccessView("assignTemplatesToOneParticipantCoordinator");
    }
    
    protected Map<String, Object> referenceData(HttpServletRequest httpServletRequest) throws Exception {
        log.debug("referenceData"); 
        
        Map<String, Object> refdata = new HashMap<String, Object>();
        Site site= siteDao.getById(ServletRequestUtils.getRequiredIntParameter(httpServletRequest, "siteId"));
        refdata.put("site", site);
        
        ProtectionGroup sitePG = siteService.getSiteProtectionGroup(site.getName());
        Long pcid = ServletRequestUtils.getRequiredLongParameter(httpServletRequest, "pcId");
        String participantcoordinatorId = pcid.toString();
        User participantcoordinator = authorizationManager.getUserObject(participantcoordinatorId);
        refdata.put("participantcoordinator", participantcoordinator);
        
        
        Map<String, List> templateLists = new HashMap<String, List>();
        templateLists = templateService.getTemplatesLists(sitePG.getProtectionGroupId().toString(), participantcoordinatorId); 
        
        log.debug("---+++sitePGid=" + sitePG.getProtectionGroupId().toString() + " pcuserid=" + participantcoordinatorId);
        List<Study> availableTplt0 = (List<Study>)templateLists.get(sitePG.getProtectionGroupId().toString());
        if(availableTplt0 != null) {
        	Iterator avi0 = availableTplt0.iterator();
        	while(avi0.hasNext()) {
        		Study avtp = (Study)avi0.next();
        		log.debug("---+++ availabletpname=" + avtp.getName() + "tpid=" + avtp.getId());
        	}
        } else {
       		log.debug("---+++ cannot get availableTplt0");
		}        
        
        refdata.put("assignedTemplates", templateLists.get(StudyCalendarAuthorizationManager.ASSIGNED_PES));
        refdata.put("availableTemplates", templateLists.get(StudyCalendarAuthorizationManager.AVAILABLE_PES));
        
        List<Study> assignTplt = (List<Study>)refdata.get("assignedTemplates");
        if(assignTplt != null) {
        	Iterator asi = assignTplt.iterator();
        	while(asi.hasNext()) {
        		Study astp = (Study)asi.next();
        		log.debug("---+++ assignedtpname=" + astp.getName() + "tpid=" + astp.getId());
        	}
        } else {
        	log.debug("---+++ cannot get assignTplt");
        }
        List<Study> availableTplt = (List<Study>)refdata.get("availableTemplates");
        if(availableTplt != null) {
        	Iterator avi = availableTplt.iterator();
        	while(avi.hasNext()) {
        		Study avtp = (Study)avi.next();
        		log.debug("---+++ availabletpname=" + avtp.getName() + "tpid=" + avtp.getId());
        	}
        } else {
        	log.debug("---+++ cannot get availableTplt");
        }
        
        refdata.put("action", "Assign");
        return refdata;
    }

    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {
    	AssignTemplatesToOneParticipantCoordinatorCommand assignCommand = (AssignTemplatesToOneParticipantCoordinatorCommand) oCommand;
    	
    	log.debug("++++++ assignCommand.getParticipantCoordinatorUserId()=" + assignCommand.getParticipantCoordinatorUserId());
        if("true".equals(assignCommand.getAssign())) {
        	templateService.assignMultipleTemplates(assignCommand.getAvailableTemplates(), assignCommand.getParticipantCoordinatorUserId());
    	} else {
    		//templateService.remove
    	}
    	
        return new ModelAndView(new RedirectView(getSuccessView()), "pcId", ServletRequestUtils.getLongParameter(request, "pcId"));
    }

    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        //log.debug("formBackingObject");
    	AssignTemplatesToOneParticipantCoordinatorCommand command = new AssignTemplatesToOneParticipantCoordinatorCommand();
        return command;
    }


    ////// CONFIGURATION
    @Required
    public void setSiteDao(SiteDao siteDao) {
        this.siteDao = siteDao;
    }

    
    @Required
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }
    
     
    @Required
    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }
    
	@Required
    public void setStudyCalendarAuthorizationManager(StudyCalendarAuthorizationManager atm) {
        this.authorizationManager = atm;
    }
    
	
}
