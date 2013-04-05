/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import org.apache.commons.lang.StringUtils;
import org.restlet.data.Form;

/**
 * @author Rhett Sutphin
 */
public enum FormParameters {
    DAY,
    ACTIVITY_CODE,
    ACTIVITY_SOURCE,
    DETAILS,
    CONDITION,
    LABEL,
    POPULATION,
    WEIGHT;

    public String attributeName() {
        return name().replaceAll("_", "-").toLowerCase();
    }

    public String extractFirstFrom(Form form) {
        String value = form.getFirstValue(attributeName());
        return StringUtils.isBlank(value) ? null : value;
    }

    public Integer extractFirstAsIntegerFrom(Form form) {
        String val = extractFirstFrom(form);
        if (val == null) return null;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException nfe) {
            throw new StudyCalendarValidationException(
                "Expected %s parameter value to be an integer, not '%s'", nfe, attributeName(), val);
        }
    }

    public void setParameter(Form parameter, String paramName, Object value) {
        parameter.set(paramName, String.valueOf(value), false);
    }
    
}
