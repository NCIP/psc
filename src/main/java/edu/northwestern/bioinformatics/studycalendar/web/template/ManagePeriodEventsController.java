package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedEventDao;
import edu.northwestern.bioinformatics.studycalendar.dao.DaoFinder;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.utils.DomainObjectTools;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.BreadcrumbContext;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
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
import edu.northwestern.bioinformatics.studycalendar.domain.Role;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
@AccessControl(roles = Role.STUDY_COORDINATOR)
public class ManagePeriodEventsController  extends PscSimpleFormController {
    private PeriodDao periodDao;
    private ActivityDao activityDao;
    private PlannedEventDao plannedEventDao;
    private StudyService studyService;
    private DeltaService deltaService;
    private AmendmentService amendmentService;
    private TemplateService templateService;
    private DaoFinder daoFinder;

    public ManagePeriodEventsController() {
        setBindOnNewForm(true);
        setCommandClass(ManagePeriodEventsCommand.class);
        setFormView("managePeriod");
        setCrumb(new Crumb());
    }

    @Override
    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        int periodId = ServletRequestUtils.getRequiredIntParameter(request, "id");
        Period period = periodDao.getById(periodId);
        if (!isFormSubmission(request)) { // TODO: the need for this branch points up that the AJAX requests should be handled by a different controller
            period = deltaService.revise(period);
        }
        return new ManagePeriodEventsCommand(period, plannedEventDao, amendmentService);
    }

    @Override
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        binder.registerCustomEditor(Boolean.class, new CustomBooleanEditor(false));
        binder.registerCustomEditor(String.class, "grid.details", new StringTrimmerEditor(true));
        binder.registerCustomEditor(String.class, "grid.conditionalDetails", new StringTrimmerEditor(true));
        getControllerTools().registerDomainObjectEditor(binder, "grid.activity", activityDao);
        binder.registerCustomEditor(Integer.class, new CustomNumberEditor(Integer.class, false));
    }

    @Override
    protected Map<String, Object> referenceData(
        HttpServletRequest request, Object oCommand, Errors errors
    ) throws Exception {
        ManagePeriodEventsCommand command = (ManagePeriodEventsCommand) oCommand;

        Map<String, Object> refdata = new HashMap<String, Object>();

        Integer selectedActivityId = ServletRequestUtils.getIntParameter(request, "selectedActivity");
        if (selectedActivityId != null) {
            refdata.put("selectedActivity", activityDao.getById(selectedActivityId));
        }

        refdata.put("activityTypes", ActivityType.values());
        refdata.put("activities", activityDao.getAll());
        refdata.put("activitiesById", DomainObjectTools.byId(activityDao.getAll()));
        Study study = templateService.findStudy(command.getPeriod());
        Amendment amendment = study.getDevelopmentAmendment();
        refdata.put("amendment", amendment);
        refdata.put("developmentRevision", amendment);
        refdata.put("revisionChanges", new RevisionChanges(daoFinder, amendment, study, command.getPeriod()));
        getControllerTools().addHierarchyToModel(command.getPeriod(), refdata);
        System.out.println("");

        return refdata;
    }

    @Override
    protected ModelAndView processFormSubmission(
        HttpServletRequest request, HttpServletResponse response, Object oCommand,
        BindException errors
    ) throws Exception {
        ManagePeriodEventsCommand command = (ManagePeriodEventsCommand) oCommand;
        PlannedEvent event = command.apply();
        studyService.saveStudyFor(command.getPeriod());
        ManagePeriodEventsCommand.GridRow row = command.getOldRow();

        Map<String, Object> map = new HashMap<String, Object>();
        if (event != null) {
            map.put("id", event.getId());
        }
        map.put("rowNumber", row.getRowNumber());
        map.put("columnNumber", row.getColumnNumber());
        getControllerTools().addHierarchyToModel(command.getPeriod(), map);
        return new ModelAndView("template/ajax/updateManagePeriod", map);
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

    @Required
    public void setPlannedEventDao(PlannedEventDao plannedEventDao) {
        this.plannedEventDao = plannedEventDao;
    }

    @Required
    public void setAmendmentService(AmendmentService amendmentService) {
        this.amendmentService = amendmentService;
    }

    @Required
    public void setDeltaService(DeltaService templateService) {
        this.deltaService = templateService;
    }

    @Required
    public void setStudyService(StudyService studyService) {
        this.studyService = studyService;
    }

    @Required
    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

    @Required
    public void setDaoFinder(DaoFinder daoFinder) {
        this.daoFinder = daoFinder;
    }

    private static class Crumb extends DefaultCrumb {
        @Override
        public String getName(BreadcrumbContext context) {
            Period p = context.getPeriod();
            if (p.getName() != null) {
                return p.getName();
            } else {
                return "Period";
            }
        }

        @Override
        public Map<String, String> getParameters(BreadcrumbContext context) {
            return Collections.singletonMap("id", context.getPeriod().getId().toString());
        }
    }
}
