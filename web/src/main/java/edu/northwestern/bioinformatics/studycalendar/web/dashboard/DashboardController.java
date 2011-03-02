package edu.northwestern.bioinformatics.studycalendar.web.dashboard;

import edu.northwestern.bioinformatics.studycalendar.configuration.Configuration;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.PscUserService;
import edu.northwestern.bioinformatics.studycalendar.utils.editors.PscUserEditor;
import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractCommandController;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class DashboardController extends PscAbstractCommandController<DashboardCommand> implements PscAuthorizedHandler {
    private ApplicationSecurityManager applicationSecurityManager;
    private StudyDao studyDao;
    private PscUserService pscUserService;
    private ActivityTypeDao activityTypeDao;
    private Configuration configuration;

    @Override
    public Collection<ResourceAuthorization> authorizations(
        String httpMethod, Map<String, String[]> queryParameters
    ) throws Exception {
        return ResourceAuthorization.createCollection(PscRole.STUDY_SUBJECT_CALENDAR_MANAGER);
    }

    @Override
    protected Object getCommand(HttpServletRequest request) throws Exception {
        return new DashboardCommand(applicationSecurityManager.getUser(), pscUserService, studyDao, configuration);
    }

    @Override
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
        binder.registerCustomEditor(PscUser.class, "user", new PscUserEditor(pscUserService));
    }

    @Override
    protected ModelAndView handle(
        DashboardCommand command, BindException errors,
        HttpServletRequest request, HttpServletResponse response
    ) throws Exception {
        ModelAndView mv = new ModelAndView("dashboard/display", errors.getModel());
        mv.addObject("activityTypes", activityTypeDao.getAll());
        mv.addObject("initialUpcomingDays",
            configuration.get(Configuration.DASHBOARD_DEFAULT_UPCOMING_WINDOW));
        return mv;
    }

    ////// CONFIGURATION

    @Required
    public void setApplicationSecurityManager(ApplicationSecurityManager applicationSecurityManager) {
        this.applicationSecurityManager = applicationSecurityManager;
    }

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    @Required
    public void setPscUserService(PscUserService pscUserService) {
        this.pscUserService = pscUserService;
    }

    @Required
    public void setActivityTypeDao(ActivityTypeDao activityTypeDao) {
        this.activityTypeDao = activityTypeDao;
    }

    @Required
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
