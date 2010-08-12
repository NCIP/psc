package edu.northwestern.bioinformatics.studycalendar.web.reporting;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.service.AuthorizationService;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractController;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;

/**
 * @author John Dzak
 */
// TODO: the binding/execution parts of this controller are no longer used.  Remove them.
@AccessControl(roles = {Role.SUBJECT_COORDINATOR, Role.SITE_COORDINATOR})
public class ScheduledActivitiesReportController
    extends PscAbstractController
    implements PscAuthorizedHandler
{
    private UserDao userDao;
    private ActivityTypeDao activityTypeDao;
    private ApplicationSecurityManager applicationSecurityManager;
    private AuthorizationService authorizationService;
    private StudyDao studyDao;

    public ScheduledActivitiesReportController() {
        setCrumb(new DefaultCrumb("Report"));
    }

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
    protected ModelAndView handleRequestInternal(
        HttpServletRequest request, HttpServletResponse response
    ) throws Exception {
        return new ModelAndView("reporting/scheduledActivitiesReport", createModel(request));
    }

    @SuppressWarnings({"unchecked"})
    protected Map createModel(HttpServletRequest request) {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("modes", ScheduledActivityMode.values());
        model.put("types", activityTypeDao.getAll());
        model.put("coordinators", Collections.<User>emptyList());
//        model.put("coordinators", getListOfColleagueUsers());
        
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
