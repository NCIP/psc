package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.*;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.*;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

import java.util.Date;

/**
 * @author John Dzak
 */
public abstract class AbstractScheduledActivityStateXmlSerializer extends AbstractStudyCalendarXmlSerializer<ScheduledActivityState> {
    private static final String MISSED = "missed";
    private static final String SCHEDULED = "scheduled";
    private static final String OCCURRED = "occurred";
    public static final String CANCELED = "canceled";
    public static final String CONDITIONAL = "conditional";
    public static final String NOT_APPLICABLE = "not-applicable";

    @Override
    public Element createElement(ScheduledActivityState state) {
        Element elt = element().create();
        SCHEDULED_ACTIVITY_STATE_REASON.addTo(elt, state.getReason());
        if (state instanceof DatedScheduledActivityState) {
            SCHEDULED_ACTIVITY_STATE_DATE.addTo(elt, ((DatedScheduledActivityState) state).getDate());
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

        ScheduledActivityState scheduledActivityState = null;

        String state = SCHEDULED_ACTIVITY_STATE_STATE.from(element);
        String reason = SCHEDULED_ACTIVITY_STATE_REASON.from(element);

        Date date = SCHEDULED_ACTIVITY_STATE_DATE.fromDate(element);

        if (state == null || StringUtils.isEmpty(state)) {
            return null;
        } else if (state.equals(CONDITIONAL)) {
            scheduledActivityState = new Conditional();
        } else if (state.equals(OCCURRED)) {
            scheduledActivityState = new Occurred();
        } else if (state.equals(SCHEDULED)) {
            scheduledActivityState = new Scheduled();
        } else if (state.equals(MISSED)) {
            scheduledActivityState = new Missed();
        } else if (state.equals(NOT_APPLICABLE)) {
            scheduledActivityState = new NotApplicable();
        } else if (state.equals(CANCELED)) {
            scheduledActivityState = new Canceled();
        } else {
            return null;
        }

        if (date != null && scheduledActivityState instanceof DatedScheduledActivityState) {
            ((DatedScheduledActivityState) scheduledActivityState).setDate(date);
        }
        if (reason != null && StringUtils.isNotEmpty(reason)) {
            scheduledActivityState.setReason(reason);
        }
        return scheduledActivityState;

    }

    protected abstract XsdElement element();
}
