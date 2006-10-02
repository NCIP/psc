package edu.northwestern.bioinformatics.studycalendar.web;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.validation.BindException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.ui.ModelMap;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.HashMap;

import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.utils.editors.ControlledVocabularyEditor;

/**
 * @author Jaron Sampson
 */
public class NewActivityController extends SimpleFormController {
    private ActivityDao activityDao;

    public NewActivityController() {
        setCommandClass(NewActivityCommand.class);
        setBindOnNewForm(true);
        setFormView("editActivity");
        setSuccessView("viewActivity");
    }

    protected Map<String, Object> referenceData(HttpServletRequest httpServletRequest) throws Exception {
        Map<String, Object> refdata = new HashMap<String, Object>();
        refdata.put("activityTypes", ActivityType.values());
        refdata.put("action", "New");
        return refdata;
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
        binder.registerCustomEditor(ActivityType.class, "activityType",
            new ControlledVocabularyEditor(ActivityType.class));
    }

    protected ModelAndView onSubmit(Object oCommand, BindException errors) throws Exception {
        NewActivityCommand command = (NewActivityCommand) oCommand;
        Activity activity = command.createActivity();
        // TODO: transaction
        activityDao.save(activity);

        if (command.getReturnToPeriodId() == null) {
            Map<String, Object> model = errors.getModel();
            model.put("activity", activity);
            return new ModelAndView(getSuccessView(), model);
        } else {
            ModelMap model = new ModelMap("id", command.getReturnToPeriodId())
                .addObject("newActivityId", activity.getId());
            return new ModelAndView("redirectToManagePeriod", model);
        }
    }

    ////// CONFIGURATION

    @Required
    public void setActivityDao(ActivityDao activityDao) {
        this.activityDao = activityDao;
    }
}
