package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.ParticipantDao;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Scheduled;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Canceled;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.*;

import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import org.springframework.transaction.annotation.Transactional;
import org.apache.log4j.Logger;

import javax.persistence.Transient;

/**
 * @author Rhett Sutphin
 */
@Transactional
public class ParticipantService {
    private ParticipantDao participantDao;
    private SiteService siteService;

    private static final Logger log = Logger.getLogger(ParticipantService.class.getName());

    public StudyParticipantAssignment assignParticipant(Participant participant, StudySite study, Arm armOfFirstEpoch, Date startDate) {
        return this.assignParticipant(participant, study, armOfFirstEpoch, startDate, null);
    }

    public StudyParticipantAssignment assignParticipant(Participant participant, StudySite study, Arm armOfFirstEpoch, Date startDate, String assignmentGridIdentifier) {
        StudyParticipantAssignment spa = new StudyParticipantAssignment();
        spa.setParticipant(participant);
        spa.setStudySite(study);
        spa.setStartDateEpoch(startDate);
        spa.setBigId(assignmentGridIdentifier);
        participant.addAssignment(spa);
        scheduleArm(spa, armOfFirstEpoch, startDate, NextArmMode.PER_PROTOCOL);
        participantDao.save(participant);
        return spa;
    }
    
    public List<StudyParticipantAssignment> getAssignedStudyParticipant(String userName, List<StudyParticipantAssignment> assignments) {
    	List<StudyParticipantAssignment> actualAssignments = new ArrayList<StudyParticipantAssignment>();
    	List<Site> sites =  new ArrayList<Site>(siteService.getSitesForParticipantCoordinator(userName));
    	for (StudyParticipantAssignment assignment : assignments) {
    		for (Site site : sites) {
    			if (site.getId()== assignment.getStudySite().getSite().getId()) 
    				actualAssignments.add(assignment);
    		}
        }
    	return actualAssignments;
    }
    
    public ScheduledArm scheduleArm(
        StudyParticipantAssignment assignment, Arm arm, Date startDate, NextArmMode mode
    ) {
        ScheduledCalendar calendar = assignment.getScheduledCalendar();
        if (calendar == null) {
            calendar = new ScheduledCalendar();
            assignment.setScheduledCalendar(calendar);
        }

        if (mode == NextArmMode.IMMEDIATE) {
            String cancellationReason = "Immediate transition to " + arm.getQualifiedName();
            for (ScheduledArm existingArm : calendar.getScheduledArms()) {
                for (ScheduledEvent event : existingArm.getEvents()) {
                    if (event.getCurrentState().getMode() == ScheduledEventMode.SCHEDULED) {
                        event.changeState(new Canceled(cancellationReason));
                    }
                }
            }
        }

        ScheduledArm scheduledArm = new ScheduledArm();
        scheduledArm.setArm(arm);
        calendar.addArm(scheduledArm);
        int normalizationFactor = arm.getDayRange().getStartDay() * -1 + 1;

        for (Period period : arm.getPeriods()) {
            for (PlannedEvent plannedEvent : period.getPlannedEvents()) {
                for (Integer armDay : plannedEvent.getDaysInArm()) {
                    // TODO: I think we might need to track which repetition an event is from
                    ScheduledEvent event = new ScheduledEvent();
                    event.setIdealDate(idealDate(armDay + normalizationFactor, startDate));
                    event.setPlannedEvent(plannedEvent);
                    event.changeState(new Scheduled("Initialized from template", event.getIdealDate()));
                    scheduledArm.addEvent(event);
                }
            }
        }

        // Sort in the same order they'll be coming out of the database (for consistency)
        Collections.sort(scheduledArm.getEvents(), new Comparator<ScheduledEvent>() {
            public int compare(ScheduledEvent e1, ScheduledEvent e2) {
                int dateCompare = e1.getIdealDate().compareTo(e2.getIdealDate());
                if (dateCompare != 0) return dateCompare;

                return e1.getPlannedEvent().getId().compareTo(e2.getPlannedEvent().getId());
            }
        });

        Site site = calendar.getAssignment().getStudySite().getSite();
        avoidWeekendsAndHolidays(site, scheduledArm);
        participantDao.save(assignment.getParticipant());

        return scheduledArm;                                                                                      
    }

    private void avoidWeekendsAndHolidays(Site site, ScheduledArm arm) {
       List<ScheduledEvent> listOfEvents = arm.getEvents();
        for (ScheduledEvent event : listOfEvents) {
            resetEvent(event, site);
        }
    }


    public void resetEvent(ScheduledEvent event, Site site) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Calendar dateCalendar = Calendar.getInstance();
        Date date = event.getActualDate();
        dateCalendar.setTime(date);

        int year = dateCalendar.get(Calendar.YEAR);
        Calendar holidayCalendar = Calendar.getInstance();
        List<AbstractHolidayState> holidayList = site.getHolidaysAndWeekends();

        for(AbstractHolidayState holiday: holidayList) {
            if (holiday instanceof Holiday) {
                //month needs to be decremented, because we are using 00 for January in the Calendar
                Holiday h = (Holiday)holiday;
                if (h.getYear() == null) {
                    holidayCalendar.set(year, h.getMonth()-1, h.getDay());
                } else {
                    holidayCalendar.set(h.getYear(), h.getMonth()-1, h.getDay());
                }
                String originalDateFormatted = df.format(date.getTime());
                String holidayDateFormatted = df.format(holidayCalendar.getTime());
                if (originalDateFormatted.equals(holidayDateFormatted)) {
                    date = shiftDayByOne(date);
                    String reason = "Rescheduled. " + holiday.getStatus();
                    Scheduled s = new Scheduled(reason, date);
                    event.changeState(s);
                    resetEvent(event, site);
                }
            } else if(holiday instanceof DayOfTheWeek) {
                DayOfTheWeek dayOfTheWeek = (DayOfTheWeek) holiday;
                int intValueOfTheDay = dayOfTheWeek.mapDayStringToInt();
                if (dateCalendar.get(Calendar.DAY_OF_WEEK) == intValueOfTheDay) {
                    date = shiftDayByOne(date);
                    String reason = "Rescheduled. " + holiday.getStatus();
                    Scheduled s = new Scheduled(reason, date);
                    event.changeState(s);
                    resetEvent(event, site);
                }
            }
        }
    }



    public Date shiftDayByOne(Date date) {
        java.sql.Timestamp timestampTo = new java.sql.Timestamp(date.getTime());
        long oneDay = 1 * 24 * 60 * 60 * 1000;
        timestampTo.setTime(timestampTo.getTime() + oneDay);
        Date d = (Date)timestampTo;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = df.format(d);
        Date d1 = null;
        try {
            d1 = df.parse(dateString);
        } catch (ParseException e) {
            log.info("=======EXCEPTION==== " + e.getMessage());
            d1 = date;
        }
        return d1;
    }

    private Date idealDate(int armDay, Date startDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);

        cal.add(Calendar.DAY_OF_YEAR, armDay - 1);
        return cal.getTime();
    }

    ////// CONFIGURATION

    public void setParticipantDao(ParticipantDao participantDao) {
        this.participantDao = participantDao;
    }
    
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }
}
