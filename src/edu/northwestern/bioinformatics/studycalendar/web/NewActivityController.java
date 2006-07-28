package edu.northwestern.bioinformatics.studycalendar.web;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.HashMap;

import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;

/**
 * @author Jaron Sampson
 */
public class NewActivityController extends SimpleFormController {
    private ActivityDao activityDao;

    public NewActivityController() {
        setCommandClass(NewActivityCommand.class);
        setFormView("editActivity");
        setSuccessView("viewActivity");
    }

    protected Map<String, Object> referenceData(HttpServletRequest httpServletRequest) throws Exception {
        Map<String, Object> refdata = new HashMap<String, Object>();
        refdata.put("action", "New");
        return refdata;
    }

    protected ModelAndView onSubmit(Object oCommand, BindException errors) throws Exception {
        NewActivityCommand command = (NewActivityCommand) oCommand;
        Activity activity = command.createActivity();
        // TODO: transaction
        activityDao.save(activity);

        Map<String, Object> model = errors.getModel();
        model.put("activity", activity);
        return new ModelAndView(getSuccessView(), model);
    }

    ////// CONFIGURATION

    @Required
    public void setActivityDao(ActivityDao activityDao) {
        this.activityDao = activityDao;
    }
}
