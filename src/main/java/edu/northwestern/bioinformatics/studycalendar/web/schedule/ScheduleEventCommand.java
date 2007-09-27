package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEventMode;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.DatedScheduledEventState;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.ScheduledEventState;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rhett Sutphin
 */
public class ScheduleEventCommand {
    private static final Logger log = LoggerFactory.getLogger(ScheduleEventCommand.class.getName());

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

    public Collection<ScheduledEventMode> getEventSpecificMode(){
        List<ScheduledEventMode> availableModes =  new ArrayList<ScheduledEventMode>();
        if (event != null) {
            availableModes = ScheduledEventMode.getAvailableModes(event.getCurrentState(), event.isConditionalEvent());
        }
        return availableModes;
    }

    public String getNewReason() {
        return newReason;
    }

    public void setNewReason(String newReason) {
        this.newReason = newReason;
    }

    public Date getNewDate() {
        if (newDate == null) {
            return getEvent() == null ? null : getEvent().getActualDate();
        } else {
            return newDate;
        }
    }

    public void setNewDate(Date newDate) {
        this.newDate = newDate;
    }

    public String getNewNotes() {
        if (newNotes  == null) {
            return getEvent() == null ? null : getEvent().getNotes();
        } else {
            return newNotes;
        }
    }

    public void setNewNotes(String newNotes) {
        this.newNotes = newNotes;
    }
}
