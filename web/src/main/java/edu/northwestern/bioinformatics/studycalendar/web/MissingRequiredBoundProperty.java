/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarUserException;

/**
 * @author Rhett Sutphin
 */
public class MissingRequiredBoundProperty extends StudyCalendarUserException {
    public MissingRequiredBoundProperty(String propertyName) {
        super("%s must be provided", propertyName);
    }
}
