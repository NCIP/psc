/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package gov.nih.nci.cabig.ctms.suite.authorization.plugin;

import gov.nih.nci.cabig.ctms.CommonsSystemException;

/**
 * The exception thrown when an invalid {@link SuiteUser} is validated.
 *
 * @since 2.10
 * @author Rhett Sutphin
 */
public class InvalidSuiteUserException extends CommonsSystemException {
    public InvalidSuiteUserException(String message, Object... messageValues) {
        super(message, messageValues);
    }

    public InvalidSuiteUserException(String message, Throwable cause, Object... messageValues) {
        super(message, cause, messageValues);
    }

    public InvalidSuiteUserException(Throwable cause) {
        super(cause);
    }
}
