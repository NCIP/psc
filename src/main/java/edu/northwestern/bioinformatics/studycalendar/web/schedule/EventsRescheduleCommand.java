package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.ScheduledActivityState;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.DatedScheduledActivityState;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledCalendarDao;

import java.util.*;
import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class EventsRescheduleCommand {
    private Integer toDate;
    private String currentDate;
    private String reason;
    private ScheduledCalendar scheduledCalendar;

    private ScheduledCalendarDao scheduledCalendarDao;

    private static final Logger log = LoggerFactory.getLogger(EventsRescheduleCommand.class.getName());

    public EventsRescheduleCommand(ScheduledCalendarDao scheduledCalendarDao) {
        this.scheduledCalendarDao = scheduledCalendarDao;
    }

    public void apply() {
        if (currentDate == null) return;
        if (toDate == null ) return;
        changeEvents();
        scheduledCalendarDao.save(getScheduledCalendar());
    }


    private void changeEvents() {
        List<ScheduledStudySegment> scheduledStudySegments = scheduledCalendar.getScheduledStudySegments();
        for (ScheduledStudySegment segment : scheduledStudySegments) {
            List<ScheduledActivity> events = segment.getEvents();
            List<ScheduledActivity> filteredEvents = filterEventsByDateAndState(events);
            for (ScheduledActivity event : filteredEvents) {
                changeState(event);
            }
        }
    }

    private List<ScheduledActivity> filterEventsByDateAndState (List<ScheduledActivity> events) {
        List<ScheduledActivity> filteredEvents = new ArrayList<ScheduledActivity>();

        Date startDate=null;
    	try {
    		SimpleDateFormat df=new SimpleDateFormat("MM/dd/yyyy");
    		startDate=df.parse(currentDate);
	    } catch (Exception err) {
	    }
        for (ScheduledActivity event : events) {
            if (event.getActualDate().getTime() >= startDate.getTime() && ((event.getCurrentState().getMode() == ScheduledActivityMode.SCHEDULED) ||
                        (event.getCurrentState().getMode() == ScheduledActivityMode.CONDITIONAL))) {
                filteredEvents.add(event);
            }
        }

        return filteredEvents;
    }

    private void changeState(ScheduledActivity event) {
        ScheduledActivityState newState = event.getCurrentState();
        newState.setReason(createReason());
        if (newState instanceof DatedScheduledActivityState) {
            ((DatedScheduledActivityState) newState).setDate(createDate(event.getActualDate()));
        }
        event.changeState(newState);
    }

    private Date createDate(Date baseDate) {
        Calendar c = Calendar.getInstance();
        c.setTime(baseDate);
        c.add(Calendar.DATE, getToDate().intValue());
        return c.getTime();
    }

    private String createReason() {
        StringBuilder reason = new StringBuilder("Full schedule change");
        String message = getReason();
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Integer getToDate() {
        return toDate;
    }

    public void setToDate(Integer toDate) {
        this.toDate = toDate;
    }

    public String getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(String currentDate) {
        this.currentDate = currentDate;
    }
}