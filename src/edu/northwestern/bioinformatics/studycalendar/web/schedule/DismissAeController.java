package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.dao.AdverseEventNotificationDao;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTools;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.validation.BindException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
@AccessControl(protectionGroups = StudyCalendarProtectionGroup.PARTICIPANT_COORDINATOR)
public class DismissAeController extends AbstractCommandController {
    private AdverseEventNotificationDao notificationDao;

    public DismissAeController() {
        setCommandClass(DismissAeCommand.class);
    }

    protected Object getCommand(HttpServletRequest request) throws Exception {
        return new DismissAeCommand(notificationDao);
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        ControllerTools.registerDomainObjectEditor(binder, "notification", notificationDao);
    }

    @SuppressWarnings("unchecked")
    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {
        DismissAeCommand command = ((DismissAeCommand) oCommand);
        command.dismiss();
        Map<String, Object> model = errors.getModel();
        model.put("notification", command.getNotification());
        return new ModelAndView("schedule/ajax/dismissAe", model);
    }

    ////// CONFIGURATION

    public void setNotificationDao(AdverseEventNotificationDao notificationDao) {
        this.notificationDao = notificationDao;
    }
}
