package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyParticipantAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.StudyParticipantAssignment;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;
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
@AccessControl(protectionGroups = StudyCalendarProtectionGroup.PARTICIPANT_COORDINATOR)
public class DisplayScheduleController implements Controller {
    private StudyParticipantAssignmentDao studyParticipantAssignmentDao;

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int assignmentId = ServletRequestUtils.getRequiredIntParameter(request, "assignment");
        StudyParticipantAssignment assignment = studyParticipantAssignmentDao.getById(assignmentId);

        ModelMap model = new ModelMap();
        model.addObject("assignment", assignment);
        model.addObject("calendar", assignment.getScheduledCalendar());
        model.addObject("participant", assignment.getParticipant());
        model.addObject("plannedCalendar", assignment.getStudySite().getStudy().getPlannedCalendar());
        // TODO: this should default to the "current" arm
        model.addObject("arm", assignment.getScheduledCalendar().getScheduledArms().get(0));

        return new ModelAndView("schedule/display", model);
    }

    ////// CONFIGURATION

    @Required
    public void setStudyParticipantAssignmentDao(StudyParticipantAssignmentDao studyParticipantAssignmentDao) {
        this.studyParticipantAssignmentDao = studyParticipantAssignmentDao;
    }
}
