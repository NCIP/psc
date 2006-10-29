package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.validation.BindException;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledEventDao;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTools;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEventMode;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm;
import edu.northwestern.bioinformatics.studycalendar.utils.editors.ControlledVocabularyEditor;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;

/**
 * @author Rhett Sutphin
 */
public class ScheduleEventController extends SimpleFormController {
    private ScheduledCalendarDao scheduledCalendarDao;
    private ScheduledEventDao scheduledEventDao;

    public ScheduleEventController() {
        setFormView("schedule/event");
        setBindOnNewForm(true);
        setCommandClass(ScheduleEventCommand.class);
    }

    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        return new ScheduleEventCommand(scheduledCalendarDao);
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
        binder.registerCustomEditor(Date.class, ControllerTools.getDateEditor(true));
        ControllerTools.registerDomainObjectEditor(binder, "event", scheduledEventDao);
        binder.registerCustomEditor(ScheduledEventMode.class, "newMode",
            new ControlledVocabularyEditor(ScheduledEventMode.class));
    }

    protected ModelAndView onSubmit(Object oCommand) throws Exception {
        ScheduleEventCommand command = (ScheduleEventCommand) oCommand;
        command.changeState();
        Map<String, Object> model = new HashMap<String, Object>();
        ScheduledArm arm = command.getEvent().getScheduledArm();
        model.put("arm", arm.getId());
        model.put("calendar", arm.getScheduledCalendar().getId());
        return new ModelAndView("redirectToSchedule", model);
    }

    ////// CONFIGURATION

    public void setScheduledCalendarDao(ScheduledCalendarDao scheduledCalendarDao) {
        this.scheduledCalendarDao = scheduledCalendarDao;
    }

    public void setScheduledEventDao(ScheduledEventDao scheduledEventDao) {
        this.scheduledEventDao = scheduledEventDao;
    }
}
