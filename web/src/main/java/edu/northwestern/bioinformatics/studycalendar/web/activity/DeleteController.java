package edu.northwestern.bioinformatics.studycalendar.web.activity;

import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractController;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
import edu.northwestern.bioinformatics.studycalendar.service.ActivityService;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.BUSINESS_ADMINISTRATOR;

/**
 * Created by IntelliJ IDEA.
 * User: nshurupova
 * Date: Jul 29, 2008
 * Time: 10:38:17 AM
 * To change this template use File | Settings | File Templates.
 */
public class DeleteController extends PscAbstractController implements PscAuthorizedHandler {
    private ActivityDao activityDao;
    private PlannedActivityDao plannedActivityDao;
    private ActivityService activityService;
    private ActivityTypeDao activityTypeDao;

    public DeleteController() {
        setCrumb(new DefaultCrumb("Activities"));
    }

    public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
        return ResourceAuthorization.createCollection(BUSINESS_ADMINISTRATOR);
    }

    @Override
     protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        //deleteAction
        Integer activityId = ServletRequestUtils.getRequiredIntParameter(request, "activityId");
        Activity a = activityDao.getById(activityId);
        Source source = a.getSource();
        String message = "Can not delete the activity because activity is used by other planned activities";
        boolean deleted= activityService.deleteActivity(a);
        if (!deleted){
            log.error(message);
            model.put("error", message);
        }
        List<Activity> activities = activityDao.getBySourceId(source.getId());
        Map<Integer, Boolean> enableDelete = new HashMap<Integer, Boolean>();
        for (Activity activity : activities) {
            if (plannedActivityDao.getPlannedActivitiesForActivity(activity.getId()).size()>0) {
                enableDelete.put(activity.getId(), false);
            } else {
                enableDelete.put(activity.getId(), true);
            }
        }
        model.put("enableDeletes", enableDelete);
        model.put("activitiesPerSource", activities);
        model.put("activityTypes", activityTypeDao.getAll());
        model.put("displayCreateNewActivity", Boolean.TRUE);
        model.put("showtable", Boolean.TRUE);
        return new ModelAndView("template/ajax/activityTableUpdate", model);
    }

   //// CONFIGURATION

    @Required
    public void setActivityDao(ActivityDao activityDao) {
        this.activityDao = activityDao;
    }

    @Required
    public void setPlannedActivityDao(PlannedActivityDao plannedActivityDao) {
        this.plannedActivityDao = plannedActivityDao;
    }

    @Required
    public void setActivityService(final ActivityService activityService) {
        this.activityService = activityService;
    }

    @Required
    public void setActivityTypeDao(ActivityTypeDao activityTypeDao) {
        this.activityTypeDao = activityTypeDao;
    }
}
