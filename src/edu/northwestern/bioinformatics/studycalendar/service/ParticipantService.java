package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.ParticipantDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Participant;
import edu.northwestern.bioinformatics.studycalendar.domain.StudyParticipantAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEventMode;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Scheduled;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Canceled;

import java.util.Date;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import org.springframework.transaction.annotation.Transactional;

/**
 * @author Rhett Sutphin
 */
@Transactional
public class ParticipantService {
    private ParticipantDao participantDao;

    public StudyParticipantAssignment assignParticipant(Participant participant, StudySite study, Arm armOfFirstEpoch, Date startDate) {
        StudyParticipantAssignment spa = new StudyParticipantAssignment();
        spa.setParticipant(participant);
        spa.setStudySite(study);
        spa.setStartDateEpoch(startDate);
        participant.addAssignment(spa);
        scheduleArm(spa, armOfFirstEpoch, startDate, NextArmMode.PER_PROTOCOL);
        participantDao.save(participant);
        return spa;
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

        participantDao.save(assignment.getParticipant());

        return scheduledArm;
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
}
