package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;
import edu.northwestern.bioinformatics.studycalendar.api.ScheduledCalendarService;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Date;

/**
 * @author Rhett Sutphin
 */
@AccessControl(protectionGroups = StudyCalendarProtectionGroup.STUDY_ADMINISTRATOR)
public class ApiController extends AbstractCommandController {
    private Log log = LogFactory.getLog(getClass());
    private ScheduledCalendarService scheduledCalendarService;

    public ApiController() {
        setCommandClass(ApiCommand.class);
    }

    protected Object getCommand(HttpServletRequest request) throws Exception {
        return new ApiCommand(scheduledCalendarService);
    }

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        binder.registerCustomEditor(Date.class, ControllerTools.getDateEditor(false));
    }

    @SuppressWarnings("unchecked")
    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object oCommand, BindException errors) throws Exception {
        ApiCommand command = (ApiCommand) oCommand;
        Map<String, Object> model = errors.getModel();
        try {
            Object result = command.execute();
            model.put("result", result);
        } catch (IllegalArgumentException iae) {
            log.info("Error in API invocation", iae);
            model.put("exception", iae);
        }
        return new ModelAndView("api", model);
    }

    ////// CONFIGURATION

    public void setScheduledCalendarService(ScheduledCalendarService scheduledCalendarService) {
        this.scheduledCalendarService = scheduledCalendarService;
    }
}
