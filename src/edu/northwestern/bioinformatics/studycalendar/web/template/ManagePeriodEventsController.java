package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.utils.DomainObjectTools;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.BreadcrumbContext;
import edu.northwestern.bioinformatics.studycalendar.utils.editors.ManagePeriodCountEditor;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTools;
import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.hibernate.reflection.java.xml.XMLContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

/**
 * @author Rhett Sutphin
 */
@AccessControl(protectionGroups = StudyCalendarProtectionGroup.STUDY_COORDINATOR)
public class ManagePeriodEventsController extends PscSimpleFormController {
    private PeriodDao periodDao;
    private ActivityDao activityDao;

    public ManagePeriodEventsController() {
        setBindOnNewForm(true);
        setCommandClass(ManagePeriodEventsCommand.class);
        setFormView("managePeriod");
        setCrumb(new Crumb());
    }

    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        int id = ServletRequestUtils.getRequiredIntParameter(request, "id");
        return new ManagePeriodEventsCommand(periodDao.getById(id));
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        binder.registerCustomEditor(Integer.class, new ManagePeriodCountEditor());
        binder.registerCustomEditor(String.class, "grid.details", new StringTrimmerEditor(true));
        ControllerTools.registerDomainObjectEditor(binder, "grid.activity", activityDao);
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
        ControllerTools.addHierarchyToModel(command.getPeriod(), refdata);

        return refdata;
    }

    protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {
        ManagePeriodEventsCommand command = (ManagePeriodEventsCommand) oCommand;
        command.apply();
        periodDao.save(command.getPeriod());
        Arm arm = command.getPeriod().getArm();
        Integer studyId = arm.getEpoch().getPlannedCalendar().getStudy().getId();
        return ControllerTools.redirectToCalendarTemplate(studyId, arm.getId());
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
