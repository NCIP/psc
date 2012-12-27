/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.nwu.bioinformatics.commons.CollectionUtils;
import org.restlet.data.Form;
import org.restlet.Request;

import java.util.Arrays;
import java.util.Collection;

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
    START_IDEAL_DATE,
    END_IDEAL_DATE,
    RESPONSIBLE_USER,
    PERSON_ID,
    PRIVILEGE,
    BRIEF,
    LIMIT,
    OFFSET,
    SOURCE,
    ACTION,
    ACTIVITY_NAME,
    ACTIVITY_DESCRIPTION,
    ACTIVITY_CODE,
    ACTIVITY_ID,
    SORT,
    ORDER;

    public String attributeName() {
        return name().replaceAll("_", "-").toLowerCase();
    }

    public String extractFrom(Request request) {
        return CollectionUtils.firstElement(extractAllFrom(request));
    }

    public Collection<String> extractAllFrom(Request request) {
        Form query = request.getResourceRef().getQueryAsForm();
        return Arrays.asList(query.getValuesArray(attributeName()));
    }

    public void putIn(Request request, String value) {
        request.getResourceRef().addQueryParameter(attributeName(), value);
    }

    public void replaceIn(Request request, String value) {
        Form query = request.getResourceRef().getQueryAsForm();
        query.removeAll(attributeName());
        query.add(attributeName(), value);
        request.getResourceRef().setQuery(query.getQueryString());
    }

    public void removeFrom(Request request) {
        Form query = request.getResourceRef().getQueryAsForm();
        query.removeAll(attributeName());
        request.getResourceRef().setQuery(query.getQueryString());
    }
}
