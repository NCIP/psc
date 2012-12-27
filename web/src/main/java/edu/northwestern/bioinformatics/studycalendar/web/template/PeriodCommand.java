/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedCommand;

/**
 * @author Rhett Sutphin
 */
public interface PeriodCommand extends PscAuthorizedCommand {
    Period getPeriod();
    StudySegment getStudySegment();

    /**
     * @return true to redirect back to the edit period page on completion; false to go to the template.
     */
    boolean apply();
}
