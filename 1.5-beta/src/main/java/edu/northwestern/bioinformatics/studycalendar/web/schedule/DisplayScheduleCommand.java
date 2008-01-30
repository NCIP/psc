package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;

/**
 * @author Rhett Sutphin
 */
public class DisplayScheduleCommand {
    private StudySubjectAssignment assignment;
    private ScheduledCalendar calendar;
    private ScheduledStudySegment studySegment;

    /*
       Different combinations of parameters are allowed.  Hence the separation between
       getters and setters.
     */

    public StudySubjectAssignment getAssignment() {
        if (assignment != null) {
            return assignment;
        } else if (calendar != null) {
            return calendar.getAssignment();
        } else {
            return null;
        }
    }

    public ScheduledStudySegment getStudySegment() {
        if (studySegment != null) {
            return studySegment;
        } else {
            return getAssignment().getScheduledCalendar().getCurrentStudySegment();
        }
    }

    //////

    public void setAssignment(StudySubjectAssignment assignment) {
        this.assignment = assignment;
    }

    public void setCalendar(ScheduledCalendar calendar) {
        this.calendar = calendar;
    }

    public void setStudySegment(ScheduledStudySegment studySegment) {
        this.studySegment = studySegment;
    }
}
