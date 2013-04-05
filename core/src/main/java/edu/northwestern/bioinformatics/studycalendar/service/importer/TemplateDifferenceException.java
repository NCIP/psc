/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.importer;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.domain.tools.Differences;

/**
 * @author Rhett Sutphin
 */
public class TemplateDifferenceException extends StudyCalendarValidationException {
    private Differences differences;

    public TemplateDifferenceException(Differences differences) {
        super(differences.toTreeString());
        this.differences = differences;
    }

    public Differences getDifferences() {
        return differences;
    }
}
