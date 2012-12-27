/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets.representations;

import edu.northwestern.bioinformatics.studycalendar.restlets.RestletTestCase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.representation.Representation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;

/**
 * @author Rhett Sutphin
 */
public abstract class JsonRepresentationTestCase extends RestletTestCase {
    private final Logger log = LoggerFactory.getLogger(getClass());

    protected JSONObject writeAndParseObject(Representation representation) throws IOException {
        StringWriter w = new StringWriter();
        representation.write(w);
        try {
            return new JSONObject(w.toString());
        } catch (JSONException e) {
            log.info("Failed due to", e);
            fail("Representation wasn't valid JSON: " + e.getMessage());
            return null;
        }
    }

    protected JSONArray writeAndParseArray(Representation representation) throws IOException {
        StringWriter w = new StringWriter();
        representation.write(w);
        try {
            return new JSONArray(w.toString());
        } catch (JSONException e) {
            log.info("Failed due to", e);
            fail("Representation wasn't valid JSON: " + e.getMessage());
            return null;
        }
    }
}
