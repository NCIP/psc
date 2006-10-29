package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEventMode;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.DatedScheduledEventState;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.ScheduledEventState;

import java.util.Date;

/**
 * @author Rhett Sutphin
 */
public class ScheduleEventCommand {
    private ScheduledEvent event;
    private ScheduledEventMode newMode;
    private String newReason;
    private Date newDate;

    private ScheduledCalendarDao scheduledCalendarDao;

    public ScheduleEventCommand(ScheduledCalendarDao scheduledCalendarDao) {
        this.scheduledCalendarDao = scheduledCalendarDao;
    }

    ////// LOGIC

    public void changeState() {
        event.changeState(createState());
        scheduledCalendarDao.save(event.getScheduledArm().getScheduledCalendar());
    }

    public ScheduledEventState createState() {
        ScheduledEventState instance = getNewMode().createStateInstance();
        instance.setReason(getNewReason());
        if (instance instanceof DatedScheduledEventState) {
            ((DatedScheduledEventState) instance).setDate(getNewDate());
        }
        return instance;
    }

    ////// BOUND PROPERTIES

    public ScheduledEvent getEvent() {
        return event;
    }

    public void setEvent(ScheduledEvent event) {
        this.event = event;
    }

    public ScheduledEventMode getNewMode() {
        return newMode;
    }

    public void setNewMode(ScheduledEventMode newMode) {
        this.newMode = newMode;
    }

    public String getNewReason() {
        return newReason;
    }

    public void setNewReason(String newReason) {
        this.newReason = newReason;
    }

    public Date getNewDate() {
        return newDate;
    }

    public void setNewDate(Date newDate) {
        this.newDate = newDate;
    }
}
