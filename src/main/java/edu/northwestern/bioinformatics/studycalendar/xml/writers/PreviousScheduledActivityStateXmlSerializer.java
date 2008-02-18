package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdElement.PREVIOUS_SCHEDULED_ACTIVITY_STATE;

/**
 * @author John Dzak
 */
public class PreviousScheduledActivityStateXmlSerializer extends AbstractScheduledActivityStateXmlSerializer {
    @Override protected XsdElement element() { return PREVIOUS_SCHEDULED_ACTIVITY_STATE; }
}
