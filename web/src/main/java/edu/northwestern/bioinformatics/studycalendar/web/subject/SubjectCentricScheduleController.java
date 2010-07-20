package edu.northwestern.bioinformatics.studycalendar.web.subject;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SubjectDao;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.service.AuthorizationService;
import edu.northwestern.bioinformatics.studycalendar.service.DomainContext;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractController;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import gov.nih.nci.cabig.ctms.dao.GridIdentifiableDao;
import gov.nih.nci.cabig.ctms.domain.DomainObject;
import gov.nih.nci.cabig.ctms.domain.GridIdentifiable;
import gov.nih.nci.cabig.ctms.editors.DaoBasedEditor;
import gov.nih.nci.cabig.ctms.editors.GridIdentifiableDaoBasedEditor;
import gov.nih.nci.cabig.ctms.lang.NowFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_SUBJECT_CALENDAR_MANAGER;

/**
 * @author Rhett Sutphin
 */
public class SubjectCentricScheduleController extends PscAbstractController implements PscAuthorizedHandler {
    private SubjectDao subjectDao;
    private StudySubjectAssignmentDao studySubjectAssignmentDao;
    private ScheduledCalendarDao scheduledCalendarDao;
    private AuthorizationService authorizationService;
    private NowFactory nowFactory;
    private ApplicationSecurityManager applicationSecurityManager;

    public SubjectCentricScheduleController() {
        setCrumb(new Crumb());
    }

    public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
        return ResourceAuthorization.createCollection(STUDY_SUBJECT_CALENDAR_MANAGER);
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Subject subject = interpretUsingIdOrGridId(request, "subject", subjectDao);

        // Try assignment
        if (subject == null) {
            StudySubjectAssignment assignment = interpretUsingIdOrGridId(request, "assignment", studySubjectAssignmentDao);
            if (assignment != null) subject = assignment.getSubject();
        }
        // Try calendar
        if (subject == null) {
            ScheduledCalendar calendar = interpretUsingIdOrGridId(request, "calendar", scheduledCalendarDao);
            if (calendar != null) subject = calendar.getAssignment().getSubject();
        }
        
        if (subject == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "No matching subject found");
            return null;
        }

        List<StudySubjectAssignment> allAssignments = subject.getAssignments();
        List<StudySubjectAssignment> visibleAssignments
            = authorizationService.filterAssignmentsForVisibility(allAssignments, applicationSecurityManager.getUser().getLegacyUser());
        Set<StudySubjectAssignment> hiddenAssignments
            = new LinkedHashSet<StudySubjectAssignment>(allAssignments);
        for (StudySubjectAssignment visibleAssignment : visibleAssignments) {
            hiddenAssignments.remove(visibleAssignment);
        }
        SubjectCentricSchedule schedule = new SubjectCentricSchedule(
            visibleAssignments, new ArrayList<StudySubjectAssignment>(hiddenAssignments), nowFactory);

        ModelMap model = new ModelMap("schedule", schedule);
        model.addObject(subject);
        model.addAttribute("schedulePreview", false);
        model.addAttribute("subjectCoordinator", applicationSecurityManager.getUser().getLegacyUser());
        return new ModelAndView("subject/schedule", model);
    }

    private <T extends GridIdentifiable & DomainObject> T interpretUsingIdOrGridId(
        HttpServletRequest request, String paramName, GridIdentifiableDao<T> dao
    ) throws ServletRequestBindingException {
        DaoBasedEditor editor = new GridIdentifiableDaoBasedEditor(dao);
        editor.setAsText(ServletRequestUtils.getStringParameter(request, paramName));
        return (T) editor.getValue();
    }

    ////// CONFIGURATION

    @Required
    public void setSubjectDao(SubjectDao subjectDao) {
        this.subjectDao = subjectDao;
    }

    @Required
    public void setStudySubjectAssignmentDao(StudySubjectAssignmentDao studySubjectAssignmentDao) {
        this.studySubjectAssignmentDao = studySubjectAssignmentDao;
    }

    @Required
    public void setScheduledCalendarDao(ScheduledCalendarDao scheduledCalendarDao) {
        this.scheduledCalendarDao = scheduledCalendarDao;
    }

    @Required
    public void setAuthorizationService(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @Required
    public void setNowFactory(NowFactory nowFactory) {
        this.nowFactory = nowFactory;
    }

    @Required
    public void setApplicationSecurityManager(ApplicationSecurityManager applicationSecurityManager) {
        this.applicationSecurityManager = applicationSecurityManager;
    }

    private static class Crumb extends DefaultCrumb {
        @Override
        public String getName(DomainContext context) {
            return new StringBuilder()
                .append("Comprehensive schedule for ").append(context.getSubject().getFullName())
                .toString();
        }

        @Override
        public Map<String, String> getParameters(DomainContext context) {
            return createParameters(
                "subject", context.getSubject().getId().toString()
            );
        }
    }
}
