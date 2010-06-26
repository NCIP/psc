package edu.northwestern.bioinformatics.studycalendar.utils.editors;

import org.json.JSONArray;
import org.json.JSONException;

import java.beans.PropertyEditorSupport;

/**
 * @author Rhett Sutphin
 */
public class JsonArrayEditor extends PropertyEditorSupport {
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        try {
            setValue(new JSONArray(text));
        } catch (JSONException e) {
            throw new IllegalArgumentException("\"" + text + "\" is not parseable as JSON.", e);
        }
    }
}
