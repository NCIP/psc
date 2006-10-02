package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.AbstractDomainObject;
import edu.northwestern.bioinformatics.studycalendar.utils.DomainObjectTools;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractFormController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * @author Rhett Sutphin
 */
public class ManagePeriodEventsController extends AbstractFormController {
    private PeriodDao periodDao;
    private ActivityDao activityDao;

    public ManagePeriodEventsController() {
        setBindOnNewForm(true);
        setCommandClass(ManagePeriodEventsCommand.class);
    }

    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        int id = ServletRequestUtils.getRequiredIntParameter(request, "id");
        return new ManagePeriodEventsCommand(periodDao.getById(id), activityDao);
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        binder.registerCustomEditor(Integer.class, "grid", new CustomNumberEditor(Integer.class, true));
    }

    protected ModelAndView showForm(HttpServletRequest request, HttpServletResponse response, BindException errors) throws Exception {
        List<Activity> activities = activityDao.getAll();
        Map<Integer, Activity> activitiesById = DomainObjectTools.byId(activities);
        Map<String, Object> model = errors.getModel();
        model.put("activityTypes", ActivityType.values());
        model.put("activities", activities);
        model.put("activitiesById", activitiesById);
        ControllerTools.addHierarchyToModel(((ManagePeriodEventsCommand) errors.getTarget()).getPeriod(), model);
        return new ModelAndView("managePeriod", model);
    }

    private <T extends AbstractDomainObject> Map<Integer, T> byId(List<T> objs) {
        Map<Integer, T> byId = new LinkedHashMap<Integer, T>();
        for (T t : objs) {
            byId.put(t.getId(), t);
        }
        return byId;
    }

    protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {
        ManagePeriodEventsCommand command = (ManagePeriodEventsCommand) oCommand;
        command.apply();
        periodDao.save(command.getPeriod());
        return new ModelAndView("redirectToCalendarTemplate", "id", command.getPeriod().getArm().getEpoch().getPlannedCalendar().getStudy().getId());
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

}
