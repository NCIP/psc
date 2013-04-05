/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.utils.editors;

import junit.framework.TestCase;
import org.json.JSONObject;

import java.util.Collections;

/**
 * @author Rhett Sutphin
 */
public class JsonObjectEditorTest extends TestCase {
    private JsonObjectEditor editor = new JsonObjectEditor();

    public void testObjectTextParsed() throws Exception {
        editor.setAsText("{ \"a\": 1, \"t\": 'loud' }");
        Object value = editor.getValue();
        assertTrue("Value should be JSONObject: " + value.getClass().getName(), value instanceof JSONObject);
        JSONObject actual = (JSONObject) value;
        assertEquals("Wrong number of keys", 2, actual.length());
        assertEquals("Wrong entry for 'a'", 1, actual.getInt("a"));
        assertEquals("Wrong entry for 't'", "loud", actual.getString("t"));
    }

    public void testValueConvertedToText() throws Exception {
        editor.setValue(new JSONObject(Collections.singletonMap("T", "Q")));
        assertEquals("Wrong text", "{\"T\":\"Q\"}", editor.getAsText());
    }

    public void testBadJsonThrowsIllegalArgException() throws Exception {
        try {
            editor.setAsText("{67,");
            fail("exception not thrown");
        } catch (IllegalArgumentException iae) {
            assertEquals("Wrong message", "\"{67,\" is not parseable as JSON.", iae.getMessage());
        }
    }
}
