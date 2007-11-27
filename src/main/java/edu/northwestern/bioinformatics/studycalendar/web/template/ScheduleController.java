package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
import edu.northwestern.bioinformatics.studycalendar.dao.*;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.editors.ControlledVocabularyEditor;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.validation.BindException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import edu.northwestern.bioinformatics.studycalendar.service.SubjectCoordinatorDashboardService;

@AccessControl(roles = Role.SUBJECT_COORDINATOR)
public class ScheduleController extends PscSimpleFormController {
	private TemplateService templateService;

    private ScheduledActivityDao scheduledActivityDao;
	private StudyDao studyDao;
    private UserDao userDao;
    private SubjectCoordinatorDashboardService subjectCoordinatorDashboardService;

    private static final Logger log = LoggerFactory.getLogger(ScheduleController.class.getName());

    public ScheduleController() {
        setCommandClass(ScheduleCommand.class);
        setBindOnNewForm(true);
    }


    public ModelAndView showForm(HttpServletRequest request, HttpServletResponse response, BindException errors) throws Exception {
        setFormView("/subjectCoordinatorSchedule");
        return showForm(request, response, errors, null);
    }

    protected Map<String, Object> referenceData(HttpServletRequest httpServletRequest) throws Exception {
        String userName = ApplicationSecurityManager.getUser();
        List<Study> studies = studyDao.getAll();
        List<Study> ownedStudies = templateService.filterForVisibility(studies, userDao.getByName(userName).getUserRole(Role.SUBJECT_COORDINATOR));
        User user = userDao.getByName(userName);
        List<StudySubjectAssignment> studySubjectAssignments = getUserDao().getAssignments(user);

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("numberOfDays", 7);
        model.put("userName", user);
        model.put("ownedStudies", ownedStudies);
        model.put("colleguesStudies", getMapOfColleagueUsersAndStudySites(ownedStudies));
        model.put("mapOfUserAndCalendar", getPAService().getMapOfCurrentEvents(studySubjectAssignments, 7));
        model.put("pastDueActivities", getPAService().getMapOfOverdueEvents(studySubjectAssignments));
        model.put("activityTypes", ActivityType.values());
        
        return model;
    }

    public Map<User, List<StudySite>> getMapOfColleagueUsersAndStudySites(List<Study> ownedStudies) throws Exception {
        String userName = ApplicationSecurityManager.getUser();

        Map<User, List<StudySite>> mapOfUsersAndStudies =  new HashMap<User, List<StudySite>>();

        List<User> pcUsers = userDao.getAllSubjectCoordinators();
        pcUsers.remove(userDao.getByName(userName));
        for (User user : pcUsers) {
            List<StudySite> studySiteForMap = new ArrayList<StudySite>();
            List<Study> studiesForUser = templateService.filterForVisibility(ownedStudies, user.getUserRole(Role.SUBJECT_COORDINATOR));
            if (studiesForUser != null && studiesForUser.size()>0) {
                for (Study study: studiesForUser) {
                    List<StudySite> studysites = study.getStudySites();
                    for (StudySite studySite: studysites){
                        if (!studySiteForMap.contains(studySite)) {
                            studySiteForMap.add(studySite);
                        }
                    }
                }
                mapOfUsersAndStudies.put(user, studySiteForMap);
            }
        }

        return  mapOfUsersAndStudies;
    }

    protected Object formBackingObject(HttpServletRequest httpServletRequest) throws Exception {
        ScheduleCommand command = new ScheduleCommand();
        command.setToDate(7);
        return command;
    }    

    protected ModelAndView onSubmit(HttpServletRequest request,
                                    HttpServletResponse response,
                                    Object oCommand, BindException errors) throws Exception {
        ScheduleCommand scheduleCommand = (ScheduleCommand) oCommand;
        String userName = ApplicationSecurityManager.getUser();
        User user = userDao.getByName(userName);
        scheduleCommand.setUser(user);
        scheduleCommand.setUserDao(userDao);
        scheduleCommand.setScheduledActivityDao(scheduledActivityDao);
        Map<String, Object> model = scheduleCommand.execute(getPAService());
        return new ModelAndView("template/ajax/listOfSubjectsAndEvents", model);
    }

    protected void initBinder(HttpServletRequest httpServletRequest,
                              ServletRequestDataBinder servletRequestDataBinder) throws Exception {
        super.initBinder(httpServletRequest, servletRequestDataBinder);
        servletRequestDataBinder.registerCustomEditor(Integer.class, new CustomNumberEditor(Integer.class, false));
        servletRequestDataBinder.registerCustomEditor(ActivityType.class, new ControlledVocabularyEditor(ActivityType.class));
        
    }

    ////// CONFIGURATION
    @Required
    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }
    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }
    @Required
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }
    public UserDao getUserDao() {
        return userDao;
    }
    @Required
    public void setScheduledActivityDao(ScheduledActivityDao scheduledActivityDao) {
        this.scheduledActivityDao = scheduledActivityDao;
    }
    public ScheduledActivityDao getScheduledActivityDao() {
        return scheduledActivityDao;
    }

    public SubjectCoordinatorDashboardService getPAService() {
        return subjectCoordinatorDashboardService;
    }

    public void setSubjectCoordinatorDashboardService(SubjectCoordinatorDashboardService subjectCoordinatorDashboardService) {
        this.subjectCoordinatorDashboardService = subjectCoordinatorDashboardService;
    }
}