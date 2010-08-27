package edu.northwestern.bioinformatics.studycalendar.web.reporting;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.service.PscUserService;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractController;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author John Dzak
 */
// TODO: the binding/execution parts of this controller are no longer used.  Remove them.
public class ScheduledActivitiesReportController
    extends PscAbstractController
    implements PscAuthorizedHandler
{
    public static PscRole[] REPORT_AUTHORIZED_ROLES = {
        PscRole.STUDY_TEAM_ADMINISTRATOR,
        PscRole.DATA_READER,
        PscRole.STUDY_SUBJECT_CALENDAR_MANAGER
    };

    private ActivityTypeDao activityTypeDao;
    private ApplicationSecurityManager applicationSecurityManager;
    private PscUserService pscUserService;

    public ScheduledActivitiesReportController() {
        setCrumb(new DefaultCrumb("Report"));
    }

    public Collection<ResourceAuthorization> authorizations(
        String httpMethod, Map<String, String[]> queryParameters
    ) {
        return ResourceAuthorization.createCollection(REPORT_AUTHORIZED_ROLES);
    }

    @Override
    protected ModelAndView handleRequestInternal(
        HttpServletRequest request, HttpServletResponse response
    ) throws Exception {
        return new ModelAndView("reporting/scheduledActivitiesReport", createModel());
    }

    @SuppressWarnings({"unchecked"})
    private Map createModel() {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("modes", ScheduledActivityMode.values());
        model.put("types", activityTypeDao.getAll());
        model.put("potentialResponsibleUsers", pscUserService.getColleaguesOf(
            applicationSecurityManager.getUser(), PscRole.STUDY_SUBJECT_CALENDAR_MANAGER,
            REPORT_AUTHORIZED_ROLES));

        return model;
    }

    ////// CONFIGURATION

    @Required
    public void setApplicationSecurityManager(ApplicationSecurityManager applicationSecurityManager) {
        this.applicationSecurityManager = applicationSecurityManager;
    }

    @Required
    public void setActivityTypeDao(ActivityTypeDao activityTypeDao) {
        this.activityTypeDao = activityTypeDao;
    }

    @Required
    public void setPscUserService(PscUserService pscUserService) {
        this.pscUserService = pscUserService;
    }
}
