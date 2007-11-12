package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityDao;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;
import edu.northwestern.bioinformatics.studycalendar.utils.editors.ControlledVocabularyEditor;
import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractCommandController;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Rhett Sutphin
 */
@AccessControl(roles = Role.PARTICIPANT_COORDINATOR)
public class BatchRescheduleController extends PscAbstractCommandController<BatchRescheduleCommand> {

    private ScheduledActivityDao scheduledActivityDao;
    private ScheduledCalendarDao scheduledCalendarDao;

    public BatchRescheduleController() {
        setCommandClass(BatchRescheduleCommand.class);
    }

    protected Object getCommand(HttpServletRequest request) throws Exception {
        return new BatchRescheduleCommand(scheduledCalendarDao);
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);

        getControllerTools().registerDomainObjectEditor(binder, "events", scheduledActivityDao);
        getControllerTools().registerDomainObjectEditor(binder, "scheduledCalendar", scheduledCalendarDao);
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
        binder.registerCustomEditor(ScheduledActivityMode.class, "newMode", new ControlledVocabularyEditor(ScheduledActivityMode.class));
    }

    @Override
    protected ModelAndView handle(BatchRescheduleCommand command, BindException errors, HttpServletRequest request, HttpServletResponse response) throws Exception {
        command.apply();
        return new ModelAndView("schedule/batchReschedule", "scheduledCalendar", command.getScheduledCalendar());
    }

    ////// CONFIGURATION

    @Required
    public void setScheduledActivityDao(ScheduledActivityDao scheduledActivityDao) {
        this.scheduledActivityDao = scheduledActivityDao;
    }

    public void setScheduledCalendarDao(ScheduledCalendarDao scheduledCalendarDao) {
        this.scheduledCalendarDao = scheduledCalendarDao;
    }
}
