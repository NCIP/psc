package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.domain.PlannedEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.StudyParticipantAssignment;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyCalendarDao;

import java.util.Map;
import java.util.Date;
import java.beans.PropertyEditor;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
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
    private static ThreadLocal<DateFormat> dateFormat = new ThreadLocal<DateFormat>();

    // TODO: make date format externally configurable
    public PropertyEditor getDateEditor(boolean required) {
        // note that date formats are not threadsafe, so we have to create a new one each time
        return new CustomDateEditor(createDateFormat(), !required);
    }

    // TODO: make date format externally configurable
    public DateFormat createDateFormat() {
        return new SimpleDateFormat("MM/dd/yyyy");
    }

    public String formatDate(Date date) {
        if (dateFormat.get() == null) {
            dateFormat.set(createDateFormat());
        }
        return dateFormat.get().format(date);
    }

    public void registerDomainObjectEditor(ServletRequestDataBinder binder, String field, StudyCalendarDao<?> dao) {
        binder.registerCustomEditor(dao.domainClass(), field, new DaoBasedEditor(dao));
    }

    public void addHierarchyToModel(ScheduledEvent event, Map<String, Object> model) {
        model.put("scheduledEvent", event);
        if (event != null) {
            addHierarchyToModel(event.getPlannedEvent(), model);
            addHierarchyToModel(event.getScheduledArm(), model);
        }
    }

    public void addHierarchyToModel(ScheduledArm arm, Map<String, Object> model) {
        model.put("scheduledArm", arm);
        if (arm != null) {
            addHierarchyToModel(arm.getArm(), model);
            addHierarchyToModel(arm.getScheduledCalendar(), model);
        }
    }

    public void addHierarchyToModel(ScheduledCalendar calendar, Map<String, Object> model) {
        model.put("scheduledCalendar", calendar);
        if (calendar != null) {
            addHierarchyToModel(calendar.getAssignment().getStudySite().getStudy().getPlannedCalendar(), model);
            addHierarchyToModel(calendar.getAssignment(), model);
        }
    }

    public void addHierarchyToModel(StudyParticipantAssignment assignment, Map<String, Object> model) {
        model.put("assignment", assignment);
        if (assignment != null) {
            model.put("participant", assignment.getParticipant());
            model.put("scheduledCalendar", assignment.getScheduledCalendar());
        }
    }

    public void addHierarchyToModel(PlannedEvent event, Map<String, Object> model) {
        model.put("plannedEvent", event);
        if (event != null) addHierarchyToModel(event.getPeriod(), model);
    }

    public void addHierarchyToModel(Period period, Map<String, Object> model) {
        model.put("period", period);
        if (period != null) addHierarchyToModel(period.getArm(), model);
    }

    public void addHierarchyToModel(Arm arm, Map<String, Object> model) {
        model.put("arm", arm);
        if (arm != null) addHierarchyToModel(arm.getEpoch(), model);
    }

    public void addHierarchyToModel(Epoch epoch, Map<String, Object> model) {
        model.put("epoch", epoch);
        if (epoch != null) addHierarchyToModel(epoch.getPlannedCalendar(), model);
    }

    public void addHierarchyToModel(PlannedCalendar plannedCalendar, Map<String, Object> model) {
        model.put("plannedCalendar", plannedCalendar);
        if (plannedCalendar != null) model.put("study", plannedCalendar.getStudy());
    }

    public boolean isAjaxRequest(HttpServletRequest request) {
        String header = request.getHeader("X-Requested-With");
        return header != null && "XMLHttpRequest".equals(header);
    }

    public ModelAndView redirectToCalendarTemplate(int studyId) {
        return redirectToCalendarTemplate(studyId, null);
    }

    @SuppressWarnings({ "unchecked" })
    public ModelAndView redirectToCalendarTemplate(int studyId, Integer selectedArmId) {
        ModelMap model = new ModelMap("study", studyId);
        if (selectedArmId != null) model.put("arm", selectedArmId);
        return new ModelAndView("redirectToCalendarTemplate", model);
    }

    // note that if you change the error message here, you need to change it in error-console.js, too
    public void sendPostOnlyError(HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "POST is the only valid method for this URL");
    }

    public ControllerTools() { }
}
