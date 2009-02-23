package edu.northwestern.bioinformatics.studycalendar.web.activity;

import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractController;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.service.ActivityTypeService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

/**
 * @author Nataliya Shurupova
 */
@AccessControl(roles = Role.STUDY_ADMIN)
public class AddEditActivityTypeController extends PscAbstractController {
    private ActivityTypeDao activityTypeDao;
    private ActivityDao activityDao;
    private ActivityTypeService activityTypeService;

    public AddEditActivityTypeController() {
        setCrumb(new DefaultCrumb("Activity types"));
    }

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        if ("POST".equals(request.getMethod())) {
            String activityTypeName = ServletRequestUtils.getRequiredStringParameter(request, "activityTypeName");
            String action = ServletRequestUtils.getRequiredStringParameter(request, "action");

            if (action.toLowerCase().equals("save")){
                Integer activityTypeId = ServletRequestUtils.getRequiredIntParameter(request, "activityTypeId");
                ActivityType at = activityTypeDao.getById(activityTypeId);
                at.setName(activityTypeName);
                activityTypeDao.save(at);
            } else if (action.toLowerCase().equals("add")) {
                ActivityType at = new ActivityType(activityTypeName);
                if (activityTypeDao.getByName(activityTypeName) == null) {
                    activityTypeDao.save(at);
                }
            } else if (action.toLowerCase().equals("delete")) {
                Integer activityTypeId = ServletRequestUtils.getRequiredIntParameter(request, "activityTypeId");
                ActivityType at = activityTypeDao.getById(activityTypeId);
                activityTypeService.deleteActivityType(at); 
            }
        }
        List<ActivityType> activityTypes = activityTypeDao.getAll();
        List<Activity> activities = activityDao.getAll();
        Map<Integer, Boolean> enableDelete = new HashMap<Integer, Boolean>();
        for (ActivityType at : activityTypes){
            for (Activity a : activities){
                if (a.getType().equals(at)) {
                    enableDelete.put(at.getId(), false);
                    break;
                }
            }
            if (!enableDelete.containsKey(at.getId())){
                enableDelete.put(at.getId(), true);
            }
        }

        model.put("activityTypes", activityTypes);
        model.put("enableDeletes", enableDelete);
        if ("POST".equals(request.getMethod())){
            return new ModelAndView("template/ajax/activityTypeTableUpdate", model);
        } else {
            return new ModelAndView("editActivityTypes", model);
        }
    }

    @Required
    public void setActivityTypeDao(ActivityTypeDao activityTypeDao) {
        this.activityTypeDao = activityTypeDao;
    }

    @Required
    public void setActivityDao(ActivityDao activityDao) {
        this.activityDao = activityDao;
    }

    @Required
    public void setActivityTypeService(ActivityTypeService activityTypeService) {
        this.activityTypeService = activityTypeService;
    }
}
