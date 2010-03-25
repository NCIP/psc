package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.restlet.data.Request;
import org.restlet.data.Form;

/**
 * @author Nataliya Shurupova
 */
public enum FilterParameters {
    STUDY,
    SITE,
    STATE,
    ACTIVITY_TYPE,
    LABEL,
    START_DATE,
    END_DATE,
    RESPONSIBLE_USER,
    PERSON_ID;

    public String attributeName() {
        return name().replaceAll("_", "-").toLowerCase();
    }    

    public String extractFrom(Request request) {
        Form query = request.getResourceRef().getQueryAsForm();
        return query != null
                ? query.getFirstValue(attributeName())
                : null;
    }

    public boolean hasParameter(Request request) {
        return request.getResourceRef().hasQuery()
            && request.getResourceRef().getQueryAsForm().getFirst(attributeName()) != null;
    }

    public void putIn(Request request, String value) {
        request.getResourceRef().addQueryParameter(attributeName(), value);
    }
}
