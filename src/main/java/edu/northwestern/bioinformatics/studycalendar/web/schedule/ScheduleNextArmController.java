package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ArmDao;
import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractCommandController;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;

import java.util.Date;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
@AccessControl(roles = Role.SUBJECT_COORDINATOR)
public class ScheduleNextArmController extends PscAbstractCommandController<ScheduleNextArmCommand> {
    private SubjectService subjectService;
    private ScheduledCalendarDao scheduledCalendarDao;
    private ArmDao armDao;

    public ScheduleNextArmController() {
        setCommandClass(ScheduleNextArmCommand.class);
    }

    @Override
    protected Object getCommand(HttpServletRequest request) throws Exception {
        return new ScheduleNextArmCommand(subjectService);
    }

    @Override
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
        getControllerTools().registerDomainObjectEditor(binder, "arm", armDao);
        getControllerTools().registerDomainObjectEditor(binder, "calendar", scheduledCalendarDao);
        binder.registerCustomEditor(Date.class, getControllerTools().getDateEditor(true));
    }

    @Override
    protected ModelAndView handle(ScheduleNextArmCommand command, BindException errors, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> model = errors.getModel();
        ScheduledArm newArm = command.schedule();
        model.put("scheduledArm", newArm);
        model.put("nextPerProtocolDate", newArm.getNextArmPerProtocolStartDate());
        return new ModelAndView("schedule/ajax/scheduleNextArm", model);
    }

    /////// CONFIGURATION

    @Required
    public void setSubjectService(SubjectService subjectService) {
        this.subjectService = subjectService;
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
