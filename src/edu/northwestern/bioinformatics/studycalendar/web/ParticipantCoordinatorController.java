package edu.northwestern.bioinformatics.studycalendar.web;

import java.util.ArrayList;
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

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarAuthorizationManager;
import gov.nih.nci.security.AuthenticationManager;

/**
 * @author Padmaja Vedula
 */
public class ParticipantCoordinatorController extends SimpleFormController {
	private static final String GROUP_NAME = "PARTICIPANT_COORDINATOR";
	private StudyDao studyDao;
	private StudyCalendarAuthorizationManager authorizationManager;

    public ParticipantCoordinatorController() {
        setCommandClass(ParticipantCoordinatorCommand.class);
        setFormView("assignParticipantCoordinator");
        setSuccessView("calendarTemplate");
    }

   
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        binder.registerCustomEditor(Set.class, "assignedCoordinators", new CustomCollectionEditor(Set.class));
        binder.registerCustomEditor(Set.class, "availableCoordinators", new CustomCollectionEditor(Set.class));
    } 

    protected Map<String, Object> referenceData(HttpServletRequest httpServletRequest) throws Exception {
        Map<String, Object> refdata = new HashMap<String, Object>();
        Study study = studyDao.getById(ServletRequestUtils.getRequiredIntParameter(httpServletRequest, "id"));
        Map<String, List> userLists = authorizationManager.getUsers(GROUP_NAME, study.getClass().getName()+"."+study.getId());
        refdata.put("study", study);
        refdata.put("assignedUsers", userLists.get(StudyCalendarAuthorizationManager.ASSIGNED_USERS));
        refdata.put("availableUsers", userLists.get(StudyCalendarAuthorizationManager.AVAILABLE_USERS));
        refdata.put("action", "Assign");
        return refdata;
    }

    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {
    	ParticipantCoordinatorCommand assignCommand = (ParticipantCoordinatorCommand) oCommand;
    	Study assignedStudy = studyDao.getById(assignCommand.getStudyId());
        authorizationManager.assignProtectionElementsToUsers(assignCommand.getAssignedCoordinators(), assignedStudy.getClass().getName()+"."+assignedStudy.getId());

        return new ModelAndView(new RedirectView(getSuccessView()), "id", ServletRequestUtils.getIntParameter(request, "id"));
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
    public void setStudyCalendarAuthorizationManager(StudyCalendarAuthorizationManager authorizationManager) {
        this.authorizationManager = authorizationManager;
    }

}
