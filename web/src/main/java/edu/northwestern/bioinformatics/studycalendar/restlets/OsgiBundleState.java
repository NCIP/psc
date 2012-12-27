/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import org.osgi.framework.Bundle;

/**
 * Enum representation of the six states an OSGi {@link Bundle} may be in. This
 * is primarily to ease conversion between the actual constant values and string
 * representations of them in the API.
 *
 * @author Rhett Sutphin
 */
public enum OsgiBundleState {
    UNINSTALLED,
    INSTALLED,
    RESOLVED,
    STARTING,
    STOPPING,
    ACTIVE;

    private int bundleConstant;

    OsgiBundleState() {
        try {
            bundleConstant = (Integer) Bundle.class.getField(name()).get(null);
        } catch (IllegalAccessException e) {
            throw new StudyCalendarError("This shouldn't be possible", e);
        } catch (NoSuchFieldException e) {
            throw new StudyCalendarError("This shouldn't be possible", e);
        }
    }

    public int constant() {
        return bundleConstant;
    }

    public static OsgiBundleState valueOfConstant(int constant) {
        for (OsgiBundleState state : values()) {
            if (state.constant() == constant) return state;
        }
        throw new IllegalArgumentException(
            "Unknown bundle state 0x" + Integer.toHexString(constant).toUpperCase());
    }
}
