package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedEventDao;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedEvent;
import edu.northwestern.bioinformatics.studycalendar.utils.DomainObjectTools;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.BreadcrumbContext;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
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
@AccessControl(protectionGroups = StudyCalendarProtectionGroup.STUDY_COORDINATOR)
public class ManagePeriodEventsController  extends PscSimpleFormController {
    private PeriodDao periodDao;
    private ActivityDao activityDao;
    private PlannedEventDao plannedEventDao;

    public ManagePeriodEventsController() {
        setBindOnNewForm(true);
        setCommandClass(ManagePeriodEventsCommand.class);
        setFormView("managePeriod");
        setCrumb(new Crumb());
    }

    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        int id = ServletRequestUtils.getRequiredIntParameter(request, "id");
        if (plannedEventDao == null) {
            throw new NullPointerException("MY NULL POINTER EXCEPTION");
        }
        Period period = periodDao.getById(id);
        period.getPlannedEvents().size();
        periodDao.evict(period);
        return new ManagePeriodEventsCommand(period, plannedEventDao);
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        binder.registerCustomEditor(Boolean.class, new CustomBooleanEditor(false));
        binder.registerCustomEditor(String.class, "grid.details", new StringTrimmerEditor(true));
        binder.registerCustomEditor(String.class, "grid.conditionalDetails", new StringTrimmerEditor(true));
        getControllerTools().registerDomainObjectEditor(binder, "grid.activity", activityDao);
        binder.registerCustomEditor(Integer.class, new CustomNumberEditor(Integer.class, false));
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

        refdata.put("activityTypes", ActivityType.values());
        refdata.put("activities", activityDao.getAll());
        refdata.put("activitiesById", DomainObjectTools.byId(activityDao.getAll()));
        getControllerTools().addHierarchyToModel(command.getPeriod(), refdata);

        return refdata;
    }

    protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response,
                                                 Object oCommand, BindException errors) throws Exception {
        ManagePeriodEventsCommand command = (ManagePeriodEventsCommand) oCommand;
        PlannedEvent event = command.apply();
        ManagePeriodEventsCommand.GridRow row = command.getOldRow();
        Map<String, Object> map = new HashMap<String, Object>();
        //map.put("grid", command.createGrid());
        if (event!=null) {
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

    private static class Crumb extends DefaultCrumb {
        public String getName(BreadcrumbContext context) {
            Period p = context.getPeriod();
            if (p.getName() != null) {
                return p.getName();
            } else {
                return "Period";
            }
        }

        public Map<String, String> getParameters(BreadcrumbContext context) {
            return Collections.singletonMap("id", context.getPeriod().getId().toString());
        }
    }
}
