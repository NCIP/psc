package edu.northwestern.bioinformatics.studycalendar.web.dashboard.subjectcoordinator;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
import edu.northwestern.bioinformatics.studycalendar.dao.NotificationDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.service.AuthorizationService;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectCoordinatorDashboardService;
import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import gov.nih.nci.cabig.ctms.editors.DaoBasedEditor;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_SUBJECT_CALENDAR_MANAGER;

@AccessControl(roles = Role.SUBJECT_COORDINATOR)
public class ScheduleController extends PscSimpleFormController implements PscAuthorizedHandler {

    private ScheduledActivityDao scheduledActivityDao;
    private StudyDao studyDao;
    private UserDao userDao;
    private SubjectCoordinatorDashboardService subjectCoordinatorDashboardService;
    private NotificationDao notificationDao;
    private ActivityTypeDao activityTypeDao;
    private ApplicationSecurityManager applicationSecurityManager;
    private AuthorizationService authorizationService;

    public ScheduleController() {
        setCommandClass(ScheduleCommand.class);
        setBindOnNewForm(true);
    }

    public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
        return ResourceAuthorization.createCollection(STUDY_SUBJECT_CALENDAR_MANAGER);
    }

    @Override
    public ModelAndView showForm(HttpServletRequest request, HttpServletResponse response, BindException errors) throws Exception {
        setFormView("/subjectCoordinatorSchedule");
        return showForm(request, response, errors, null);
    }

    @Override
    protected Map<String, Object> referenceData(HttpServletRequest httpServletRequest) throws Exception {
        User user = applicationSecurityManager.getFreshUser();
        List<Study> studies = studyDao.getAll();
        List<Study> ownedStudies = authorizationService.filterStudiesForVisibility(studies, user.getUserRole(Role.SUBJECT_COORDINATOR));
        List<StudySite> ownedStudySites = authorizationService.filterStudySitesForVisibilityFromStudiesList(ownedStudies, user.getUserRole(Role.SUBJECT_COORDINATOR));
        List<StudySubjectAssignment> studySubjectAssignments = getUserDao().getAssignments(user);
        List<StudySubjectAssignment> filteredAssignmnetns = authorizationService.filterStudySubjectAssignmentsByStudySite(ownedStudySites, studySubjectAssignments);

        // show notifications on dashboard
        List<Notification> notifications = new ArrayList<Notification>();
        for (StudySubjectAssignment studySubjectAssignment : filteredAssignmnetns) {
            notifications.addAll(studySubjectAssignment.getNotifications());
        }

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("numberOfDays", 7);
        model.put("userName", user);
        model.put("ownedStudies", ownedStudies);
        model.put("ownedStudySites", ownedStudySites);
        model.put("colleguesStudies", getMapOfColleagueUsersAndStudySites(ownedStudies));
        model.put("mapOfUserAndCalendar", getPAService().getMapOfCurrentEvents(filteredAssignmnetns, 7));
        model.put("pastDueActivities", getPAService().getMapOfOverdueEvents(filteredAssignmnetns));
        model.put("activityTypes", activityTypeDao.getAll());
        model.put("activityTypesCount", activityTypeDao.getAll().size());
        Map<Subject, List<Notification>> subjectNotificationsMap = getMapOfSubjectsAndNotifications(filteredAssignmnetns);
        model.put("notificationsSubjectMap", subjectNotificationsMap);
        model.put("notifications",notifications);
       
        return model;
    }

    public Map<Subject, List<Notification>> getMapOfSubjectsAndNotifications(List<StudySubjectAssignment> studySubjectAssignments) throws Exception {
        Map<Subject, List<Notification>> subjectNotificationSubject = new HashMap<Subject, List<Notification>>();

        for (StudySubjectAssignment studySubjectAssignment : studySubjectAssignments) {
            Subject s = studySubjectAssignment.getSubject();
            List<Notification> notifications = studySubjectAssignment.getNotifications();
            if (! notifications.isEmpty()) {
                if (! areAllNotificationsDismissed(notifications)){
                    subjectNotificationSubject.put(s, notifications);
                }
            }
        }
        return subjectNotificationSubject;
    }

    public boolean areAllNotificationsDismissed(List<Notification> notifications) throws Exception {
        boolean dismissed = true;
        for (Notification notification : notifications) {
            if (! notification.isDismissed()) {
                dismissed = notification.isDismissed();
            }
        }
        return dismissed;
    }

    public Map<User, List<StudySite>> getMapOfColleagueUsersAndStudySites(List<Study> ownedStudies) throws Exception {
        Map<User, List<StudySite>> mapOfUsersAndStudies = new HashMap<User, List<StudySite>>();
        List<User> colleagues = new ArrayList<User>();
        User current = applicationSecurityManager.getUser().getLegacyUser();
        // userDao.getAllSubjectCoordinators().remove doesn't remove user with assignment from list. 
        for (User pcUser : userDao.getAllSubjectCoordinators()) {
            if (!pcUser.equals(current)) {
                colleagues.add(pcUser);
            }
        }

        for (User user : colleagues) {
            List<StudySite> studySiteForMap = new ArrayList<StudySite>();
            List<Study> studiesForUser = authorizationService.filterStudiesForVisibility(ownedStudies, user.getUserRole(Role.SUBJECT_COORDINATOR));
            if (studiesForUser != null && studiesForUser.size() > 0) {
                for (Study study : studiesForUser) {
                    List<StudySite> studysites = study.getStudySites();
                    for (StudySite studySite : studysites) {
                        if (!studySiteForMap.contains(studySite)) {
                            studySiteForMap.add(studySite);
                        }
                    }
                }
                mapOfUsersAndStudies.put(user, studySiteForMap);
            }
        }

        return mapOfUsersAndStudies;
    }

    @Override
    protected Object formBackingObject(HttpServletRequest httpServletRequest) throws Exception {
        ScheduleCommand command = new ScheduleCommand();
        command.setToDate(7);
        return command;
    }

    @Override
    protected ModelAndView onSubmit(HttpServletRequest request,
                                    HttpServletResponse response,
                                    Object oCommand, BindException errors) throws Exception {
        ScheduleCommand scheduleCommand = (ScheduleCommand) oCommand;
        User user = applicationSecurityManager.getUser().getLegacyUser();
        if (scheduleCommand.getNotificationId() != null ) {
            Notification notification = notificationDao.getById(scheduleCommand.getNotificationId());
            notification.setDismissed(true);
            notificationDao.save(notification);

            Map<String, Object> model = new HashMap<String, Object>();
            List<Notification> notifications = new ArrayList<Notification>();
            List<StudySubjectAssignment> studySubjectAssignments = getUserDao().getAssignments(user);
            for (StudySubjectAssignment studySubjectAssignment : studySubjectAssignments) {
                notifications.addAll(studySubjectAssignment.getNotifications());
            }
            model.put("notifications",notifications);
            Map<Subject, List<Notification>> subjectNotificationsMap = getMapOfSubjectsAndNotifications(studySubjectAssignments);
            model.put("notificationsSubjectMap", subjectNotificationsMap);
            return new ModelAndView("template/ajax/notificationList", model);
        } else {
            scheduleCommand.setUser(user);
            scheduleCommand.setUserDao(userDao);
            scheduleCommand.setScheduledActivityDao(scheduledActivityDao);
            Map<String, Object> model = scheduleCommand.execute(getPAService());
            return new ModelAndView("template/ajax/listOfSubjectsAndEvents", model);
        }

    }

    @Override
    protected void initBinder(HttpServletRequest httpServletRequest,
                              ServletRequestDataBinder servletRequestDataBinder) throws Exception {
        super.initBinder(httpServletRequest, servletRequestDataBinder);
        servletRequestDataBinder.registerCustomEditor(Integer.class, new CustomNumberEditor(Integer.class, false));
        servletRequestDataBinder.registerCustomEditor(ActivityType.class, new DaoBasedEditor(activityTypeDao));
    }

    ////// CONFIGURATION

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

    public NotificationDao getNotificationDao() {
        return notificationDao;
    }

    @Required
    public void setNotificationDao(NotificationDao notificationDao) {
        this.notificationDao = notificationDao;
    }

    @Required
    public void setActivityTypeDao(ActivityTypeDao activityTypeDao) {
        this.activityTypeDao = activityTypeDao;
    }

    @Required
    public void setApplicationSecurityManager(ApplicationSecurityManager applicationSecurityManager) {
        this.applicationSecurityManager = applicationSecurityManager;
    }

    @Required
    public void setAuthorizationService(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }
}