package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.SortedSet;
import java.util.List;


/**
 * @author Jaron Sampson
 */

public class NewPlannedEventController extends SimpleFormController {
    private PeriodDao periodDao;
    private ActivityDao activityDao;

    public NewPlannedEventController() {
        setCommandClass(NewPlannedEventCommand.class);
        setFormView("editPlannedEvent");
    }

    @Override
    protected Map referenceData(HttpServletRequest request) throws Exception {
        Map<String, Object> data = new HashMap<String, Object>();
        Period period = periodDao.getById(ServletRequestUtils.getIntParameter(request, "id"));
        data.put("period", period);
        data.put("lastDay", period.getEndDay());
        data.put("activities", activityDao.getAll());
        return data;
    }

    @Override
    protected ModelAndView onSubmit(Object command) throws Exception {
        return onSubmit((NewPlannedEventCommand) command);
    }

    private ModelAndView onSubmit(NewPlannedEventCommand command) throws Exception {
        doSubmitAction(command);
        Map<String, ? extends Object> model = Collections.singletonMap("id", command.getPeriod().getId());
        return new ModelAndView(new RedirectView("/pages/studyList", true), model);
    }

    @Override
    protected void doSubmitAction(Object command) throws Exception {
        doSubmitAction((NewPlannedEventCommand) command);
    }

    private void doSubmitAction(NewPlannedEventCommand command) throws Exception {
        Period period = periodDao.getById(command.getPeriodId());
        command.setPeriod(period);
        Activity activity = activityDao.getById(command.getActivityId());
        command.setActivity(activity);
        List<PlannedEvent> plannedEvents;
        plannedEvents = period.getPlannedEvents();
        plannedEvents.add(command);
        periodDao.save(period);
    }

    @Required
    public void setPeriodDao(PeriodDao periodDao) {
        this.periodDao = periodDao;
    }

    @Required
    public void setActivityDao(ActivityDao activityDao) {
        this.activityDao = activityDao;
    }
}
