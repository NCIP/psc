/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.core.editors.ControlledVocabularyEditor;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserActionDao;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.domain.UserAction;
import edu.northwestern.bioinformatics.studycalendar.domain.auditing.AuditEvent;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.DomainContext;
import edu.northwestern.bioinformatics.studycalendar.service.ScheduleService;
import edu.northwestern.bioinformatics.studycalendar.tools.FormatTools;
import edu.northwestern.bioinformatics.studycalendar.tools.spring.ApplicationPathAware;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
import edu.nwu.bioinformatics.commons.spring.ValidatableValidator;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class ScheduleActivityController extends PscSimpleFormController implements ApplicationPathAware {
    private ScheduledCalendarDao scheduledCalendarDao;
    private ScheduledActivityDao scheduledActivityDao;
    private ScheduleService scheduleService;
    private ApplicationSecurityManager applicationSecurityManager;
    private UserActionDao userActionDao;
    private String applicationPath;

    public ScheduleActivityController() {
        setBindOnNewForm(true);
        setCommandClass(ScheduleActivityCommand.class);
        setCrumb(new Crumb());
        setValidator(new ValidatableValidator());
    }

    protected Object formBackingObject(HttpServletRequest request) throws Exception {
        return new ScheduleActivityCommand(scheduledCalendarDao, applicationSecurityManager.getUser());
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
        StudySubjectAssignment ssa = command.getEvent().getScheduledStudySegment().getScheduledCalendar().getAssignment();
        return new ModelAndView("schedule/event", model);
    }

    protected ModelAndView onSubmit(Object oCommand) throws Exception {
        ScheduleActivityCommand command = (ScheduleActivityCommand) oCommand;
        associateWithUserAction(command);
        command.apply();
        Map<String, Object> model = new HashMap<String, Object>();
        ScheduledStudySegment studySegment = command.getEvent().getScheduledStudySegment();
        model.put("subject", studySegment.getScheduledCalendar().getAssignment().getSubject().getId());
        return new ModelAndView("redirectToSchedule", model);
    }

    private void associateWithUserAction(ScheduleActivityCommand command) {
        ScheduledActivity sa = command.getEvent();
        StudySubjectAssignment assignment = sa.getScheduledStudySegment().getScheduledCalendar().getAssignment();

        StringBuilder sb = new StringBuilder(applicationPath);
        Subject subject =  assignment.getSubject();
        sb.append("/api/v1/subjects/").append(subject.getGridId()).append("/schedules");
        UserAction userAction = new UserAction();
        userAction.setContext(sb.toString());
        userAction.setActionType("activity update");
        StringBuilder des = new StringBuilder(sa.getActivity().getName());
        des.append(" is updated for ").append(subject.getFullName()).append(" for ").append(assignment.getName());
        userAction.setDescription(des.toString());
        PscUser user = applicationSecurityManager.getUser();
        if (user != null) {
            userAction.setUser(user.getCsmUser());
        }

        userActionDao.save(userAction);
        AuditEvent.setUserAction(userAction);
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

    public void setApplicationSecurityManager(ApplicationSecurityManager applicationSecurityManager) {
        this.applicationSecurityManager = applicationSecurityManager;
    }

    public void setUserActionDao(UserActionDao userActionDao) {
        this.userActionDao = userActionDao;
    }

    public void setApplicationPath(String applicationPath) {
        this.applicationPath = applicationPath;
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
