package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyParticipantAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledArmDao;
import edu.northwestern.bioinformatics.studycalendar.domain.StudyParticipantAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEventMode;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.BreadcrumbContext;
import edu.northwestern.bioinformatics.studycalendar.service.NextArmMode;
import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractController;
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
import java.util.HashMap;

/**
 * @author Rhett Sutphin
 */
@AccessControl(protectionGroups = StudyCalendarProtectionGroup.PARTICIPANT_COORDINATOR)
public class DisplayScheduleController extends PscAbstractController {
    private static final Log log = LogFactory.getLog(DisplayScheduleController.class);
    private StudyParticipantAssignmentDao studyParticipantAssignmentDao;
    private ScheduledCalendarDao scheduledCalendarDao;
    private ScheduledArmDao scheduledArmDao;


    public DisplayScheduleController() {
        setCrumb(new Crumb());
    }

    public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Integer assignmentId = ServletRequestUtils.getIntParameter(request, "assignment");
        StudyParticipantAssignment assignment;
        if (assignmentId != null) {
            assignment = studyParticipantAssignmentDao.getById(assignmentId);
        } else {
            int calendarId = ServletRequestUtils.getRequiredIntParameter(request, "calendar");
            assignment = scheduledCalendarDao.getById(calendarId).getAssignment();
        }

        Integer armId = ServletRequestUtils.getIntParameter(request, "arm");
        ScheduledArm selectedArm;
        if (armId == null) {
            selectedArm = assignment.getScheduledCalendar().getCurrentArm();
        } else {
            selectedArm = scheduledArmDao.getById(armId);
        }

        ModelMap model = new ModelMap();
        model.addObject("assignment", assignment);
        model.addObject("calendar", assignment.getScheduledCalendar());
        model.addObject(assignment.getParticipant());
        model.addObject(assignment.getStudySite().getStudy().getPlannedCalendar());
        model.addObject("dates", createDates(assignment.getScheduledCalendar()));
        model.addObject("arm", selectedArm);

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

    @Required
    public void setScheduledCalendarDao(ScheduledCalendarDao scheduledCalendarDao) {
        this.scheduledCalendarDao = scheduledCalendarDao;
    }

    @Required
    public void setScheduledArmDao(ScheduledArmDao scheduledArmDao) {
        this.scheduledArmDao = scheduledArmDao;
    }

    private static class Crumb extends DefaultCrumb {
        public String getName(BreadcrumbContext context) {
            return new StringBuilder()
                .append("Schedule for ").append(context.getParticipant().getFullName())
                .toString();
        }

        public Map<String, String> getParameters(BreadcrumbContext context) {
            Map<String, String> params = createParameters(
                "calendar", context.getScheduledCalendar().toString()
            );
            if (context.getScheduledArm() != null) {
                params.put("arm", context.getScheduledArm().getId().toString());
            }
            return params;
        }
    }
}
