/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdElement.PREVIOUS_SCHEDULED_ACTIVITY_STATE;

/**
 * @author John Dzak
 */
public class PreviousScheduledActivityStateXmlSerializer extends AbstractScheduledActivityStateXmlSerializer {
    @Override protected XsdElement element() { return PREVIOUS_SCHEDULED_ACTIVITY_STATE; }
}
