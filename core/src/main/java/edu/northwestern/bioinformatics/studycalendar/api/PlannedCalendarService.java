/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.api;

import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;

/**
 * The public interface for accessing and manipulating the PSC on the study-wide "plan" side.
 *
 * @author Rhett Sutphin
 */
public interface PlannedCalendarService {
    /**
     * Notify the PSC about the given study.  The PSC will create a default template for it.
     * The study must include all site associations.
     * <p>
     * If the study has previously been registered, the existing template will be returned.
     * If a study is re-registered with different site associations, any new site associations will
     * be added.  Attempting to re-register with missing site associations is an error.
     *
     * @param study
     * @return the newly created default template for the study.
     * @see edu.northwestern.bioinformatics.studycalendar.domain.StudySite
     * @see edu.northwestern.bioinformatics.studycalendar.domain.Site
     */
    PlannedCalendar registerStudy(Study study);

    /**
     * Retrieve the calendar template for the given study.
     *
     * @param study The study for which to return the calendar.  For purposes of matching,
     * implementations are only required to consider the study's
     * {@link edu.northwestern.bioinformatics.studycalendar.domain.Study#getGridId() grid ID}.
     * @return The full calendar template as it currently exists in the system.  
     * @see PlannedCalendar
     * @see edu.northwestern.bioinformatics.studycalendar.domain.Epoch
     * @see edu.northwestern.bioinformatics.studycalendar.domain.StudySegment
     * @see edu.northwestern.bioinformatics.studycalendar.domain.Period
     * @see edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity
     */
    PlannedCalendar getPlannedCalendar(Study study);
}
