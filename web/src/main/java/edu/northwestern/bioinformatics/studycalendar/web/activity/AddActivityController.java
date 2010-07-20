package edu.northwestern.bioinformatics.studycalendar.web.activity;

import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractCommandController;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import edu.northwestern.bioinformatics.studycalendar.web.template.NewActivityCommand;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.nwu.bioinformatics.commons.spring.ValidatableValidator;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.BUSINESS_ADMINISTRATOR;

public class AddActivityController extends PscAbstractCommandController<NewActivityCommand> implements PscAuthorizedHandler {
    private ActivityDao activityDao;
    private PlannedActivityDao plannedActivityDao;
    private SourceDao sourceDao;
    private ActivityTypeDao activityTypeDao;

    public AddActivityController() {
        setCommandClass(NewActivityCommand.class);
        setValidator(new ValidatableValidator());
    }

    public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
        return ResourceAuthorization.createCollection(BUSINESS_ADMINISTRATOR);
    }
    
    @Override
    protected Object getCommand(HttpServletRequest request) throws Exception {
        return new NewActivityCommand(activityDao);
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
        getControllerTools().registerDomainObjectEditor(binder, "activityType", activityTypeDao);
        getControllerTools().registerDomainObjectEditor(binder, "activitySource", sourceDao);
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    protected ModelAndView handle(NewActivityCommand command, BindException errors, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        if (! errors.hasErrors()) {
            Activity activity = command.createActivity();
            activityDao.save(activity);
        } else {
            //customize message
            if(errors.getFieldError("activityName")!=null && errors.getFieldError("activityCode")!=null)
                model.put("error",errors.getFieldError("activityName").getCode().concat(errors.getFieldError("activityCode").getCode()));
            else if(errors.getFieldError("activityName")!=null)
                model.put("error",errors.getFieldError("activityName").getCode());
            else
                model.put("error",errors.getFieldError("activityCode").getCode());              
        }
        List<Activity> activities = activityDao.getBySourceId(command.getActivitySource().getId());
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
        return new ModelAndView("template/ajax/activityTableUpdate", model);
    }

    //// CONFIGURATION

    @Required
    public void setActivityDao(ActivityDao activityDao) {
        this.activityDao = activityDao;
    }

    @Required
    public void setSourceDao(SourceDao sourceDao) {
        this.sourceDao = sourceDao;
    }

    @Required
    public void setPlannedActivityDao(PlannedActivityDao plannedActivityDao) {
        this.plannedActivityDao = plannedActivityDao;
    }

    @Required
    public void setActivityTypeDao(ActivityTypeDao activityTypeDao) {
        this.activityTypeDao = activityTypeDao;
    }
}



