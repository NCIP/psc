package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.validation.BindException;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityDao;
import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.utils.editors.ControlledVocabularyEditor;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.BreadcrumbContext;
import edu.northwestern.bioinformatics.studycalendar.utils.FormatTools;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;

/**
 * @author Rhett Sutphin
 */
@AccessControl(roles = Role.SUBJECT_COORDINATOR)
public class ScheduleActivityController extends PscSimpleFormController {
    private ScheduledCalendarDao scheduledCalendarDao;
    private ScheduledActivityDao scheduledActivityDao;

    public ScheduleActivityController() {
        setBindOnNewForm(true);
        setCommandClass(ScheduleActivityCommand.class);
        setCrumb(new Crumb());
    }

    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        return new ScheduleActivityCommand(scheduledCalendarDao);
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
        binder.registerCustomEditor(Date.class, getControllerTools().getDateEditor(true));
        getControllerTools().registerDomainObjectEditor(binder, "event", scheduledActivityDao);
        binder.registerCustomEditor(ScheduledActivityMode.class, "newMode",
            new ControlledVocabularyEditor(ScheduledActivityMode.class, true));
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    protected ModelAndView showForm(
        HttpServletRequest request, HttpServletResponse response, BindException errors
    ) throws Exception {
        Map<String, Object> model = errors.getModel();
        ScheduleActivityCommand command = (ScheduleActivityCommand) errors.getTarget();
        getControllerTools().addHierarchyToModel(command.getEvent(), model);
//        model.put("modes", ScheduledActivityMode.values());
        model.put("modes", command.getEventSpecificMode());
        return new ModelAndView("schedule/event", model);
    }

    protected ModelAndView onSubmit(Object oCommand) throws Exception {
        ScheduleActivityCommand command = (ScheduleActivityCommand) oCommand;
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

    public void setScheduledActivityDao(ScheduledActivityDao scheduledActivityDao) {
        this.scheduledActivityDao = scheduledActivityDao;
    }

    private class Crumb extends DefaultCrumb {
        public String getName(BreadcrumbContext context) {
            ScheduledActivity evt = context.getScheduledActivity();
            return new StringBuilder()
                .append(evt.getActivity().getName())
                .append(" on ")
                .append(FormatTools.formatDate(evt.getActualDate()))
                .toString();
        }

        public Map<String, String> getParameters(BreadcrumbContext context) {
            return createParameters("event", context.getScheduledActivity().getId().toString());
        }
    }
}
