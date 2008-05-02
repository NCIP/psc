package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.restlet.data.Reference;
import org.restlet.data.Request;

/**
 * @author Rhett Sutphin
 */
public enum UriTemplateParameters {
    STUDY_IDENTIFIER,
    ACTIVITY_SOURCE_NAME,
    ACTIVITY_CODE,
    SITE_IDENTIFIER,
    ASSIGNMENT_IDENTIFIER,
    BLACKOUT_DATE_IDENTIFIER,
    AMENDMENT_IDENTIFIER;

    public String attributeName() {
        return name().replaceAll("_", "-").toLowerCase();
    }

    public String extractFrom(Request request) {
        return hasParameter(request)
            ? Reference.decode((String) request.getAttributes().get(attributeName()))
            : null;
    }

    public boolean hasParameter(Request request) {
        return request.getAttributes().get(attributeName()) != null;
    }
}
