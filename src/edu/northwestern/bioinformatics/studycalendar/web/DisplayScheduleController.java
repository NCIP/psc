package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyParticipantAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.StudyParticipantAssignment;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Rhett Sutphin
 */
public class DisplayScheduleController implements Controller {
    private StudyParticipantAssignmentDao studyParticipantAssignmentDao;

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int assignmentId = ServletRequestUtils.getRequiredIntParameter(request, "assignment");
        StudyParticipantAssignment assignment = studyParticipantAssignmentDao.getById(assignmentId);

        ModelMap model = new ModelMap();
        model.addObject("calendar", assignment.getScheduledCalendar());
        model.addObject("participant", assignment.getScheduledCalendar().getAssignment().getParticipant());
        model.addObject("plannedCalendar", assignment.getScheduledCalendar().getAssignment().getStudySite().getStudy().getPlannedCalendar());
        model.addObject("arm", assignment.getScheduledCalendar().getScheduledArms().get(0));

        return new ModelAndView("displaySchedule", model);
    }

    ////// CONFIGURATION

    @Required
    public void setStudyParticipantAssignmentDao(StudyParticipantAssignmentDao studyParticipantAssignmentDao) {
        this.studyParticipantAssignmentDao = studyParticipantAssignmentDao;
    }
}
