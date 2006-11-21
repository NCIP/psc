package edu.northwestern.bioinformatics.studycalendar.api;

import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Participant;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEvent;

import java.util.Collection;
import java.util.Date;

/**
 * @author Rhett Sutphin
 */
public interface PlannedCalendarService {
    /**
     * Notify the PSC about the given study.  The PSC will create a default template for it.
     * The study must include all site associations. 
     *
     * @param study
     * @return the newly created default template for the study
     * @see edu.northwestern.bioinformatics.studycalendar.domain.StudySite
     * @see Site
     */
    PlannedCalendar registerStudy(Study study);

    /**
     * Retrieve the calendar template for the given study.
     *
     * @param study The study for which to return the calendar.  For purposes of matching,
     * implementations are only required to consider the study's
     * {@link edu.northwestern.bioinformatics.studycalendar.domain.Study#getBigId() grid ID}.
     * @return The full calendar template as it currently exists in the system.  
     * @see PlannedCalendar
     * @see edu.northwestern.bioinformatics.studycalendar.domain.Epoch
     * @see edu.northwestern.bioinformatics.studycalendar.domain.Arm
     * @see edu.northwestern.bioinformatics.studycalendar.domain.Period
     * @see edu.northwestern.bioinformatics.studycalendar.domain.PlannedEvent
     */
    PlannedCalendar getPlannedCalendar(Study study);
}
