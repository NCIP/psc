package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;

import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import org.springframework.beans.factory.annotation.Required;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ParticipantCoordinatorDashboardService {

    private ScheduledActivityDao scheduledActivityDao;

    final String dayNames[] =
    {"Sunday",
    "Monday",
    "Tuesday",
    "Wednesday",
    "Thursday",
    "Friday",
    "Saturday"
    };

    private static final Logger log = LoggerFactory.getLogger(ParticipantCoordinatorDashboardService.class.getName());

    public Map<String, Object> getMapOfCurrentEvents(List<StudyParticipantAssignment> studyParticipantAssignments, int initialShiftDate) {
        Date startDate = new Date();
        Collection<ScheduledActivity> collectionOfEvents;
        SortedMap<String, Object> mapOfUserAndCalendar = new TreeMap<String, Object>();

        Map <String, Object> participantAndEvents;

        for (int i =0; i< initialShiftDate; i++) {
            Date tempStartDate = shiftStartDayByNumberOfDays(startDate, i);
            participantAndEvents = new HashMap<String, Object>();
            for (StudyParticipantAssignment studyParticipantAssignment : studyParticipantAssignments) {

                List<ScheduledActivity> events = new ArrayList<ScheduledActivity>();
                ScheduledCalendar calendar = studyParticipantAssignment.getScheduledCalendar();
                collectionOfEvents = getScheduledActivityDao().getEventsByDate(calendar, tempStartDate, tempStartDate);

                Participant participant = studyParticipantAssignment.getParticipant();
                String participantName = participant.getFullName();
                if (collectionOfEvents.size()>0) {
                    for (ScheduledActivity event : collectionOfEvents) {
                        events.add(event);
                    }
                }
                if (events != null && events.size() > 0) {
                    participantAndEvents.put(participantName, events);
                }
            }
            String keyDate = formatDateToString(tempStartDate);
            keyDate = keyDate + " - " + convertDateKeyToString(tempStartDate);
            if (participantAndEvents!= null && participantAndEvents.size()>0) {
                mapOfUserAndCalendar.put(keyDate, participantAndEvents);
            }
        }
       return mapOfUserAndCalendar;
    }

    public Map<String, Object> getMapOfCurrentEventsForSpecificActivity(
            List<StudyParticipantAssignment> studyParticipantAssignments, int initialShiftDate, Map<ActivityType, Boolean> activities) {
        Date startDate = new Date();
        Collection<ScheduledActivity> collectionOfEvents;
        SortedMap<String, Object> mapOfUserAndCalendar = new TreeMap<String, Object>();

        Map <String, Object> participantAndEvents;

        for (int i =0; i< initialShiftDate; i++) {
            Date tempStartDate = shiftStartDayByNumberOfDays(startDate, i);
            participantAndEvents = new HashMap<String, Object>();
            for (StudyParticipantAssignment studyParticipantAssignment : studyParticipantAssignments) {
                List<ScheduledActivity> events = new ArrayList<ScheduledActivity>();
                ScheduledCalendar calendar = studyParticipantAssignment.getScheduledCalendar();
                collectionOfEvents = getScheduledActivityDao().getEventsByDate(calendar, tempStartDate, tempStartDate);
                Participant participant = studyParticipantAssignment.getParticipant();
                String participantName = participant.getFullName();

                if (collectionOfEvents.size()>0) {
                    for (ScheduledActivity event : collectionOfEvents) {
                        ActivityType eventActivityType = event.getActivity().getType();
                        Boolean value = activities.get(eventActivityType);
                        if (value) {
                            events.add(event);
                        }
                    }
                }
                if (events != null && events.size()> 0)  {
                    participantAndEvents.put(participantName, events);
                }
            }
            String keyDate = formatDateToString(tempStartDate);
            keyDate = keyDate + " - " + convertDateKeyToString(tempStartDate);
            if (participantAndEvents!= null && participantAndEvents.size()>0) {
                mapOfUserAndCalendar.put(keyDate, participantAndEvents);
            }
        }
        return mapOfUserAndCalendar;
    }

    public String convertDateKeyToString(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int dayOfTheWeek = c.get(Calendar.DAY_OF_WEEK);
        return dayNames[dayOfTheWeek-1];
    }


    public Map<Object, Object> getMapOfOverdueEvents(List<StudyParticipantAssignment> studyParticipantAssignments) {
        Date currentDate = new Date();
        Date endDate = shiftStartDayByNumberOfDays(currentDate, -1);

        //list of events overtue
        Map <Object, Object> participantAndOverDueEvents = new HashMap<Object, Object>();
        for (StudyParticipantAssignment studyParticipantAssignment : studyParticipantAssignments) {
            List<ScheduledActivity> events = new ArrayList<ScheduledActivity>();

            Map<Object, Integer> key = new HashMap<Object, Integer>();

            ScheduledCalendar calendar = studyParticipantAssignment.getScheduledCalendar();

            Date startDate = studyParticipantAssignment.getStartDateEpoch();
            Collection<ScheduledActivity> collectionOfEvents = getScheduledActivityDao().getEventsByDate(calendar, startDate, endDate);
            Participant participant = studyParticipantAssignment.getParticipant();

            for (ScheduledActivity event : collectionOfEvents) {
                if(event.getCurrentState().getMode().equals(ScheduledEventMode.SCHEDULED)) {
                    events.add(event);
                }
            }
            if (events.size()>0) {
                key.put(participant, events.size());
                participantAndOverDueEvents.put(key, studyParticipantAssignment);
            }
        }
        return participantAndOverDueEvents;
    }


    public Date shiftStartDayByNumberOfDays(Date startDate, Integer numberOfDays) {
        java.sql.Timestamp timestampTo = new java.sql.Timestamp(startDate.getTime());
        long numberOfDaysToShift = numberOfDays * 24 * 60 * 60 * 1000;
        timestampTo.setTime(timestampTo.getTime() + numberOfDaysToShift);
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

    public String formatDateToString(Date date) {
        DateFormat df = new SimpleDateFormat("MM/dd");
        return df.format(date);
    }


    @Required
    public void setScheduledActivityDao(ScheduledActivityDao scheduledActivityDao) {
        this.scheduledActivityDao = scheduledActivityDao;
    }
    public ScheduledActivityDao getScheduledActivityDao() {
        return scheduledActivityDao;
    }
}
