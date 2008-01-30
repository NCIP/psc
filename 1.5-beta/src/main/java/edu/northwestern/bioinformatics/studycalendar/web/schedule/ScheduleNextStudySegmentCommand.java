package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.NextStudySegmentMode;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;

import java.util.Date;

/**
 * @author Rhett Sutphin
 */
public class ScheduleNextStudySegmentCommand {
    private ScheduledCalendar calendar;
    private StudySegment studySegment;
    private Date startDate;
    private NextStudySegmentMode mode;

    private SubjectService subjectService;

    public ScheduleNextStudySegmentCommand(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    ////// LOGIC

    public ScheduledStudySegment schedule() {
        return subjectService.scheduleStudySegment(
            getCalendar().getAssignment(), getStudySegment(), getStartDate(), getMode());
    }

    ////// BOUND PROPERTIES

    public ScheduledCalendar getCalendar() {
        return calendar;
    }

    public void setCalendar(ScheduledCalendar calendar) {
        this.calendar = calendar;
    }

    public StudySegment getStudySegment() {
        return studySegment;
    }

    public void setStudySegment(StudySegment studySegment) {
        this.studySegment = studySegment;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public NextStudySegmentMode getMode() {
        return mode;
    }

    public void setMode(NextStudySegmentMode mode) {
        this.mode = mode;
    }
}
