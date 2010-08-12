package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.restlet.data.Form;
import org.restlet.data.Request;

/**
 * An enum of all the query parameters used by any resource in the system.
 * The semantics of the parameters are not defined here.
 * <p>
 * Among other things, this enum is intended to promote the use of similarly named
 * parameters for similar uses in different resources.
 *
 * @author Rhett Sutphin
 */
public enum QueryParameters {
    Q,
    TYPE_ID, /* deprecated */
    TYPE,  /* TODO: deprecate TYPE and replace with ACTIVITY_TYPE */
    ACTIVITY_TYPE,
    STUDY,
    SITE,
    STATE,
    LABEL,
    START_DATE,
    END_DATE,
    RESPONSIBLE_USER,
    PERSON_ID
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

    public void putIn(Request request, String value) {
        request.getResourceRef().addQueryParameter(attributeName(), value);
    }
}
