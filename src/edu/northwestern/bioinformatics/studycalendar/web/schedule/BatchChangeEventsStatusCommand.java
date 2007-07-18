package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEventMode;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.ScheduledEventState;

import java.util.Set;

public class BatchChangeEventsStatusCommand {
    private Set<ScheduledEvent> events;
    private ScheduledEventMode newEventMode;
    private ScheduledCalendarDao scheduledCalendarDao;

    BatchChangeEventsStatusCommand(ScheduledCalendarDao scheduledCalendarDao){
        this.scheduledCalendarDao = scheduledCalendarDao;
    }
    
    public void apply() {
        ScheduledEventState state;
        if (hasStateChange()) {
            state = createState();

            for(ScheduledEvent event: events) {
                event.changeState(state);
                scheduledCalendarDao.save(event.getScheduledArm().getScheduledCalendar());
            }
        }
    }

    private boolean hasStateChange() {
        return newEventMode!= null;
    }

    public ScheduledEventState createState() {
        return newEventMode.createStateInstance();
    }

    public Set<ScheduledEvent> getEvents() {
        return events;
    }

    public ScheduledCalendar getScheduledCalendar(){
        return events.iterator().next().getScheduledArm().getScheduledCalendar();
    }

    public void setEvents(Set<ScheduledEvent> events) {
        this.events = events;
    }

    public ScheduledEventMode getNewEventMode() {
        return newEventMode;
    }

    public void setNewEventMode(ScheduledEventMode newEventMode) {
        this.newEventMode = newEventMode;
    }

    public void setScheduledCalendarDao(ScheduledCalendarDao scheduledCalendarDao) {
        this.scheduledCalendarDao = scheduledCalendarDao;
    }
}
