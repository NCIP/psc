package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.DatedScheduledActivityState;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.ScheduledActivityState;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdElement.SCHEDULED_ACTIVITY_STATE;
import org.dom4j.Element;

/**
 * @author John Dzak
 */
public class ScheduledActivityStateXmlSerializer extends AbstractStudyCalendarXmlSerializer<ScheduledActivityState> {
    public Element createElement(ScheduledActivityState state) {
        Element elt = SCHEDULED_ACTIVITY_STATE.create();
        XsdAttribute.SCHEDULED_ACTIVITY_STATE_REASON.addTo(elt, state.getReason());
        if (state instanceof DatedScheduledActivityState) {
            XsdAttribute.SCHEDULED_ACTIVITY_STATE_DATE.addTo(elt, ((DatedScheduledActivityState)state).getDate());
        }
        return elt;
    }

    public ScheduledActivityState readElement(Element element) {
        throw new UnsupportedOperationException();
    }
}
