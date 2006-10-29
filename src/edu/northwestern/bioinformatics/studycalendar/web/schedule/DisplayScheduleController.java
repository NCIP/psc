package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyParticipantAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.StudyParticipantAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;
import edu.northwestern.bioinformatics.studycalendar.service.NextArmMode;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.beans.factory.annotation.Required;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Map;
import java.util.List;
import java.util.Calendar;
import java.util.HashMap;

/**
 * @author Rhett Sutphin
 */
@AccessControl(protectionGroups = StudyCalendarProtectionGroup.PARTICIPANT_COORDINATOR)
public class DisplayScheduleController implements Controller {
    private static final Log log = LogFactory.getLog(DisplayScheduleController.class);
    private StudyParticipantAssignmentDao studyParticipantAssignmentDao;

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int assignmentId = ServletRequestUtils.getRequiredIntParameter(request, "assignment");
        StudyParticipantAssignment assignment = studyParticipantAssignmentDao.getById(assignmentId);

        ModelMap model = new ModelMap();
        model.addObject("assignment", assignment);
        model.addObject("calendar", assignment.getScheduledCalendar());
        model.addObject(assignment.getParticipant());
        model.addObject(assignment.getStudySite().getStudy().getPlannedCalendar());
        model.addObject("dates", createDates(assignment.getScheduledCalendar()));
        model.addObject("arm", assignment.getScheduledCalendar().getCurrentArm());

        return new ModelAndView("schedule/display", model);
    }

    private Map<String, Date> createDates(ScheduledCalendar scheduledCalendar) {
        Map<String, Date> dates = new HashMap<String, Date>();

        Date perProtocolDate = null;
        List<ScheduledArm> existingArms = scheduledCalendar.getScheduledArms();
        if (existingArms.size() > 0) {
            ScheduledArm lastArm = existingArms.get(existingArms.size() - 1);
            log.debug("Building PER_PROTOCOL start date from " + lastArm);
            perProtocolDate = lastArm.getNextArmPerProtocolStartDate();
        }
        dates.put(NextArmMode.PER_PROTOCOL.name(), perProtocolDate);
        dates.put(NextArmMode.IMMEDIATE.name(), new Date());

        return dates;
    }

    ////// CONFIGURATION

    @Required
    public void setStudyParticipantAssignmentDao(StudyParticipantAssignmentDao studyParticipantAssignmentDao) {
        this.studyParticipantAssignmentDao = studyParticipantAssignmentDao;
    }
}
