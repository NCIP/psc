package edu.northwestern.bioinformatics.studycalendar.web.reporting;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.dao.reporting.ScheduledActivitiesReportFilters;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.service.AuthorizationService;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.utils.editors.ControlledVocabularyEditor;
import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractCommandController;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import gov.nih.nci.cabig.ctms.editors.DaoBasedEditor;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;

/**
 * @author John Dzak
 */
@AccessControl(roles = {Role.SUBJECT_COORDINATOR, Role.SITE_COORDINATOR})
public class ScheduledActivitiesReportController
    extends PscAbstractCommandController<ScheduledActivitiesReportCommand>
    implements PscAuthorizedHandler
{
    private UserDao userDao;
    private ActivityTypeDao activityTypeDao;
    private ApplicationSecurityManager applicationSecurityManager;
    private AuthorizationService authorizationService;
    private StudyDao studyDao;


    public ScheduledActivitiesReportController() {
        setCommandClass(ScheduledActivitiesReportCommand.class);
        setCrumb(new DefaultCrumb("Report"));
    }

    @Override
    public Collection<ResourceAuthorization> authorizations(
        String httpMethod, Map<String, String[]> queryParameters
    ) {
        return ResourceAuthorization.createCollection(
            DATA_READER,
            STUDY_SUBJECT_CALENDAR_MANAGER,
            STUDY_TEAM_ADMINISTRATOR
        );
    }

    @Override
    protected Object getCommand(HttpServletRequest request) throws Exception {
        return new ScheduledActivitiesReportCommand(new ScheduledActivitiesReportFilters());
    }

    @Override
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
    protected ModelAndView handle(
        ScheduledActivitiesReportCommand command, BindException errors,
        HttpServletRequest request, HttpServletResponse response
    ) throws Exception {
        return new ModelAndView("reporting/scheduledActivitiesReport", createModel(errors));
    }

    @SuppressWarnings({"unchecked"})
    protected Map createModel(BindException errors) {
        Map<String, Object> model = errors.getModel();
        model.put("modes", ScheduledActivityMode.values());
        model.put("types", activityTypeDao.getAll());
        model.put("coordinators", getListOfColleagueUsers());
        return model;
    }

    public List<User> getListOfColleagueUsers() {
        User signedUser = applicationSecurityManager.getUser().getLegacyUser();
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

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    @Required
    public void setActivityTypeDao(ActivityTypeDao activityTypeDao) {
        this.activityTypeDao = activityTypeDao;
    }
}
