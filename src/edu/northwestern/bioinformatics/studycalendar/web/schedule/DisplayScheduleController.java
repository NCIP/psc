package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyParticipantAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledArmDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.BreadcrumbContext;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractCommandController;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTools;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Map;
import java.util.List;
import java.util.HashMap;

import gov.nih.nci.cabig.ctms.editors.GridIdentifiableDaoBasedEditor;

/**
 * @author Rhett Sutphin
 */
@AccessControl(protectionGroups = StudyCalendarProtectionGroup.PARTICIPANT_COORDINATOR)
public class DisplayScheduleController extends PscAbstractCommandController<DisplayScheduleCommand> {
//    private static final Log log = LogFactory.getLog(DisplayScheduleController.class);
    private static final Logger log = LoggerFactory.getLogger(DisplayScheduleController.class);
    private StudyParticipantAssignmentDao assignmentDao;
    private ScheduledCalendarDao scheduledCalendarDao;
    private ScheduledArmDao scheduledArmDao;
    private StudyDao studyDao;

    public DisplayScheduleController() {
        setCrumb(new Crumb());
        setCommandClass(DisplayScheduleCommand.class);
    }

    @Override
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        ControllerTools.registerDomainObjectEditor(binder, "arm", scheduledArmDao);
        ControllerTools.registerDomainObjectEditor(binder, "calendar", scheduledCalendarDao);
        binder.registerCustomEditor(StudyParticipantAssignment.class, "assignment",
            new GridIdentifiableDaoBasedEditor(assignmentDao));
    }

    @Override
    @SuppressWarnings("unchecked")
    protected ModelAndView handle(DisplayScheduleCommand command, BindException errors, HttpServletRequest request, HttpServletResponse response) throws Exception {
        StudyParticipantAssignment assignment = command.getAssignment();

        ModelMap model = new ModelMap();
        ControllerTools.addHierarchyToModel(assignment.getScheduledCalendar(), model);
        model.addObject("assignment", assignment);
        model.addObject("calendar", assignment.getScheduledCalendar());
        model.addObject("dates", createDates(assignment.getScheduledCalendar()));
        model.addObject("arm", command.getArm());

        Study study = assignment.getStudySite().getStudy();
        if (study != null) {
            List<StudyParticipantAssignment> assignments = studyDao.getAssignmentsForStudy(study.getId());
            model.addObject("assignments", assignments);
        }

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
        this.assignmentDao = studyParticipantAssignmentDao;
    }

    @Required
    public void setScheduledCalendarDao(ScheduledCalendarDao scheduledCalendarDao) {
        this.scheduledCalendarDao = scheduledCalendarDao;
    }

    @Required
    public void setScheduledArmDao(ScheduledArmDao scheduledArmDao) {
        this.scheduledArmDao = scheduledArmDao;
    }

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    private static class Crumb extends DefaultCrumb {
        @Override
        public String getName(BreadcrumbContext context) {
            return new StringBuilder()
                .append("Schedule for ").append(context.getParticipant().getFullName())
                .toString();
        }

        @Override
        public Map<String, String> getParameters(BreadcrumbContext context) {
            Map<String, String> params = createParameters(
                "calendar", context.getScheduledCalendar().getId().toString()
            );
            if (context.getScheduledArm() != null) {
                params.put("arm", context.getScheduledArm().getId().toString());
            }
            return params;
        }
    }
}
