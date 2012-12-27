/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.api;

import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityState;

import java.util.Collection;
import java.util.Date;

/**
 * The public interface for accessing and manipulating the PSC on the per-patient "schedule" side.
 *
 * @author Rhett Sutphin
 */
public interface ScheduledCalendarService {
    /**
     * Assign a subject to the given study from the given site.
     *
     * @param study The study to which the subject will be assigned.  PSC must already know about it.
     * @param subject  If the subject is unknown, it will be automatically registered.
     * @param site The site from which the subject is being assigned.  The PSC must already know about it and its association with the study.
     * @param firstStudySegment The studySegment of the template to which the subject should be initially assigned.  If null, the first studySegment of the first epoch will be used.
     * @param startDate
     * @return the newly created schedule
     *
     * @see PlannedCalendarService#registerStudy(Study)
     */
    ScheduledCalendar assignSubject(Study study, Subject subject, Site site, StudySegment firstStudySegment, Date startDate, String registrationGridId);

    /**
     * Retrieve the full schedule for a subject on a study at a site.  Implementations may
     * consider only the grid IDs for the parameters.
     *
     * @param study
     * @param subject
     * @param site
     * @return The full schedule, with all scheduled studySegments and scheduled events, as it currently exists.
     * @see edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar
     * @see edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment
     * @see edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity
     */
    ScheduledCalendar getScheduledCalendar(Study study, Subject subject, Site site);

    /**
     * Return the events that exist for a schedule in the given date range.  The date range is
     * matched by the actual date for events in the scheduled and occurred states, and the ideal
     * date for events in the canceled state.
     *
     * @param study
     * @param subject
     * @param site
     * @param startDate The beginning of the range of dates to include.  If null, there is no early limit.
     * @param endDate The end of the range of dates to include.  If null, there is no late limit.
     */
    Collection<ScheduledActivity> getScheduledActivities(
        Study study, Subject subject, Site site, Date startDate, Date endDate);

    /**
     * Change the state of the given event to the given new state.  For matching, implementations
     * may consider only the grid ID of the event.
     *
     * @param event
     * @param newState
     * @return The same event, updated into the new state
     */
    ScheduledActivity changeEventState(ScheduledActivity event, ScheduledActivityState newState);

    /**
     * Indicate the next studySegment for the subject's schedule.
     *
     * @param study
     * @param subject
     * @param site
     * @param nextStudySegment
     * @param mode
     * @param startDate
     * @see NextStudySegmentMode
     * 
     */
    void scheduleNextStudySegment(
        Study study, Subject subject, Site site, StudySegment nextStudySegment, NextStudySegmentMode mode, Date startDate);

    /**
     * Notify the PSC about an adverse event for a subject.
     *
     * @param study
     * @param subject
     * @param site
     * @param adverseEvent
     */
    void registerSevereAdverseEvent(Study study, Subject subject, Site site, AdverseEvent adverseEvent);
    
    /**
     * 
     * @param assignment
     * @param adverseEvent
     * @see #registerSevereAdverseEvent(Study, Subject, Site, AdverseEvent)
     */
    void registerSevereAdverseEvent(StudySubjectAssignment assignment, AdverseEvent adverseEvent);
}
