package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm;
import edu.northwestern.bioinformatics.studycalendar.service.ParticipantService;
import edu.northwestern.bioinformatics.studycalendar.service.NextArmMode;

import java.util.Date;

/**
 * @author Rhett Sutphin
 */
public class ScheduleNextArmCommand {
    private ScheduledCalendar calendar;
    private Arm arm;
    private Date startDate;

    private ParticipantService participantService;

    public ScheduleNextArmCommand(ParticipantService participantService) {
        this.participantService = participantService;
    }

    ////// LOGIC

    public ScheduledArm schedule() {
        return participantService.scheduleArm(getCalendar().getAssignment(), getArm(), getStartDate(), NextArmMode.PER_PROTOCOL);
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
}
