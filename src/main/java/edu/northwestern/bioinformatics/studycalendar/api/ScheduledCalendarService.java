package edu.northwestern.bioinformatics.studycalendar.api;

import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.NextArmMode;
import edu.northwestern.bioinformatics.studycalendar.domain.Participant;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.AdverseEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.StudyParticipantAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.ScheduledEventState;

import java.util.Collection;
import java.util.Date;

/**
 * The public interface for accessing and manipulating the PSC on the per-patient "schedule" side.
 *
 * @author Rhett Sutphin
 */
public interface ScheduledCalendarService {
    /**
     * Assign a participant to the given study from the given site.
     *
     * @param study The study to which the participant will be assigned.  PSC must already know about it.
     * @param participant  If the participant is unknown, it will be automatically registered.
     * @param site The site from which the participant is being assigned.  The PSC must already know about it and its association with the study.
     * @param firstArm The arm of the template to which the participant should be initially assigned.  If null, the first arm of the first epoch will be used.
     * @param startDate
     * @return the newly created schedule
     *
     * @see PlannedCalendarService#registerStudy(Study)
     */
    ScheduledCalendar assignParticipant(Study study, Participant participant, Site site, Arm firstArm, Date startDate, String registrationGridId);

    /**
     * Retrieve the full schedule for a participant on a study at a site.  Implementations may
     * consider only the grid IDs for the parameters.
     *
     * @param study
     * @param participant
     * @param site
     * @return The full schedule, with all scheduled arms and scheduled events, as it currently exists.
     * @see edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar
     * @see edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm
     * @see edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity
     */
    ScheduledCalendar getScheduledCalendar(Study study, Participant participant, Site site);

    /**
     * Return the events that exist for a schedule in the given date range.  The date range is
     * matched by the actual date for events in the scheduled and occurred states, and the ideal
     * date for events in the canceled state.
     *
     * @param study
     * @param participant
     * @param site
     * @param startDate The beginning of the range of dates to include.  If null, there is no early limit.
     * @param endDate The end of the range of dates to include.  If null, there is no late limit.
     */
    Collection<ScheduledActivity> getScheduledActivities(
        Study study, Participant participant, Site site, Date startDate, Date endDate);

    /**
     * Change the state of the given event to the given new state.  For matching, implementations
     * may consider only the grid ID of the event.
     *
     * @param event
     * @param newState
     * @return The same event, updated into the new state
     * @see edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Scheduled
     * @see edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Occurred
     * @see edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Canceled
     */
    ScheduledActivity changeEventState(ScheduledActivity event, ScheduledEventState newState);

    /**
     * Indicate the next arm for the participant's schedule.
     *
     * @param study
     * @param participant
     * @param site
     * @param nextArm
     * @param mode
     * @param startDate
     * @see NextArmMode
     * 
     */
    void scheduleNextArm(
        Study study, Participant participant, Site site, Arm nextArm, NextArmMode mode, Date startDate);

    /**
     * Notify the PSC about an adverse event for a participant.
     *
     * @param study
     * @param participant
     * @param site
     * @param adverseEvent
     */
    void registerSevereAdverseEvent(Study study, Participant participant, Site site, AdverseEvent adverseEvent);
    
    /**
     * 
     * @param assignment
     * @param adverseEvent
     * @see #registerSevereAdverseEvent(Study, Participant, Site, AdverseEvent)
     */
    void registerSevereAdverseEvent(StudyParticipantAssignment assignment, AdverseEvent adverseEvent);
}
