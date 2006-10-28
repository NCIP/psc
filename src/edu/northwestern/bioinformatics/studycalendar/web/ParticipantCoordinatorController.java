package edu.northwestern.bioinformatics.studycalendar.web;

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

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarAuthorizationManager;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;

/**
 * @author Padmaja Vedula
 */
@AccessControl(protectionGroups = StudyCalendarProtectionGroup.SITE_COORDINATOR)
public class ParticipantCoordinatorController extends SimpleFormController {
	private TemplateService templateService;
	private StudyDao studyDao;
	
    public ParticipantCoordinatorController() {
        setCommandClass(ParticipantCoordinatorCommand.class);
        setFormView("assignParticipantCoordinator");
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        binder.registerCustomEditor(Set.class, "assignedCoordinators", new CustomCollectionEditor(Set.class));
        binder.registerCustomEditor(Set.class, "availableCoordinators", new CustomCollectionEditor(Set.class));
    } 

    protected Map<String, Object> referenceData(HttpServletRequest httpServletRequest) throws Exception {
        Map<String, Object> refdata = new HashMap<String, Object>();
        Study study = studyDao.getById(ServletRequestUtils.getRequiredIntParameter(httpServletRequest, "id"));
        Map<String, List> userLists = templateService.getParticipantCoordinators(study);
        refdata.put("study", study);
        refdata.put("assignedUsers", userLists.get(StudyCalendarAuthorizationManager.ASSIGNED_USERS));
        refdata.put("availableUsers", userLists.get(StudyCalendarAuthorizationManager.AVAILABLE_USERS));
        refdata.put("action", "Assign");
        return refdata;
    }

    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {
    	ParticipantCoordinatorCommand assignCommand = (ParticipantCoordinatorCommand) oCommand;
    	Study assignedStudy = studyDao.getById(assignCommand.getStudyId());
        templateService.assignTemplateToParticipantCds(assignedStudy, assignCommand.getAssignedCoordinators());

        return ControllerTools.redirectToCalendarTemplate(ServletRequestUtils.getIntParameter(request, "id"));
    }

    protected Object formBackingObject(HttpServletRequest request) throws Exception {
    	ParticipantCoordinatorCommand command = new ParticipantCoordinatorCommand();
        return command;
    }

    ////// CONFIGURATION

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }
    
    @Required
    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

}
