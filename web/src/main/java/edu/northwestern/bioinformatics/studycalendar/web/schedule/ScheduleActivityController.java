package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.service.ActivityService;
import edu.northwestern.bioinformatics.studycalendar.tools.FormatTools;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.BreadcrumbContext;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.utils.editors.ControlledVocabularyEditor;
import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.AccessControl;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Rhett Sutphin
 */
@AccessControl(roles = Role.SUBJECT_COORDINATOR)
public class ScheduleActivityController extends PscSimpleFormController {
    private ScheduledCalendarDao scheduledCalendarDao;
    private ScheduledActivityDao scheduledActivityDao;
    private ActivityService activityService;

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
        Map<String,String> uriMap = new TreeMap<String,String>();
        Collection<List<String>> uriList = activityService.createActivityUriList(command.getEvent().getActivity()).values();
        Iterator iterator = uriList.iterator();
        while(iterator.hasNext()) {
            List<String> values = (List)(iterator.next());
            uriMap.put(values.get(0),values.get(1));
        }
        model.put("uri",uriMap);
//        model.put("modes", ScheduledActivityMode.values());
        model.put("modes", command.getEventSpecificMode());
        return new ModelAndView("schedule/event", model);
    }

    protected ModelAndView onSubmit(Object oCommand) throws Exception {
        ScheduleActivityCommand command = (ScheduleActivityCommand) oCommand;
        command.apply();
        Map<String, Object> model = new HashMap<String, Object>();
        ScheduledStudySegment studySegment = command.getEvent().getScheduledStudySegment();
        model.put("studySegment", studySegment.getId());
        model.put("calendar", studySegment.getScheduledCalendar().getId());
        return new ModelAndView("redirectToSchedule", model);
    }

    ////// CONFIGURATION

    public void setScheduledCalendarDao(ScheduledCalendarDao scheduledCalendarDao) {
        this.scheduledCalendarDao = scheduledCalendarDao;
    }

    public void setScheduledActivityDao(ScheduledActivityDao scheduledActivityDao) {
        this.scheduledActivityDao = scheduledActivityDao;
    }

    public void setActivityService(ActivityService activityService) {
        this.activityService = activityService;
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
