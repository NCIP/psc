/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;

/**
 * @author Rhett Sutphin
 */
public class MembraneException extends RuntimeException {
    public MembraneException(String message, Object... formatParams) {
        super(String.format(message, formatParams));
    }

    public MembraneException(Throwable cause, String message, Object... formatParams) {
        super(String.format(message, formatParams), cause);
    }
}
