package edu.northwestern.bioinformatics.studycalendar.web.reporting;

import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.reporting.ScheduledActivitiesReportFilters;
import edu.northwestern.bioinformatics.studycalendar.dao.reporting.ScheduledActivitiesReportRowDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.reporting.ScheduledActivitiesReportRow;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.editors.ControlledVocabularyEditor;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractCommandController;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.service.AuthorizationService;
import gov.nih.nci.cabig.ctms.editors.DaoBasedEditor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * @author John Dzak
 */
@AccessControl(roles = {Role.SUBJECT_COORDINATOR, Role.SITE_COORDINATOR})
public class ScheduledActivitiesReportController extends PscAbstractCommandController {
    private ScheduledActivitiesReportRowDao dao;
    private UserDao userDao;
    private ActivityTypeDao activityTypeDao;
    private ApplicationSecurityManager applicationSecurityManager;
    private AuthorizationService authorizationService;
    private StudyDao studyDao;


    public ScheduledActivitiesReportController() {
        setCommandClass(ScheduledActivitiesReportCommand.class);
        setCrumb(new DefaultCrumb("Report"));
    }

    protected Object getCommand(HttpServletRequest request) throws Exception {
        return new ScheduledActivitiesReportCommand(new ScheduledActivitiesReportFilters());
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
        binder.registerCustomEditor(ScheduledActivityMode.class, "filters.currentStateMode",
            new ControlledVocabularyEditor(ScheduledActivityMode.class, true));
        getControllerTools().registerDomainObjectEditor(binder, "filters.activityType", activityTypeDao);
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
        binder.registerCustomEditor(Date.class, "filters.actualActivityDate.start", getControllerTools().getDateEditor(false));
        binder.registerCustomEditor(Date.class, "filters.actualActivityDate.stop", getControllerTools().getDateEditor(false));
        binder.registerCustomEditor(User.class, "filters.subjectCoordinator", new DaoBasedEditor(userDao));
    }

    @Override
    protected ModelAndView handle(Object oCommand, BindException errors, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ScheduledActivitiesReportCommand command = (ScheduledActivitiesReportCommand) oCommand;
        return new ModelAndView("reporting/scheduledActivitiesReport", createModel(errors, command));
    }

    @SuppressWarnings({"unchecked"})
    protected Map createModel(BindException errors, ScheduledActivitiesReportCommand command) {
        Map<String, Object> model = errors.getModel();
        List<User> users = getListOfColleagueUsers();
        model.put("modes", ScheduledActivityMode.values());
        model.put("types", activityTypeDao.getAll());
        model.put("coordinators", users);
        model.put("personId", command.getPersonId());
        if (command.getStartDate()!=null){
            model.put("startDate", command.getStartDate());
        }
        if (command.getEndDate() !=null) {
            model.put("endDate", command.getEndDate());
        }
        return model;
    }

    public List<User> getListOfColleagueUsers() {
        User signedUser = applicationSecurityManager.getUser();
        List<Study> studies = studyDao.getAll();
        List<Study> ownedStudies = authorizationService.filterStudiesForVisibility(studies, signedUser.getUserRole(Role.SUBJECT_COORDINATOR));
        List<User> mapOfUsers = new ArrayList<User>();
        List<User> allSubjCoord = new ArrayList<User>();
        List<User> coord = userDao.getAllSubjectCoordinators();

        for (User pcUser : coord) {
            if (!pcUser.equals(signedUser)) {
                allSubjCoord.add(pcUser);
            }
        }
        allSubjCoord.add(signedUser);
        for (User user : allSubjCoord) {
            List<Study> studiesForUser = authorizationService.filterStudiesForVisibility(ownedStudies, user.getUserRole(Role.SUBJECT_COORDINATOR));
            if (studiesForUser != null && studiesForUser.size() > 0) {
                mapOfUsers.add(user);
            }
        }
        if (mapOfUsers.size() ==0) {
            mapOfUsers.add(signedUser);
        }

        return mapOfUsers;
    }

    @Required
    public void setApplicationSecurityManager(ApplicationSecurityManager applicationSecurityManager) {
        this.applicationSecurityManager = applicationSecurityManager;
    }

    @Required
    public void setAuthorizationService(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    ////// Bean Setters
    public void setScheduledActivitiesReportRowDao(ScheduledActivitiesReportRowDao dao) {
        this.dao = dao;
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    @Required
    public void setActivityTypeDao(ActivityTypeDao activityTypeDao) {
        this.activityTypeDao = activityTypeDao;
    }
}
