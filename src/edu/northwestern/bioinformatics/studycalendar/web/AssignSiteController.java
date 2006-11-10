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
import org.springframework.web.servlet.mvc.SimpleFormController;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarAuthorizationManager;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;

/**
 * @author Padmaja Vedula
 */
@AccessControl(protectionGroups = StudyCalendarProtectionGroup.STUDY_ADMINISTRATOR)
public class AssignSiteController extends PscSimpleFormController {
	private TemplateService templateService;
	private StudyDao studyDao;
	private SiteDao siteDao;
	
    public AssignSiteController() {
        setCommandClass(AssignSiteCommand.class);
        setFormView("assignSite");
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        ControllerTools.registerDomainObjectEditor(binder, "assignedSites", siteDao);
        ControllerTools.registerDomainObjectEditor(binder, "availableSites", siteDao);
    } 

    protected Map<String, Object> referenceData(HttpServletRequest httpServletRequest) throws Exception {
        Map<String, Object> refdata = new HashMap<String, Object>();
        Study study = studyDao.getById(ServletRequestUtils.getRequiredIntParameter(httpServletRequest, "id"));
        Map<String, List> userLists = templateService.getSiteLists(study);
        refdata.put("study", study);
        refdata.put("assignedSites", userLists.get(StudyCalendarAuthorizationManager.ASSIGNED_PGS));
        refdata.put("availableSites", userLists.get(StudyCalendarAuthorizationManager.AVAILABLE_PGS));
        refdata.put("action", "Assign");
        return refdata;
    }

    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {
    	AssignSiteCommand assignCommand = (AssignSiteCommand) oCommand;
    	Study assignedStudy = studyDao.getById(assignCommand.getStudyId());
    	 if("true".equals(assignCommand.getAssign())) {
    		templateService.assignTemplateToSites(assignedStudy, assignCommand.getAvailableSites());
    	 } else {
    		templateService.removeTemplateFromSites(assignedStudy, assignCommand.getAssignedSites());
    	 }
         return ControllerTools.redirectToCalendarTemplate(ServletRequestUtils.getIntParameter(request, "id"));
    }

    protected Object formBackingObject(HttpServletRequest request) throws Exception {
    	AssignSiteCommand command = new AssignSiteCommand();
        return command;
    }

    ////// CONFIGURATION

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }
    
    @Required
    public void setSiteDao(SiteDao siteDao) {
        this.siteDao = siteDao;
    }
    
    @Required
    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

}
