package edu.northwestern.bioinformatics.studycalendar.api;

import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.NextArmMode;
import edu.northwestern.bioinformatics.studycalendar.domain.Participant;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.ScheduledEventState;

import java.util.Collection;
import java.util.Date;

/**
 * @author Rhett Sutphin
 */
public interface ScheduledCalendarService {
    /**
     * Assign a participant to the given study from the given site.
     *
     * @param study The study to which the participant will be assigned.  PSC must already know about it.
     * @param participant  If the participant is unknown, it will be automatically registered.
     * @param site The site from which the participant is being assigned.  The PSC must already know about it and its association with the study.
     * @param firstArm The arm of the template to which the participant should be initially assigned
     * @return the newly created schedule
     *
     * @see PlannedCalendarService#registerStudy(Study)
     */
    ScheduledCalendar assignParticipant(Study study, Participant participant, Site site, Arm firstArm);


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
     * @see edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEvent
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
     * @return
     */
    Collection<ScheduledEvent> getScheduledEvents(
        Study study, Participant participant, Site site, Date startDate, Date endDate);

    /**
     * Change the state of the given event to the given new state.  For matching, implementations
     * may consider only the grid ID of the event.
     *
     * @param event
     * @param newState
     * @return The same event, updated into the new state
     * @see edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Scheduled
     * @see edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Occurred
     * @see edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Canceled
     */
    ScheduledEvent changeEventState(ScheduledEvent event, ScheduledEventState newState);

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
}
