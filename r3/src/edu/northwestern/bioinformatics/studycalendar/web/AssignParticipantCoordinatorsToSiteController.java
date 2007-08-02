package edu.northwestern.bioinformatics.studycalendar.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;
import gov.nih.nci.security.AuthenticationManager;
import gov.nih.nci.security.authorization.domainobjects.User;
import gov.nih.nci.security.UserProvisioningManager;
import gov.nih.nci.security.authorization.domainobjects.User;
import gov.nih.nci.security.authorization.domainobjects.ProtectionGroup;
import org.apache.log4j.Logger;

/**
 * @author Yufang Wang
 */
@AccessControl(protectionGroups = StudyCalendarProtectionGroup.SITE_COORDINATOR)
public class AssignParticipantCoordinatorsToSiteController extends SimpleFormController {
	private static final String GROUP_NAME = "PARTICIPANT_COORDINATOR";
	private SiteDao siteDao;
	private SiteService siteService;
    private static final Logger log = Logger.getLogger(AssignParticipantCoordinatorsToSiteController.class.getName());

    public AssignParticipantCoordinatorsToSiteController() {
        setCommandClass(AssignParticipantCoordinatorsToSiteCommand.class);
        setFormView("assignParticipantCoordinatorsToSite");
        setSuccessView("assignParticipantCoordinatorsToSite");
    }
    
    protected Map<String, Object> referenceData(HttpServletRequest httpServletRequest) throws Exception {
        log.debug("referenceData");
        Map<String, Object> refdata = new HashMap<String, Object>();
        Site site= siteDao.getById(ServletRequestUtils.getRequiredIntParameter(httpServletRequest, "id"));
        Map<String, List> userLists = siteService.getParticipantCoordinatorLists(site);
        
        refdata.put("site", site);
        refdata.put("assignedUsers", userLists.get(SiteService.ASSIGNED_USERS));
        refdata.put("availableUsers", userLists.get(SiteService.AVAILABLE_USERS));
        refdata.put("action", "Assign");
        return refdata;
    }

    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {
    	AssignParticipantCoordinatorsToSiteCommand assignCommand = (AssignParticipantCoordinatorsToSiteCommand) oCommand;
    	Site assignedSite = siteDao.getById(assignCommand.getSiteId());
    	//ProtectionGroup sitePG = siteService.getSiteProtectionGroup(assignedSite.getName()); 
    	
        if("true".equals(assignCommand.getAssign())) {   
            siteService.assignParticipantCoordinators(assignedSite, assignCommand.getAvailableCoordinators());
        } else {
            log.debug("onSubmit:remove");
             
            siteService.removeParticipantCoordinators(assignedSite, assignCommand.getAssignedCoordinators());
    	}

        return new ModelAndView(new RedirectView(getSuccessView()), "id", ServletRequestUtils.getIntParameter(request, "id"));
    }

    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        log.debug("formBackingObject");
    	AssignParticipantCoordinatorsToSiteCommand command = new AssignParticipantCoordinatorsToSiteCommand();
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

}
