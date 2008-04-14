package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.BreadcrumbContext;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.utils.editors.ControlledVocabularyEditor;
import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
import edu.nwu.bioinformatics.commons.spring.ValidatableValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Jaron Sampson
 */
@AccessControl(roles = Role.STUDY_COORDINATOR)
public class NewActivityController extends PscSimpleFormController {
    public static final String PSC_CREATE_NEW_ACTIVITY_SOURCE_NAME = "PSC - Manual Activity Creation";

    private ActivityDao activityDao;
    private PeriodDao periodDao;

    private static final Logger log = LoggerFactory.getLogger(NewActivityController.class.getName());
    private SourceDao sourceDao;

    public NewActivityController() {
        setCommandClass(NewActivityCommand.class);
        setValidator(new ValidatableValidator());
        setBindOnNewForm(true);
        setFormView("editActivity");
        setSuccessView("viewActivity");
        setCrumb(new Crumb());
    }


    @Override
    protected Map referenceData(HttpServletRequest request, Object command, Errors errors) throws Exception {
        Map<String, Object> refdata = new HashMap<String, Object>();
        refdata.put("activityTypes", ActivityType.values());
        refdata.put("action", "New");
        refdata.put("sourceName", PSC_CREATE_NEW_ACTIVITY_SOURCE_NAME);

        Integer periodId = ((NewActivityCommand) command).getReturnToPeriodId();

        if (periodId != null) {
            getControllerTools().addHierarchyToModel(periodDao.getById(periodId), refdata);
        }
        return refdata;
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
        binder.registerCustomEditor(ActivityType.class, "activityType",
                new ControlledVocabularyEditor(ActivityType.class));
    }

    protected ModelAndView onSubmit(Object oCommand, BindException errors) throws Exception {
        NewActivityCommand command = (NewActivityCommand) oCommand;
        command.setActivitySource(sourceDao.getByName(PSC_CREATE_NEW_ACTIVITY_SOURCE_NAME));
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


    private static class Crumb extends DefaultCrumb {
        public Crumb() {
            super("Add activity");
        }

        @Override
        public Map<String, String> getParameters(BreadcrumbContext context) {
            return Collections.singletonMap("returnToPeriodId", context.getPeriod().getId().toString());
        }
    }

    ////// CONFIGURATION

    @Required
    public void setActivityDao(ActivityDao activityDao) {
        this.activityDao = activityDao;
    }

    public void setSourceDao(SourceDao sourceDao) {
        this.sourceDao = sourceDao;
    }

    @Required
    public void setPeriodDao(final PeriodDao periodDao) {
        this.periodDao = periodDao;
    }
}
