package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.BreadcrumbContext;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.utils.editors.ControlledVocabularyEditor;
import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
import edu.nwu.bioinformatics.commons.spring.ValidatableValidator;
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
    private SourceDao sourceDao;
    private ActivityTypeDao activityTypeDao;

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
        refdata.put("activityTypes", activityTypeDao.getAll());
        refdata.put("action", "New");
        refdata.put("sourceName", PSC_CREATE_NEW_ACTIVITY_SOURCE_NAME);

        Integer periodId = ((NewActivityCommand) command).getReturnToPeriod();

        if (periodId != null) {
            getControllerTools().addHierarchyToModel(periodDao.getById(periodId), refdata);
        }
        return refdata;
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
        getControllerTools().registerDomainObjectEditor(binder, "activityType", activityTypeDao);
    }

    protected ModelAndView onSubmit(Object oCommand, BindException errors) throws Exception {
        NewActivityCommand command = (NewActivityCommand) oCommand;
        command.setActivitySource(sourceDao.getByName(PSC_CREATE_NEW_ACTIVITY_SOURCE_NAME));
        Activity activity = command.createActivity();
        activityDao.save(activity);

        if (command.getReturnToPeriod() == null) {
            Map<String, Object> model = errors.getModel();
            model.put("activity", activity);
            return new ModelAndView(getSuccessView(), model);
        } else {
            ModelMap model = new ModelMap("period", command.getReturnToPeriod())
                    .addObject("selectedActivity", activity.getId());
            return new ModelAndView("redirectToManagePeriod", model);
        }
    }

    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        return new NewActivityCommand(activityDao);
    }

    private static class Crumb extends DefaultCrumb {
        public Crumb() {
            super("Add activity");
        }

        @Override
        public Map<String, String> getParameters(BreadcrumbContext context) {
            return Collections.singletonMap("returnToPeriod", context.getPeriod().getId().toString());
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

    @Required
    public void setActivityTypeDao(ActivityTypeDao activityTypeDao) {
        this.activityTypeDao = activityTypeDao;
    }
}
