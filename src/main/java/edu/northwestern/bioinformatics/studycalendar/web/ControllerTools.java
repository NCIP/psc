package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.utils.FormatTools;

import java.util.Map;
import java.beans.PropertyEditor;
import java.io.IOException;

import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.ui.ModelMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gov.nih.nci.cabig.ctms.editors.DaoBasedEditor;

/**
 * @author Rhett Sutphin
 */
public class ControllerTools {
    private TemplateService templateService;

    // TODO: make date format externally configurable
    public PropertyEditor getDateEditor(boolean required) {
        // note that date formats are not threadsafe, so we have to create a new one each time
        return new CustomDateEditor(FormatTools.createDateFormat(), !required);
    }

    public void registerDomainObjectEditor(ServletRequestDataBinder binder, String field, StudyCalendarDao<?> dao) {
        binder.registerCustomEditor(dao.domainClass(), field, new DaoBasedEditor(dao));
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

    // note that if you change the error message here, you need to change it in error-console.js, too
    public void sendPostOnlyError(HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "POST is the only valid method for this URL");
    }

    ////// CONFIGURATION

    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }
}
