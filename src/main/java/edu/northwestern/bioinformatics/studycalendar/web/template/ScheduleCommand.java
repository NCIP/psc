package edu.northwestern.bioinformatics.studycalendar.web.template;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledEventDao;

import java.util.*;
import java.text.DateFormat;                                                                                                                       
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class ScheduleCommand {
    private Integer toDate;
    private User user;
    private UserDao userDao;
    private ScheduledEventDao scheduledEventDao;

    private static final Logger log = LoggerFactory.getLogger(ScheduleCommand.class.getName());

    public Map<String, Object> execute() {
        List<StudyParticipantAssignment> studyParticipantAssignments = getUserDao().getAssignments(getUser());
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("mapOfUserAndCalendar", getMapOfCurrentEvents(studyParticipantAssignments));
        return model;
    }

    public Map<String, Object> getMapOfCurrentEvents(List<StudyParticipantAssignment> studyParticipantAssignments) {
        Date startDate = new Date();
        int initialShiftDate = getToDate();
        Collection<ScheduledEvent> collectionOfEvents = null;
        SortedMap<String, Object> mapOfUserAndCalendar = new TreeMap<String, Object>();
        Map <String, Object> participantAndEvents;
        for (int i =0; i< initialShiftDate; i++) {
            participantAndEvents = new HashMap<String, Object>();
            Date tempStartDate = shiftStartDayByNumberOfDays(startDate, i);
            List<ScheduledEvent> events = new ArrayList<ScheduledEvent>();
            for (StudyParticipantAssignment studyParticipantAssignment : studyParticipantAssignments) {
                ScheduledCalendar calendar = studyParticipantAssignment.getScheduledCalendar();
                collectionOfEvents = getScheduledEventDao().getEventsByDate(calendar, tempStartDate, tempStartDate);

                Participant participant = studyParticipantAssignment.getParticipant();
                String participantName = participant.getFullName();

                for (ScheduledEvent event : collectionOfEvents) {
                    String participantAndEventsKey = participantName + " - " + event.getActivity().getName();
                    participantAndEvents.put(participantAndEventsKey, event);
                    events.add(event);
                }
            }
            String keyDate = formatDateToString(tempStartDate);
            mapOfUserAndCalendar.put(keyDate, participantAndEvents);
        }
        return mapOfUserAndCalendar;
    }


    public String formatDateToString(Date date) {
        DateFormat df = new SimpleDateFormat("MM/dd");
        return df.format(date);
    }


    public Date shiftStartDayByNumberOfDays(Date startDate, Integer numberOfDays) {
        java.sql.Timestamp timestampTo = new java.sql.Timestamp(startDate.getTime());
        long oneDay = numberOfDays * 24 * 60 * 60 * 1000;
        timestampTo.setTime(timestampTo.getTime() + oneDay);
        Date d = timestampTo;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = df.format(d);
        Date d1;
        try {
            d1 = df.parse(dateString);
        } catch (ParseException e) {
            log.debug("Exception " + e);
            d1 = startDate;
        }
        return d1;
    }

    ////// BOUND PROPERTIES


    public Integer getToDate() {
        return toDate;
    }

    public void setToDate(Integer toDate) {
        this.toDate = toDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }


    public UserDao getUserDao() {
        return userDao;
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public ScheduledEventDao getScheduledEventDao() {
        return scheduledEventDao;
    }

    public void setScheduledEventDao(ScheduledEventDao scheduledEventDao) {
        this.scheduledEventDao = scheduledEventDao;
    }
}
