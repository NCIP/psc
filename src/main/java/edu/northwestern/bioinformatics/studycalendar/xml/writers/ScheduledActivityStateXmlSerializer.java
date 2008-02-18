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
    private static final String MISSED = "missed";
    private static final String SCHEDULED = "scheduled";
    private static final String OCCURRED = "occurred";
    private static final String CANCELED = "canceled";
    private static final String CONDITIONAL = "conditional";
    private static final String NOT_APPLICABLE = "not-applicable";

    @Override
    public Element createElement(ScheduledActivityState state) {
        Element elt = SCHEDULED_ACTIVITY_STATE.create();
        SCHEDULED_ACTIVITY_STATE_REASON.addTo(elt, state.getReason());
        if (state instanceof DatedScheduledActivityState) {
            SCHEDULED_ACTIVITY_STATE_DATE.addTo(elt, ((DatedScheduledActivityState)state).getDate());
        }

        if (ScheduledActivityMode.SCHEDULED.equals(state.getMode())) {
            SCHEDULED_ACTIVITY_STATE_STATE.addTo(elt, SCHEDULED);
        } else if (ScheduledActivityMode.OCCURRED.equals(state.getMode())) {
            SCHEDULED_ACTIVITY_STATE_STATE.addTo(elt, OCCURRED);
        } else if (ScheduledActivityMode.CANCELED.equals(state.getMode())) {
            SCHEDULED_ACTIVITY_STATE_STATE.addTo(elt, CANCELED);
        } else if (ScheduledActivityMode.CONDITIONAL.equals(state.getMode())) {
            SCHEDULED_ACTIVITY_STATE_STATE.addTo(elt, CONDITIONAL);
        } else if (ScheduledActivityMode.NOT_APPLICABLE.equals(state.getMode())) {
            SCHEDULED_ACTIVITY_STATE_STATE.addTo(elt, NOT_APPLICABLE);
        } else if (ScheduledActivityMode.MISSED.equals(state.getMode())) {
            SCHEDULED_ACTIVITY_STATE_STATE.addTo(elt, MISSED);
        }
        return elt;
    }

    @Override
    public ScheduledActivityState readElement(Element element) {
        throw new UnsupportedOperationException("Functionality to read a scheduled activity state element does not exist");
    }
}
