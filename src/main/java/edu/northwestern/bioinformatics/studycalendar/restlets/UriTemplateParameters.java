package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.restlet.data.Reference;
import org.restlet.data.Request;

/**
 * @author Rhett Sutphin
 */
public enum UriTemplateParameters {
    STUDY_IDENTIFIER,
    SOURCE_NAME;

    public String attributeName() {
        return name().toLowerCase();
    }

    public String extractFrom(Request request) {
        return Reference.decode((String) request.getAttributes().get(attributeName()));
    }
}
