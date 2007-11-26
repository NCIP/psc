package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySegmentDao;
import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractCommandController;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;

import java.util.Date;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
@AccessControl(roles = Role.SUBJECT_COORDINATOR)
public class ScheduleNextStudySegmentController extends PscAbstractCommandController<ScheduleNextStudySegmentCommand> {
    private SubjectService subjectService;
    private ScheduledCalendarDao scheduledCalendarDao;
    private StudySegmentDao studySegmentDao;

    public ScheduleNextStudySegmentController() {
        setCommandClass(ScheduleNextStudySegmentCommand.class);
    }

    @Override
    protected Object getCommand(HttpServletRequest request) throws Exception {
        return new ScheduleNextStudySegmentCommand(subjectService);
    }

    @Override
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
        getControllerTools().registerDomainObjectEditor(binder, "studySegment", studySegmentDao);
        getControllerTools().registerDomainObjectEditor(binder, "calendar", scheduledCalendarDao);
        binder.registerCustomEditor(Date.class, getControllerTools().getDateEditor(true));
    }

    @Override
    protected ModelAndView handle(ScheduleNextStudySegmentCommand command, BindException errors, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> model = errors.getModel();
        ScheduledStudySegment newStudySegment = command.schedule();
        model.put("scheduledStudySegment", newStudySegment);
        model.put("nextPerProtocolDate", newStudySegment.getNextStudySegmentPerProtocolStartDate());
        return new ModelAndView("schedule/ajax/scheduleNextStudySegment", model);
    }

    /////// CONFIGURATION

    @Required
    public void setSubjectService(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @Required
    public void setScheduledCalendarDao(ScheduledCalendarDao scheduledCalendarDao) {
        this.scheduledCalendarDao = scheduledCalendarDao;
    }

    @Required
    public void setStudySegmentDao(StudySegmentDao studySegmentDao) {
        this.studySegmentDao = studySegmentDao;
    }
}
