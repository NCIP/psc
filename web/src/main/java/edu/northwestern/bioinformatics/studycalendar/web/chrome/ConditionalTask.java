/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.chrome;

import gov.nih.nci.cabig.ctms.web.chrome.Task;

/**
 * @author Rhett Sutphin
 */
public abstract class ConditionalTask extends Task {
    public abstract boolean isEnabled();
}
