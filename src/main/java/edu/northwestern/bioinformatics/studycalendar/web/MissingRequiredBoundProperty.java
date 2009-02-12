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
