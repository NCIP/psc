package edu.northwestern.bioinformatics.studycalendar.restlets.representations;

import edu.northwestern.bioinformatics.studycalendar.restlets.RestletTestCase;
import org.json.JSONObject;
import org.json.JSONException;
import org.restlet.resource.Representation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.io.IOException;

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
}
