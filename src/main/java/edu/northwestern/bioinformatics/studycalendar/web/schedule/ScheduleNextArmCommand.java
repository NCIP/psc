package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.NextArmMode;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;

import java.util.Date;

/**
 * @author Rhett Sutphin
 */
public class ScheduleNextArmCommand {
    private ScheduledCalendar calendar;
    private Arm arm;
    private Date startDate;
    private NextArmMode mode;

    private SubjectService subjectService;

    public ScheduleNextArmCommand(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    ////// LOGIC

    public ScheduledArm schedule() {
        return subjectService.scheduleArm(
            getCalendar().getAssignment(), getArm(), getStartDate(), getMode());
    }

    ////// BOUND PROPERTIES

    public ScheduledCalendar getCalendar() {
        return calendar;
    }

    public void setCalendar(ScheduledCalendar calendar) {
        this.calendar = calendar;
    }

    public Arm getArm() {
        return arm;
    }

    public void setArm(Arm arm) {
        this.arm = arm;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public NextArmMode getMode() {
        return mode;
    }

    public void setMode(NextArmMode mode) {
        this.mode = mode;
    }
}
