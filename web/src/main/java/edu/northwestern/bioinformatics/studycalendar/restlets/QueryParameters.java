package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.restlet.data.Form;
import org.restlet.data.Request;

/**
 * @author Rhett Sutphin
 */
public enum QueryParameters {
    Q,
    TYPE_ID, /* deprecated */
    TYPE
    ;

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
