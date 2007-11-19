package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm;

/**
 * @author Rhett Sutphin
 */
public class DisplayScheduleCommand {
    private StudySubjectAssignment assignment;
    private ScheduledCalendar calendar;
    private ScheduledArm arm;

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

    public ScheduledArm getArm() {
        if (arm != null) {
            return arm;
        } else {
            return getAssignment().getScheduledCalendar().getCurrentArm();
        }
    }

    //////

    public void setAssignment(StudySubjectAssignment assignment) {
        this.assignment = assignment;
    }

    public void setCalendar(ScheduledCalendar calendar) {
        this.calendar = calendar;
    }

    public void setArm(ScheduledArm arm) {
        this.arm = arm;
    }
}
