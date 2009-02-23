package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractCommandController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@AccessControl(roles = Role.SUBJECT_COORDINATOR)
public class EventsRescheduleController extends PscAbstractCommandController<EventsRescheduleCommand> {

    private ScheduledCalendarDao scheduledCalendarDao;

    private static final Logger log = LoggerFactory.getLogger(EventsRescheduleController.class.getName());

    public EventsRescheduleController() {
        setCommandClass(EventsRescheduleCommand.class);
    }

    protected Object getCommand(HttpServletRequest request) throws Exception {
        return new EventsRescheduleCommand(scheduledCalendarDao);
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
        getControllerTools().registerDomainObjectEditor(binder, "scheduledCalendar", scheduledCalendarDao);
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
        binder.registerCustomEditor(Date.class, getControllerTools().getDateEditor(true));
    }

    @Override
    protected ModelAndView handle(EventsRescheduleCommand command, BindException errors, HttpServletRequest request, HttpServletResponse response) throws Exception {
        command.apply();
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("scheduledCalendar", command.getScheduledCalendar());
        return new ModelAndView("schedule/batchReschedule", model);
    }

    ////// CONFIGURATION

    public void setScheduledCalendarDao(ScheduledCalendarDao scheduledCalendarDao) {
        this.scheduledCalendarDao = scheduledCalendarDao;
    }

}

