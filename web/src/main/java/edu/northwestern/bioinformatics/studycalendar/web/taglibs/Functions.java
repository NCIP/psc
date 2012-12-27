/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.taglibs;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;

import java.beans.Expression;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.capitalize;

public class Functions {
    public static Collection pluck(Collection collection, String property) {
        Collection result = new ArrayList(collection.size());
        for(Object o : collection) {
            try {
                Object plucked = new Expression(o, "get" + capitalize(property), null).getValue();
                result.add(plucked);
            } catch (Exception e) {
                throw new StudyCalendarError("Unable to get the '" + property + "' property", e);
            }

        }
        return result;
    }

    public static Map<String, String> JS_ESCAPE_MAP = new MapBuilder<String, String>().
        put("\\\\", "\\\\\\\\").
        put("</", "<\\\\/").
        put("\"", "\\\\\"").
        put("\'", "\\\\\'").
        toMap();

    // Logic is based on code from
    // http://api.rubyonrails.org/classes/ActionView/Helpers/JavaScriptHelper.html#method-i-escape_javascript
    public static String escapeJavascript(String in) {
        String result = in;
        for (String unescaped : JS_ESCAPE_MAP.keySet()) {
            result = result.replaceAll(unescaped, JS_ESCAPE_MAP.get(unescaped));
        }
        return result;
    }
}
