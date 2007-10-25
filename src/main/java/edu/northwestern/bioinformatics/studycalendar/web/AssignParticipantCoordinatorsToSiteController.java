package edu.northwestern.bioinformatics.studycalendar.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Yufang Wang
 */
@AccessControl(roles = Role.SITE_COORDINATOR)
public class AssignParticipantCoordinatorsToSiteController extends PscSimpleFormController {
	private static final String GROUP_NAME = "PARTICIPANT_COORDINATOR";
	private SiteDao siteDao;
	private SiteService siteService;
    private static final Logger log = LoggerFactory.getLogger(AssignParticipantCoordinatorsToSiteController.class.getName());

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
            siteService.assignParticipantCoordinatorsInCsm(assignedSite, assignCommand.getAvailableCoordinators());
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
