package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.dao.AdverseEventNotificationDao;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;
import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractCommandController;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.validation.BindException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
@AccessControl(roles = Role.PARTICIPANT_COORDINATOR)
public class DismissAeController extends PscAbstractCommandController<DismissAeCommand> {
    private AdverseEventNotificationDao notificationDao;

    public DismissAeController() {
        setCommandClass(DismissAeCommand.class);
    }

    @Override
    protected Object getCommand(HttpServletRequest request) throws Exception {
        return new DismissAeCommand(notificationDao);
    }

    @Override
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        getControllerTools().registerDomainObjectEditor(binder, "notification", notificationDao);
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    protected ModelAndView handle(DismissAeCommand command, BindException errors, HttpServletRequest request, HttpServletResponse response) throws Exception {
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
