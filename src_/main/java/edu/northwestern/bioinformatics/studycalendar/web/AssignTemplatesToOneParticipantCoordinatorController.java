package edu.northwestern.bioinformatics.studycalendar.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarAuthorizationManager;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;
import gov.nih.nci.security.authorization.domainobjects.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Yufang Wang
 */
@AccessControl(roles = Role.SITE_COORDINATOR)
public class AssignTemplatesToOneParticipantCoordinatorController extends PscSimpleFormController {
    private int siteId;
    private String pcId;
    private Site site;
    private SiteDao siteDao;
    private StudyDao studyDao;
    private TemplateService templateService;
    private StudyCalendarAuthorizationManager authorizationManager;
    private static final Logger log = LoggerFactory.getLogger(AssignTemplatesToOneParticipantCoordinatorController.class.getName());

    public AssignTemplatesToOneParticipantCoordinatorController() {
        setCommandClass(AssignTemplatesToOneParticipantCoordinatorCommand.class);
        setFormView("assignTemplatesToOneParticipantCoordinator");
        setSuccessView("assignTemplatesToOneParticipantCoordinator");
    }
    
    public void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) {
        getControllerTools().registerDomainObjectEditor(binder, "availableTemplates", studyDao);
        getControllerTools().registerDomainObjectEditor(binder, "assignedTemplates", studyDao);
    }
    
    protected Map<String, Object> referenceData(HttpServletRequest httpServletRequest) throws Exception {
        log.debug("referenceData"); 
        String stemp = ServletRequestUtils.getRequiredStringParameter(httpServletRequest, "siteId");
        siteId = Integer.valueOf( stemp.split("\\.")[0] ).intValue();
        pcId = stemp.split("\\.")[1];
        Map<String, Object> refdata = new HashMap<String, Object>();
        
        site= siteDao.getById(siteId);
        refdata.put("site", site);
        
        User participantcoordinator = authorizationManager.getUserObject(pcId);
        refdata.put("participantcoordinator", participantcoordinator);

        Map<String, List> templateLists = new HashMap<String, List>();
        templateLists = templateService.getTemplatesLists(site, participantcoordinator); 
        refdata.put("assignedTemplates", templateLists.get(StudyCalendarAuthorizationManager.ASSIGNED_PES));
        refdata.put("availableTemplates", templateLists.get(StudyCalendarAuthorizationManager.AVAILABLE_PES));
        
        refdata.put("action", "Assign");
        return refdata;
    }

    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {
    	AssignTemplatesToOneParticipantCoordinatorCommand assignCommand = (AssignTemplatesToOneParticipantCoordinatorCommand) oCommand;
    	
        if("true".equals(assignCommand.getAssign())) {
        	templateService.assignMultipleTemplates(assignCommand.getAvailableTemplates(), site, pcId);
        } else {
        	templateService.removeMultipleTemplates(assignCommand.getAssignedTemplates(), site, pcId);
    	}
    	
        return new ModelAndView(new RedirectView(getSuccessView()), "siteId", request.getParameter("siteId"));
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
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
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
