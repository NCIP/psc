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
import gov.nih.nci.security.AuthenticationManager;
import gov.nih.nci.security.authorization.domainobjects.User;
import gov.nih.nci.security.UserProvisioningManager;
import gov.nih.nci.security.authorization.domainobjects.User;
import gov.nih.nci.security.authorization.domainobjects.ProtectionGroup;
import org.apache.log4j.Logger;

/**
 * @author Jaron Sampson
 * @author Yufang Wang
 */

public class AssignSiteCoordinatorController extends SimpleFormController {
	private static final String GROUP_NAME = "SITE_COORDINATOR";
	private SiteDao siteDao;
	private SiteService siteService;
    private static final Logger log = Logger.getLogger(AssignSiteCoordinatorController.class.getName());

    public AssignSiteCoordinatorController() {
        setCommandClass(AssignSiteCoordinatorCommand.class);
        setFormView("assignSiteCoordinator");
        setSuccessView("manageSites");
    }
    
/*    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        binder.registerCustomEditor(Set.class, "assignedCoordinators", new CustomCollectionEditor(Set.class));
        binder.registerCustomEditor(Set.class, "availableCoordinators", new CustomCollectionEditor(Set.class));
    } 
*/
    protected Map<String, Object> referenceData(HttpServletRequest httpServletRequest) throws Exception {
        log.debug("referenceData");
        Map<String, Object> refdata = new HashMap<String, Object>();
        Site site= siteDao.getById(ServletRequestUtils.getRequiredIntParameter(httpServletRequest, "id"));
        Map<String, List> userLists = siteService.getSiteCoordinatorLists(site.getClass().getName()+"."+site.getId());
        refdata.put("site", site);
        refdata.put("assignedUsers", userLists.get(SiteService.ASSIGNED_USERS));
        refdata.put("availableUsers", userLists.get(SiteService.AVAILABLE_USERS));
        refdata.put("action", "Assign");
        return refdata;
    }

    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {
    	AssignSiteCoordinatorCommand assignCommand = (AssignSiteCoordinatorCommand) oCommand;
    	Site assignedSite = siteDao.getById(assignCommand.getSiteId());
    	
        if("true".equals(assignCommand.getAssign())) {
    		
            log.debug("onSubmit:assign" + " siteId=" + assignedSite.getClass().getName()+"."+assignedSite.getId().toString());
            String ac = assignCommand.getAvailableCoordinators().toString();
            log.debug(ac);
        	log.debug("+++ available coordinators size=" + assignCommand.getAvailableCoordinators().size());
            if(assignCommand.getAvailableCoordinators().size()>0) {
            	for(int i=0; i<assignCommand.getAvailableCoordinators().size(); ++i) {
            		log.debug("+++ available coordinators i=" + i + ", " + assignCommand.getAvailableCoordinators().get(i));
            	}
            }
            ProtectionGroup pg = siteService.getSiteProtectionGroup(assignedSite.getClass().getName());
            
            siteService.assignSiteCoordinators(pg, assignCommand.getAvailableCoordinators());
        } else {
            log.debug("onSubmit:remove");
            /*
            if(assignCommand.getAvailableCoordinators().size()>0) {
                log.debug("--- available coordinators size=" + assignCommand.getAvailableCoordinators().size());
            	for(int i=0; i<assignCommand.getAvailableCoordinators().size(); ++i) {
            		log.debug("--- available coordinators i=" + i + ", " + assignCommand.getAvailableCoordinators().get(i));
            	}
            }
           
            if(assignCommand.getAssignedCoordinators().size()>0) {
            	log.debug("--- assigned coordinators size=" + assignCommand.getAssignedCoordinators().size());
            	for(int i=0; i<assignCommand.getAssignedCoordinators().size(); ++i) {
            		log.debug("--- assigned coordinators i=" + i + ", " + assignCommand.getAssignedCoordinators().get(i));
            	}
            }
            if(assignCommand.getAvailableCoordinators().size()>0) { 
            
            	authorizationManager.removeProtectionGroupUsers(assignedSite.getClass().getName()+"."+assignedSite.getId(), assignCommand.getAssignedCoordinators());	
            
            	//}
            UserProvisioningManager provisioningManager = null;
        	provisioningManager = authorizationManager.getProvisioningManager();
            for (String siteCoor : assignCommand.getAssignedCoordinators())
        	{
        		provisioningManager.removeUserFromProtectionGroup(assignedSite.getClass().getName()+"."+assignedSite.getId(), siteCoor);
        	}
            */
           
        	
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
