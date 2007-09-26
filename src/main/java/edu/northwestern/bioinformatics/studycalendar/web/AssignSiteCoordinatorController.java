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
 * @author Jaron Sampson
 * @author Yufang Wang
 */
@AccessControl(roles = Role.STUDY_ADMIN)
public class AssignSiteCoordinatorController extends PscSimpleFormController {
	private static final String GROUP_NAME = "SITE_COORDINATOR";
	private SiteDao siteDao;
	private SiteService siteService;
    private static final Logger log = LoggerFactory.getLogger(AssignSiteCoordinatorController.class.getName());

    public AssignSiteCoordinatorController() {
        setCommandClass(AssignSiteCoordinatorCommand.class);
        setFormView("assignSiteCoordinator");
        setSuccessView("assignSiteCoordinator");
    }
    
    protected Map<String, Object> referenceData(HttpServletRequest httpServletRequest) throws Exception {
        log.debug("referenceData");
        Map<String, Object> refdata = new HashMap<String, Object>();
        Site site= siteDao.getById(ServletRequestUtils.getRequiredIntParameter(httpServletRequest, "id"));
        Map<String, List> userLists = siteService.getSiteCoordinatorLists(site);
        
        refdata.put("site", site);
        refdata.put("assignedUsers", userLists.get(SiteService.ASSIGNED_USERS));
        refdata.put("availableUsers", userLists.get(SiteService.AVAILABLE_USERS));
        refdata.put("action", "Assign");
        return refdata;
    }

    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {
    	AssignSiteCoordinatorCommand assignCommand = (AssignSiteCoordinatorCommand) oCommand;
    	Site assignedSite = siteDao.getById(assignCommand.getSiteId());
    	//ProtectionGroup sitepg = siteService.getSiteProtectionGroup(assignedSite.getName()); 
    	
        if("true".equals(assignCommand.getAssign())) {
    		
            log.debug("onSubmit:assign" + " siteId=" + assignedSite.getName());
            String ac = assignCommand.getAvailableCoordinators().toString();
            log.debug(ac);
        	log.debug("+++ available coordinators size=" + assignCommand.getAvailableCoordinators().size());
            if(assignCommand.getAvailableCoordinators().size()>0) {
            	for(int i=0; i<assignCommand.getAvailableCoordinators().size(); ++i) {
            		log.debug("+++ available coordinators i=" + i + ", " + assignCommand.getAvailableCoordinators().get(i));
            	}
            }
            
            siteService.assignSiteCoordinators(assignedSite, assignCommand.getAvailableCoordinators());
            
        } else {
            log.debug("onSubmit:remove");
             
            siteService.removeSiteCoordinators(assignedSite, assignCommand.getAssignedCoordinators());
    	}

        return new ModelAndView(new RedirectView(getSuccessView()), "id", ServletRequestUtils.getIntParameter(request, "id"));
    }

    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        log.debug("formBackingObject");
    	AssignSiteCoordinatorCommand command = new AssignSiteCoordinatorCommand();
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
