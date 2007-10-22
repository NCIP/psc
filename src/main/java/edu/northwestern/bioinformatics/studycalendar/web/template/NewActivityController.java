package edu.northwestern.bioinformatics.studycalendar.web.template;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.validation.BindException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.ui.ModelMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.HashMap;

import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.utils.editors.ControlledVocabularyEditor;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;
import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
import edu.northwestern.bioinformatics.studycalendar.web.CreateUserCommand;
import edu.nwu.bioinformatics.commons.spring.ValidatableValidator;

/**
 * @author Jaron Sampson
 */
@AccessControl(roles = Role.STUDY_COORDINATOR)
public class NewActivityController extends PscSimpleFormController {
    private ActivityDao activityDao;

    private static final Logger log = LoggerFactory.getLogger(NewActivityController.class.getName());    

    public NewActivityController() {
        setCommandClass(NewActivityCommand.class);
        setValidator(new ValidatableValidator());
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
                .addObject("selectedActivity", activity.getId());
            return new ModelAndView("redirectToManagePeriod", model);
        }
    }

    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        NewActivityCommand command = new NewActivityCommand(activityDao);
        return command;
    }

    ////// CONFIGURATION

    @Required
    public void setActivityDao(ActivityDao activityDao) {
        this.activityDao = activityDao;
    }
}
