/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets.representations;

import edu.northwestern.bioinformatics.studycalendar.restlets.AbstractPscResource;
import org.codehaus.jackson.JsonGenerator;

import java.io.IOException;
import java.util.Date;

/**
 * @author Rhett Sutphin
 */
public class JacksonTools {
    @SuppressWarnings({ "ChainOfInstanceofChecks" })
    public static void nullSafeWritePrimitiveField(JsonGenerator g, String fieldName, Object value) throws IOException {
        if (value instanceof Integer) {
            g.writeNumberField(fieldName, (Integer) value);
        } else if (value instanceof Long) {
            g.writeNumberField(fieldName, (Long) value);
        } else if (value instanceof Float) {
            g.writeNumberField(fieldName, (Float) value);
        } else if (value instanceof Double) {
            g.writeNumberField(fieldName, (Double) value);
        } else if (value instanceof Boolean) {
            g.writeBooleanField(fieldName, (Boolean) value);
        } else if (value instanceof String) {
            g.writeStringField(fieldName, (String) value);
        }
    }

    public static void nullSafeWriteStringField(JsonGenerator g, String fieldName, String value) throws IOException {
        if (value != null) {
            g.writeStringField(fieldName, value);
        }
    }

    public static void nullSafeWriteDateField(JsonGenerator g, String fieldName, Date date) throws IOException {
        if (date != null) {
            g.writeStringField(fieldName, AbstractPscResource.getApiDateFormat().format(date));
        }
    }
}
