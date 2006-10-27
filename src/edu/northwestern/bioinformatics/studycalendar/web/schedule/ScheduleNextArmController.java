package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.northwestern.bioinformatics.studycalendar.service.ParticipantService;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ArmDao;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTools;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;

import java.util.Date;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
@AccessControl(protectionGroups = StudyCalendarProtectionGroup.PARTICIPANT_COORDINATOR)
public class ScheduleNextArmController extends AbstractCommandController {
    private ParticipantService participantService;
    private ScheduledCalendarDao scheduledCalendarDao;
    private ArmDao armDao;

    public ScheduleNextArmController() {
        setCommandClass(ScheduleNextArmCommand.class);
    }

    @Override
    protected Object getCommand(HttpServletRequest request) throws Exception {
        return new ScheduleNextArmCommand(participantService);
    }

    @Override
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
        ControllerTools.registerDomainObjectEditor(binder, "arm", armDao);
        ControllerTools.registerDomainObjectEditor(binder, "calendar", scheduledCalendarDao);
        binder.registerCustomEditor(Date.class, ControllerTools.getDateEditor(true));
    }

    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {
        ScheduleNextArmCommand command = (ScheduleNextArmCommand) oCommand;
        Map<String, Object> model = errors.getModel();
        model.put("scheduledArm", command.schedule());
        return new ModelAndView("schedule/ajax/scheduleNextArm", model);
    }

    /////// CONFIGURATION

    @Required
    public void setParticipantService(ParticipantService participantService) {
        this.participantService = participantService;
    }

    @Required
    public void setScheduledCalendarDao(ScheduledCalendarDao scheduledCalendarDao) {
        this.scheduledCalendarDao = scheduledCalendarDao;
    }

    @Required
    public void setArmDao(ArmDao armDao) {
        this.armDao = armDao;
    }
}
