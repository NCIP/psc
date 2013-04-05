/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.utils.editors;

import org.json.JSONException;
import org.json.JSONObject;

import java.beans.PropertyEditorSupport;

/**
 * @author Rhett Sutphin
 */
public class JsonObjectEditor extends PropertyEditorSupport {
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        try {
            setValue(new JSONObject(text));
        } catch (JSONException e) {
            throw new IllegalArgumentException("\"" + text + "\" is not parseable as JSON.", e);
        }
    }
}
