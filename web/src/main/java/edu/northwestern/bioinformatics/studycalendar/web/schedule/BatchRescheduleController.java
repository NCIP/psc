package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.editors.ControlledVocabularyEditor;
import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractCommandController;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Map;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_SUBJECT_CALENDAR_MANAGER;

/**
 * @author Rhett Sutphin
 */
@AccessControl(roles = Role.SUBJECT_COORDINATOR)
public class BatchRescheduleController extends PscAbstractCommandController<BatchRescheduleCommand> implements PscAuthorizedHandler {

    private ScheduledActivityDao scheduledActivityDao;
    private ScheduledCalendarDao scheduledCalendarDao;

    public BatchRescheduleController() {
        setCommandClass(BatchRescheduleCommand.class);
    }

    public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
        return ResourceAuthorization.createCollection(STUDY_SUBJECT_CALENDAR_MANAGER);
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
