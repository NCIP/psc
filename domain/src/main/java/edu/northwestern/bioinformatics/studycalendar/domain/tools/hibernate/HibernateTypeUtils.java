/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain.tools.hibernate;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Utilities to assist {@link org.hibernate.usertype.UserType}s in behaving more like
 * built in types.
 *
 * @author Rhett Sutphin
 */
public final class HibernateTypeUtils {
    private HibernateTypeUtils() { }

    public static Logger getLog(Class clazz) {
        return LoggerFactory.getLogger(getLogCategory(clazz));
    }

    /**
     * Provide a log category for the given class that is similar to that used by hibernate for
     * its own type classes.  This makes it easier to filter the log entries to show all type
     * bindings together.
     * @param clazz the type class
     * @return a log category starting with org.hibernate.type
     */
    public static String getLogCategory(Class clazz) {
        return new StringBuffer("org.hibernate.type.studycalendar.")
            .append(clazz.getSimpleName())
            .toString();
    }

    public static void logBind(Logger log, int index, Object value) {
        if (log.isDebugEnabled()) {
            StringBuffer msg = new StringBuffer("binding ");
            appendBoundValue(value, msg);
            msg.append(" to parameter: ").append(index);
            log.debug(msg.toString());
        }
    }

    private static StringBuffer appendBoundValue(Object value, StringBuffer msg) {
        if (value == null) {
            msg.append("null");
        } else {
            msg.append("'").append(value).append("'");
        }
        return msg;
    }

    public static void logReturn(Logger log, String column, Object value) {
        if (log.isDebugEnabled()) {
            StringBuffer msg = new StringBuffer("returning ");
            appendBoundValue(value, msg);
            msg.append(" as column: ").append(column);
            log.debug(msg.toString());
        }
    }
}
