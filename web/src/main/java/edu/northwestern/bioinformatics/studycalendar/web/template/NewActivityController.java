package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityPropertyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.service.DomainContext;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import edu.northwestern.bioinformatics.studycalendar.web.activity.AdvancedEditActivityCommand;
import edu.nwu.bioinformatics.commons.spring.ValidatableValidator;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;

/**
 * @author Jaron Sampson
 * @author Jalpa Patel
 */
public class NewActivityController extends PscSimpleFormController implements PscAuthorizedHandler {
    private ActivityDao activityDao;
    Activity activity;
    private SourceDao sourceDao;
    private PeriodDao periodDao;
    private ActivityTypeDao activityTypeDao;
    private ActivityPropertyDao activityPropertyDao;

    public NewActivityController() {
        setCommandClass(AdvancedEditActivityCommand.class);
        setValidator(new ValidatableValidator());
        setBindOnNewForm(true);
        setFormView("advancedEditActivity");
        setSuccessView("viewActivity");
        setCrumb(new Crumb());
    }

    public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
        return ResourceAuthorization.createCollection(STUDY_CALENDAR_TEMPLATE_BUILDER);
    }

    @Override
    protected Map referenceData(HttpServletRequest request, Object command, Errors errors) throws Exception {
        Map<String, Object> refdata = new HashMap<String, Object>();
        Integer periodId = ServletRequestUtils.getIntParameter(request, "returnToPeriod");
        if (periodId != null) {
            getControllerTools().addHierarchyToModel(periodDao.getById(periodId), refdata);
        }
        refdata.put("source", sourceDao.getManualTargetSource());
        refdata.put("activityTypes", activityTypeDao.getAll());
        refdata.put("action", "New");
        return refdata;
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
        getControllerTools().registerDomainObjectEditor(binder, "activity.type", activityTypeDao);
    }

    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {
        AdvancedEditActivityCommand command = (AdvancedEditActivityCommand) oCommand;
        Source source = sourceDao.getManualTargetSource();
        if (source != null) {
            if (activityDao.getByNameAndSourceName(command.getActivity().getName(), source.getName()) != null) {
                errors.rejectValue("activity.name","error.activity.name.already.exists");
                return showForm(request, response, errors);
            } else if (activityDao.getByCodeAndSourceName(command.getActivity().getCode(), source.getName()) != null) {
                errors.rejectValue("activity.code","error.activity.code.already.exists");
                return showForm(request, response, errors);
            }
            command.getActivity().setSource(source);
            activity = command.updateActivity();
            if (request.getParameter("returnToPeriod") == null) {
                Map<String, Object> model = errors.getModel();
                model.put("activity", activity);
                return new ModelAndView(getSuccessView(), model);
            } else {
                ModelMap model = new ModelMap("period",Integer.parseInt(request.getParameter("returnToPeriod")))
                    .addAttribute("selectedActivity", activity.getId());
                return new ModelAndView("redirectToManagePeriod", model);
            }
        } else {
            errors.reject("error.no.manual.activity.target.source");
            return showForm(request, response, errors);
        }
    }

    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        Activity activity = new Activity();
        return new AdvancedEditActivityCommand(activity, activityDao, activityPropertyDao);
    }

    private static class Crumb extends DefaultCrumb {
        public Crumb() {
            super("New Activity");
        }

        @Override
        public Map<String, String> getParameters(DomainContext context) {
            return Collections.singletonMap("returnToPeriod", context.getPeriod().getId().toString());
        }
    }

    ////// CONFIGURATION

    @Required
    public void setActivityDao(ActivityDao activityDao) {
        this.activityDao = activityDao;
    }

    @Required
    public void setSourceDao(SourceDao sourceDao) {
        this.sourceDao = sourceDao;
    }

    @Required
    public void setActivityTypeDao(ActivityTypeDao activityTypeDao) {
        this.activityTypeDao = activityTypeDao;
    }

    @Required
    public void setActivityPropertyDao(ActivityPropertyDao activityPropertyDao) {
        this.activityPropertyDao = activityPropertyDao;
    }

    @Required
    public void setPeriodDao(final PeriodDao periodDao) {
        this.periodDao = periodDao;
    }

}
