package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.DatedScheduledActivityState;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.ScheduledActivityState;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.*;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdElement.SCHEDULED_ACTIVITY_STATE;
import org.dom4j.Element;

/**
 * @author John Dzak
 */
public class ScheduledActivityStateXmlSerializer extends AbstractStudyCalendarXmlSerializer<ScheduledActivityState> {
    private final String SCHEDULED = "scheduled";

    @Override
    public Element createElement(ScheduledActivityState state) {
        Element elt = SCHEDULED_ACTIVITY_STATE.create();
        SCHEDULED_ACTIVITY_STATE_REASON.addTo(elt, state.getReason());
        if (state instanceof DatedScheduledActivityState) {
            SCHEDULED_ACTIVITY_STATE_DATE.addTo(elt, ((DatedScheduledActivityState)state).getDate());
        }

        if(state.getMode().equals(ScheduledActivityMode.SCHEDULED)) {
            SCHEDULED_ACTIVITY_STATE_STATE.addTo(elt, SCHEDULED);
        }
        return elt;
    }

    @Override
    public ScheduledActivityState readElement(Element element) {
        throw new UnsupportedOperationException("Functionality to read a scheduled activity state element does not exist");
    }
}
