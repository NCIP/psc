package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.restlet.data.Reference;
import org.restlet.data.Request;

/**
 * @author Rhett Sutphin
 */
public enum UriTemplateParameters {
    STUDY_IDENTIFIER,
    ACTIVITY_SOURCE_NAME,
    SITE_NAME,
    ACTIVITY_CODE, SITE_IDENTIFIER;

    public String attributeName() {
        return name().replaceAll("_", "-").toLowerCase();
    }

    public String extractFrom(Request request) {
        return Reference.decode((String) request.getAttributes().get(attributeName()));
    }
}
