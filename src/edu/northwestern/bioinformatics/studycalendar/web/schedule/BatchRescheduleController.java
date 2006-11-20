package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEventMode;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;
import edu.northwestern.bioinformatics.studycalendar.utils.editors.ControlledVocabularyEditor;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTools;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Rhett Sutphin
 */
@AccessControl(protectionGroups = StudyCalendarProtectionGroup.PARTICIPANT_COORDINATOR)
public class BatchRescheduleController extends AbstractCommandController {
    private ScheduledCalendarDao scheduledCalendarDao;

    public BatchRescheduleController() {
        super(BatchRescheduleCommand.class);
    }

    protected Object getCommand(HttpServletRequest request) throws Exception {
        return new BatchRescheduleCommand(scheduledCalendarDao);
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
        ControllerTools.registerDomainObjectEditor(binder, "scheduledCalendar", scheduledCalendarDao);
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
        binder.registerCustomEditor(ScheduledEventMode.class, "newMode", new ControlledVocabularyEditor(ScheduledEventMode.class));
    }

    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {
        BatchRescheduleCommand command = (BatchRescheduleCommand) oCommand;
        command.apply();
        return new ModelAndView("schedule/batchReschedule", "scheduledCalendar", command.getScheduledCalendar());
    }

    ////// CONFIGURATION

    @Required
    public void setScheduledCalendarDao(ScheduledCalendarDao scheduledCalendarDao) {
        this.scheduledCalendarDao = scheduledCalendarDao;
    }
}
