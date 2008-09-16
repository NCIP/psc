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
    LABELS,
    POPULATION;

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
}
