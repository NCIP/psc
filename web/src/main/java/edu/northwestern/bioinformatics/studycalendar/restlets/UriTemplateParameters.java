/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.restlet.data.Reference;
import org.restlet.Request;

/**
 * @author Rhett Sutphin
 */
public enum UriTemplateParameters {
    STUDY_IDENTIFIER,
    EPOCH_NAME,
    STUDY_SEGMENT_NAME,
    PERIOD_IDENTIFIER,
    PLANNED_ACTIVITY_IDENTIFIER,
    ACTIVITY_SOURCE_NAME,
    ACTIVITY_CODE,
    SITE_IDENTIFIER,
    ASSIGNMENT_IDENTIFIER,
    BLACKOUT_DATE_IDENTIFIER,
    AMENDMENT_IDENTIFIER,
    SUBJECT_IDENTIFIER,
    YEAR,
    MONTH,
    DAY,
    SCHEDULED_ACTIVITY_IDENTIFIER,
    USERNAME,
    ROLENAME,
    BUNDLE_ID,
    NOTIFICATION_IDENTIFIER,
    SOURCE_NAME,
    SERVICE_IDENTIFIER,
    USER_ACTION_IDENTIFIER
    ;

    public String attributeName() {
        return name().replaceAll("_", "-").toLowerCase();
    }

    public String extractFrom(Request request) {
        return hasParameter(request)
                ? Reference.decode((String) request.getAttributes().get(attributeName())).replaceAll("\04", "/")
                : null;
    }

    public boolean hasParameter(Request request) {
        return request.getAttributes().get(attributeName()) != null;
    }

    public void putIn(Request request, Object value) {
        request.getAttributes().put(attributeName(), value);
    }

    public void removeFrom(Request request) {
        request.getAttributes().remove(attributeName());
    }
}
