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
    private String newNotes;

    private ScheduledCalendarDao scheduledCalendarDao;

    public ScheduleEventCommand(ScheduledCalendarDao scheduledCalendarDao) {
        this.scheduledCalendarDao = scheduledCalendarDao;
    }

    ////// LOGIC

    public void apply() {
        if (hasStateChange()) {
            event.changeState(createState());
        }
        event.setNotes(getNewNotes());
        scheduledCalendarDao.save(event.getScheduledArm().getScheduledCalendar());
    }

    private boolean hasStateChange() {
        return getNewMode() != null;
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
        if (newDate == null) {
            return getEvent().getActualDate();
        } else {
            return newDate;
        }
    }

    public void setNewDate(Date newDate) {
        this.newDate = newDate;
    }

    public String getNewNotes() {
        if (newNotes  == null) {
            return getEvent().getNotes();
        } else {
            return newNotes;
        }
    }

    public void setNewNotes(String newNotes) {
        this.newNotes = newNotes;
    }
}
