package edu.northwestern.bioinformatics.studycalendar.web.taglibs;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;

import java.beans.Expression;
import java.util.ArrayList;
import java.util.Collection;

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
}
