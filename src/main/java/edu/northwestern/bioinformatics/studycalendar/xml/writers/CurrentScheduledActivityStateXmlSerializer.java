package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdElement.CURRENT_SCHEDULED_ACTIVITY_STATE;

/**
 * @author John Dzak
 */
public class CurrentScheduledActivityStateXmlSerializer  extends AbstractScheduledActivityStateXmlSerializer {
    @Override protected XsdElement element() { return CURRENT_SCHEDULED_ACTIVITY_STATE; }
}