/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.tools.FormatTools;
import gov.nih.nci.cabig.ctms.dao.GridIdentifiableDao;
import gov.nih.nci.cabig.ctms.editors.DaoBasedEditor;
import gov.nih.nci.cabig.ctms.editors.GridIdentifiableDaoBasedEditor;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.beans.PropertyEditor;
import java.io.IOException;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class ControllerTools {
    private TemplateService templateService;

    /**
     * Create a new CustomDateEditor instance, using the given DateFormat
     * for parsing and rendering.
     * <p>The "required" parameter states if an empty String should not
     * be allowed for parsing, i.e. get interpreted as null value.
     * Otherwise, an IllegalArgumentException gets thrown in that case.
     * <p>The "exactDateLength" parameter states that IllegalArgumentException gets
     * thrown if the String does not exactly match the length specified. This is useful
     * because SimpleDateFormat does not enforce strict parsing of the year part,
     * not even with <code>setLenient(false)</code>. Without an "exactDateLength"
     * specified, the "01/01/05" would get parsed to "01/01/0005".
     *
     */
    public PropertyEditor getDateEditor(boolean required) {
        // note that date formats are not threadsafe, so we have to create a new one each time
        return new DateCustomEditor(FormatTools.getLocal().getDateFormat(), !required);
    }

    public void registerDomainObjectEditor(ServletRequestDataBinder binder, String field, StudyCalendarDao<?> dao) {
        DaoBasedEditor editor;
        if (dao instanceof GridIdentifiableDao) {
            editor = new GridIdentifiableDaoBasedEditor((GridIdentifiableDao<?>) dao);
        } else {
            editor = new DaoBasedEditor(dao);
        }
        binder.registerCustomEditor(dao.domainClass(), field, editor);
    }

    public void addHierarchyToModel(ScheduledActivity event, Map<String, Object> model) {
        model.put("scheduledActivity", event);
        if (event != null) {
            addHierarchyToModel(event.getPlannedActivity(), model);
            addHierarchyToModel(event.getScheduledStudySegment(), model);
        }
    }

    public void addHierarchyToModel(ScheduledStudySegment studySegment, Map<String, Object> model) {
        model.put("scheduledStudySegment", studySegment);
        if (studySegment != null) {
            addHierarchyToModel(studySegment.getStudySegment(), model);
            addHierarchyToModel(studySegment.getScheduledCalendar(), model);
        }
    }

    public void addHierarchyToModel(ScheduledCalendar calendar, Map<String, Object> model) {
        model.put("scheduledCalendar", calendar);
        if (calendar != null) {
            addHierarchyToModel(calendar.getAssignment().getStudySite().getStudy().getPlannedCalendar(), model);
            addHierarchyToModel(calendar.getAssignment(), model);
        }
    }

    public void addHierarchyToModel(StudySubjectAssignment assignment, Map<String, Object> model) {
        model.put("assignment", assignment);
        if (assignment != null) {
            model.put("subject", assignment.getSubject());
            model.put("scheduledCalendar", assignment.getScheduledCalendar());
            addToModel(assignment.getStudySite(), model);
        }
    }

    public void addHierarchyToModel(PlannedActivity event, Map<String, Object> model) {
        model.put("plannedActivity", event);
        if (event != null) addHierarchyToModel(templateService.findParent(event), model);
    }

    public void addHierarchyToModel(Period period, Map<String, Object> model) {
        model.put("period", period);
        if (period != null) addHierarchyToModel(templateService.findParent(period), model);
    }

    public void addHierarchyToModel(StudySegment studySegment, Map<String, Object> model) {
        model.put("studySegment", studySegment);
        if (studySegment != null) addHierarchyToModel(templateService.findParent(studySegment), model);
    }

    public void addHierarchyToModel(Epoch epoch, Map<String, Object> model) {
        model.put("epoch", epoch);
        if (epoch != null) addHierarchyToModel(templateService.findParent(epoch), model);
    }

    public void addHierarchyToModel(PlannedCalendar plannedCalendar, Map<String, Object> model) {
        model.put("plannedCalendar", plannedCalendar);
        if (plannedCalendar != null) addHierarchyToModel(plannedCalendar.getStudy(), model);
    }

    public void addHierarchyToModel(Study study, Map<String, Object> model) {
        model.put("study", study);
    }

    public void addToModel(StudySite studySite, Map<String, Object> model) {
        model.put("studySite", studySite);
        model.put("study", studySite.getStudy());
        model.put("site", studySite.getSite());
    }

    public void addToModel(Population population, Map<String, Object> model) {
        model.put("population", population);
        addHierarchyToModel(population.getStudy(), model);
    }

    public boolean isAjaxRequest(HttpServletRequest request) {
        String header = request.getHeader("X-Requested-With");
        return header != null && "XMLHttpRequest".equals(header);
    }

    public ModelAndView redirectToCalendarTemplate(int studyId) {
        return redirectToCalendarTemplate(studyId, null);
    }

    @SuppressWarnings({ "unchecked" })
    public ModelAndView redirectToCalendarTemplate(int studyId, Integer selectedStudySegmentId) {
        return redirectToCalendarTemplate(studyId, selectedStudySegmentId, null);
    }

    @SuppressWarnings({ "unchecked" })
    public ModelAndView redirectToCalendarTemplate(int studyId, Integer selectedStudySegmentId, Integer selectedAmendmentId) {
        ModelMap model = new ModelMap("study", studyId);
        if (selectedStudySegmentId != null) model.put("studySegment", selectedStudySegmentId);
        if (selectedAmendmentId != null) model.put("amendment", selectedAmendmentId);
        return new ModelAndView("redirectToCalendarTemplate", model);
    }

    public ModelAndView redirectToSchedule(int assignmentId) {
        ModelMap model = new ModelMap("assignment", assignmentId);
        return new ModelAndView("redirectToSchedule", model);
    }

    // note that if you change the error message here, you need to change it in error-console.js, too
    public void sendPostOnlyError(HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "POST is the only valid method for this URL");
    }

    // note that if you change the error message here, you need to change it in error-console.js, too
    public void sendGetOnlyError(HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "GET is the only valid method for this URL");
    }

    /**
     * @see UserInRequestFilter
     */
    public PscUser getCurrentUser(HttpServletRequest request) {
        return (PscUser) request.getAttribute("currentUser");
    }

    ////// CONFIGURATION

    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }
}
