/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar;

import static java.lang.String.*;

/**
 * Base class for all PSC exceptions.  Cannot be used directly -- a typed exception
 * should be used instead. 
 *
 * @author Rhett Sutphin
 */
public abstract class StudyCalendarRuntimeException extends RuntimeException {
    public StudyCalendarRuntimeException(Throwable cause) {
        super(cause);
    }

    @SuppressWarnings({ "ThrowableResultOfMethodCallIgnored" })
    public StudyCalendarRuntimeException(String message, Object... messageParameters) {
        super(message == null ? null : format(message, withoutCause(messageParameters)), extractCause(messageParameters));
    }

    private static Object[] withoutCause(Object[] messageParameters) {
        if (messageParameters == null) return new Object[0];
        Integer causeIndex = findCauseIndex(messageParameters);
        if (causeIndex == null) {
            return messageParameters;
        } else {
            Object[] without = new Object[messageParameters.length - 1];
            if (causeIndex == 0) {
                System.arraycopy(messageParameters, 1, without, 0, without.length);
            } else {
                System.arraycopy(messageParameters, 0, without, 0, without.length);
            }
            if (without.length > 0 && without[0] instanceof Object[]) {
                return (Object[]) without[0];
            } else {
                return without;
            }
        }
    }

    private static Throwable extractCause(Object[] parameters) {
        if (parameters == null) return null;
        Integer causeIndex = findCauseIndex(parameters);
        if (causeIndex == null) {
            return null;
        } else {
            return (Throwable) parameters[causeIndex];
        }
    }

    private static Integer findCauseIndex(Object[] parameters) {
        if (parameters.length == 0) {
            return null;
        } else if (parameters[0] instanceof Throwable) {
            return 0;
        } else if (parameters[parameters.length - 1] instanceof Throwable) {
            return parameters.length - 1;
        } else {
            return null;
        }
    }
}
