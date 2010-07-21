package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.service.DomainContext;
import edu.northwestern.bioinformatics.studycalendar.service.ScheduleService;
import edu.northwestern.bioinformatics.studycalendar.tools.FormatTools;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.utils.editors.ControlledVocabularyEditor;
import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER;

/**
 * @author Rhett Sutphin
 */
@AccessControl(roles = Role.SUBJECT_COORDINATOR)
public class ScheduleActivityController extends PscSimpleFormController implements PscAuthorizedHandler {
    private ScheduledCalendarDao scheduledCalendarDao;
    private ScheduledActivityDao scheduledActivityDao;
    private ScheduleService scheduleService;

    public ScheduleActivityController() {
        setBindOnNewForm(true);
        setCommandClass(ScheduleActivityCommand.class);
        setCrumb(new Crumb());
    }

    public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
        return ResourceAuthorization.createCollection(STUDY_CALENDAR_TEMPLATE_BUILDER);
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
        Map<String,String> uriMap = scheduleService.generateActivityTemplateUri(command.getEvent());
        model.put("uriMap",uriMap);
        model.put("modes", command.getEventSpecificMode());
        return new ModelAndView("schedule/event", model);
    }

    protected ModelAndView onSubmit(Object oCommand) throws Exception {
        ScheduleActivityCommand command = (ScheduleActivityCommand) oCommand;
        command.apply();
        Map<String, Object> model = new HashMap<String, Object>();
        ScheduledStudySegment studySegment = command.getEvent().getScheduledStudySegment();
        model.put("subject", studySegment.getScheduledCalendar().getAssignment().getSubject().getId());
        return new ModelAndView("redirectToSchedule", model);
    }

    ////// CONFIGURATION

    public void setScheduledCalendarDao(ScheduledCalendarDao scheduledCalendarDao) {
        this.scheduledCalendarDao = scheduledCalendarDao;
    }

    public void setScheduledActivityDao(ScheduledActivityDao scheduledActivityDao) {
        this.scheduledActivityDao = scheduledActivityDao;
    }

    public void setScheduleService(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    private class Crumb extends DefaultCrumb {
        public String getName(DomainContext context) {
            ScheduledActivity evt = context.getScheduledActivity();
            return new StringBuilder()
                .append(evt.getActivity().getName())
                .append(" on ")
                .append(FormatTools.getLocal().formatDate(evt.getActualDate()))
                .toString();
        }

        public Map<String, String> getParameters(DomainContext context) {
            return createParameters("event", context.getScheduledActivity().getId().toString());
        }
    }
}
