package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.DaoFinder;
import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.utils.DomainObjectTools;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.BreadcrumbContext;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractCommandController;
import edu.northwestern.bioinformatics.studycalendar.web.delta.RevisionChanges;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.propertyeditors.CustomBooleanEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
@Deprecated // will be replaced with period.ManagePeriodActivities* when all behaviors are ported
@AccessControl(roles = Role.STUDY_COORDINATOR)
public class ManagePeriodEventsController extends PscAbstractCommandController<ManagePeriodEventsCommand> {
    private PeriodDao periodDao;
    private ActivityDao activityDao;
    private PlannedActivityDao plannedActivityDao;
    private TemplateService templateService;
    private DeltaService deltaService;
    private DaoFinder daoFinder;
    private SourceDao sourceDao;

    public ManagePeriodEventsController() {
        setCommandClass(ManagePeriodEventsCommand.class);
        setCrumb(new Crumb());
    }

    @Override
    protected Object getCommand(HttpServletRequest request) throws Exception {
        int periodId = ServletRequestUtils.getRequiredIntParameter(request, "id");
        Period period = periodDao.getById(periodId);
        period = deltaService.revise(period);
        return new ManagePeriodEventsCommand(period);
    }

    @Override
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        binder.registerCustomEditor(Boolean.class, new CustomBooleanEditor(false));
        binder.registerCustomEditor(String.class, "grid.details", new StringTrimmerEditor(true));
        binder.registerCustomEditor(String.class, "grid.conditionalDetails", new StringTrimmerEditor(true));
        getControllerTools().registerDomainObjectEditor(binder, "grid.activity", activityDao);
        getControllerTools().registerDomainObjectEditor(binder, "grid.plannedActivities", plannedActivityDao);
        binder.registerCustomEditor(Integer.class, new CustomNumberEditor(Integer.class, false));
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    protected ModelAndView handle(ManagePeriodEventsCommand command, BindException errors, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> refdata = referenceData(request, command, errors);
        refdata.putAll(errors.getModel());
        return new ModelAndView("managePeriod", refdata);
    }

    protected Map<String, Object> referenceData(
        HttpServletRequest request, Object oCommand, Errors errors
    ) throws Exception {
        ManagePeriodEventsCommand command = (ManagePeriodEventsCommand) oCommand;

        Map<String, Object> refdata = new HashMap<String, Object>();

        Integer selectedActivityId = ServletRequestUtils.getIntParameter(request, "selectedActivity");
        if (selectedActivityId != null) {
            refdata.put("selectedActivity", activityDao.getById(selectedActivityId));
        }

        refdata.put("sources", sourceDao.getAll());
        refdata.put("activityTypes", ActivityType.values());
        refdata.put("activities", activityDao.getAll());
        refdata.put("activitiesById", DomainObjectTools.byId(activityDao.getAll()));
        Study study = templateService.findStudy(command.getPeriod());
        Amendment amendment = study.getDevelopmentAmendment();
        refdata.put("amendment", amendment);
        refdata.put("developmentRevision", amendment);
        refdata.put("revisionChanges", new RevisionChanges(daoFinder, amendment, study, command.getPeriod()));
        getControllerTools().addHierarchyToModel(command.getPeriod(), refdata);

        return refdata;
    }

    ////// CONFIGURATION

    @Required
    public void setPeriodDao(PeriodDao periodDao) {
        this.periodDao = periodDao;
    }

    @Required
    public void setActivityDao(ActivityDao activityDao) {
        this.activityDao = activityDao;
    }

    public void setPlannedActivityDao(PlannedActivityDao plannedActivityDao) {
        this.plannedActivityDao = plannedActivityDao;
    }

    @Required
    public void setDeltaService(DeltaService templateService) {
        this.deltaService = templateService;
    }

    @Required
    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

    @Required
    public void setDaoFinder(DaoFinder daoFinder) {
        this.daoFinder = daoFinder;
    }

    @Required
    public void setSourceDao(SourceDao sourceDao) {
        this.sourceDao = sourceDao;
    }

    private static class Crumb extends DefaultCrumb {
        @Override
        public String getName(BreadcrumbContext context) {
            Period p = context.getPeriod();
            if (p.getName() != null) {
                return "Manage " + p.getName();
            } else {
                return "Manage period";
            }
        }

        @Override
        public Map<String, String> getParameters(BreadcrumbContext context) {
            return Collections.singletonMap("id", context.getPeriod().getId().toString());
        }
    }
}
