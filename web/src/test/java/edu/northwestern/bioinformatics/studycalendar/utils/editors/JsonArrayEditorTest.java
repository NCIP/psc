/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.utils.editors;

import junit.framework.TestCase;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * @author Rhett Sutphin
 */
public class JsonArrayEditorTest extends TestCase {
    private JsonArrayEditor editor = new JsonArrayEditor();

    public void testArrayTextParsed() throws Exception {
        editor.setAsText("[{ \"a\": 1 }, null]");
        Object value = editor.getValue();
        assertTrue("Value should be JSONArray: " + value.getClass().getName(), value instanceof JSONArray);
        JSONArray actual = (JSONArray) value;
        assertEquals("Wrong number of array entries", 2, actual.length());
        assertEquals("Wrong first entry", 1, actual.getJSONObject(0).getInt("a"));
        assertEquals("Wrong second entry", JSONObject.NULL, actual.get(1));
    }

    public void testValueConvertedToText() throws Exception {
        editor.setValue(new JSONArray(Arrays.asList("T", "Q")));
        assertEquals("Wrong text", "[\"T\",\"Q\"]", editor.getAsText());
    }

    public void testBadJsonThrowsIllegalArgException() throws Exception {
        try {
            editor.setAsText("[67,");
            fail("exception not thrown");
        } catch (IllegalArgumentException iae) {
            assertEquals("Wrong message", "\"[67,\" is not parseable as JSON.", iae.getMessage());
        }
    }
}
