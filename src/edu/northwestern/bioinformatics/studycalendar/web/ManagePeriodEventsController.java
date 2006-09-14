package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractFormController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

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
        Map<String, Object> model = errors.getModel();
        model.put("activities", activities);
        return new ModelAndView("editPeriod", model);
    }

    protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {
        ManagePeriodEventsCommand command = (ManagePeriodEventsCommand) oCommand;
        command.apply();
        periodDao.save(command.getPeriod());
        return new ModelAndView("redirectToCalendarTemplate", "id", command.getPeriod().getArm().getEpoch().getPlannedSchedule().getStudy().getId());
    }

    ////// CONFIGURATION

    public void setPeriodDao(PeriodDao periodDao) {
        this.periodDao = periodDao;
    }

    public void setActivityDao(ActivityDao activityDao) {
        this.activityDao = activityDao;
    }
}
