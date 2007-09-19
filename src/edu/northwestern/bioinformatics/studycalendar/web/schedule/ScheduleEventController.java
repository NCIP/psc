package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.validation.BindException;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledEventDao;
import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEventMode;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.utils.editors.ControlledVocabularyEditor;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.BreadcrumbContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;

/**
 * @author Rhett Sutphin
 */
@AccessControl(roles = Role.PARTICIPANT_COORDINATOR)
public class ScheduleEventController extends PscSimpleFormController {
    private ScheduledCalendarDao scheduledCalendarDao;
    private ScheduledEventDao scheduledEventDao;

    public ScheduleEventController() {
        setBindOnNewForm(true);
        setCommandClass(ScheduleEventCommand.class);
        setCrumb(new Crumb());
    }

    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        return new ScheduleEventCommand(scheduledCalendarDao);
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
        binder.registerCustomEditor(Date.class, getControllerTools().getDateEditor(true));
        getControllerTools().registerDomainObjectEditor(binder, "event", scheduledEventDao);
        binder.registerCustomEditor(ScheduledEventMode.class, "newMode",
            new ControlledVocabularyEditor(ScheduledEventMode.class, true));
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    protected ModelAndView showForm(
        HttpServletRequest request, HttpServletResponse response, BindException errors
    ) throws Exception {
        Map<String, Object> model = errors.getModel();
        ScheduleEventCommand command = (ScheduleEventCommand) errors.getTarget();
        getControllerTools().addHierarchyToModel(command.getEvent(), model);
//        model.put("modes", ScheduledEventMode.values());
        model.put("modes", command.getEventSpecificMode());
        return new ModelAndView("schedule/event", model);
    }

    protected ModelAndView onSubmit(Object oCommand) throws Exception {
        ScheduleEventCommand command = (ScheduleEventCommand) oCommand;
        command.apply();
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

    private class Crumb extends DefaultCrumb {
        public String getName(BreadcrumbContext context) {
            ScheduledEvent evt = context.getScheduledEvent();
            return new StringBuilder()
                .append(evt.getActivity().getName())
                .append(" on ")
                .append(getControllerTools().formatDate(evt.getActualDate()))
                .toString();
        }

        public Map<String, String> getParameters(BreadcrumbContext context) {
            return createParameters("event", context.getScheduledEvent().getId().toString());
        }
    }
}
