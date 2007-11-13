package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.ScheduledActivityState;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.DatedScheduledActivityState;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledCalendarDao;

import java.util.Date;
import java.util.Calendar;
import java.util.Set;

/**
 * @author Rhett Sutphin
 */
public class BatchRescheduleCommand {
    private ScheduledActivityMode<?> newMode;
    private Integer dateOffset;
    private String newReason;
    private Set<ScheduledActivity> events;
    private ScheduledCalendar scheduledCalendar;

    private ScheduledCalendarDao scheduledCalendarDao;

    public BatchRescheduleCommand(ScheduledCalendarDao scheduledCalendarDao) {
        this.scheduledCalendarDao = scheduledCalendarDao;
    }

    public void apply() {
        if (getNewMode() == null) return;

        for (ScheduledActivity event : events) {
            if (event.isValidNewState(newMode.getClazz())) {
                changeState(event);
            }
        }
        scheduledCalendarDao.save(getScheduledCalendar());
    }

    private void changeState(ScheduledActivity event) {
        ScheduledActivityState newState = getNewMode().createStateInstance();
        newState.setReason(createReason());
        if (newState instanceof DatedScheduledActivityState) {
            ((DatedScheduledActivityState) newState).setDate(createDate(event.getActualDate()));
        }
        event.changeState(newState);
    }

    private Date createDate(Date baseDate) {
        Calendar c = Calendar.getInstance();
        c.setTime(baseDate);
        if(ScheduledActivityMode.OCCURRED != getNewMode()) {
            c.add(Calendar.DATE, getDateOffset());
        }
        return c.getTime();
    }

    private String createReason() {
        StringBuilder reason = new StringBuilder("Batch change");
        String message = getNewReason();
        if (message != null) {
            reason.append(": ").append(message);
        }
        return reason.toString();
    }

    ////// BOUND PROPERTIES

    public ScheduledCalendar getScheduledCalendar(){
        return scheduledCalendar;
    }

    public void setScheduledCalendar(ScheduledCalendar scheduledCalendar) {
        this.scheduledCalendar = scheduledCalendar;
    }

    public String getNewReason() {
        return newReason;
    }

    public void setNewReason(String newReason) {
        this.newReason = newReason;
    }

    public ScheduledActivityMode<?> getNewMode() {
        return newMode;
    }

    public void setNewMode(ScheduledActivityMode<?> newMode) {
        this.newMode = newMode;
    }

    public Integer getDateOffset() {
        return dateOffset;
    }

    public void setDateOffset(Integer dateOffset) {
        this.dateOffset = dateOffset;
    }

    public Set<ScheduledActivity> getEvents() {
        return events;
    }

    public void setEvents(Set<ScheduledActivity> events) {
        this.events = events;
    }
}
