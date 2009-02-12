package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.*;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.*;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdElement.CURRENT_SCHEDULED_ACTIVITY_STATE;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

import java.util.Date;

/**
 * @author John Dzak
 */
public class CurrentScheduledActivityStateXmlSerializer  extends AbstractScheduledActivityStateXmlSerializer {
    @Override protected XsdElement element() { return CURRENT_SCHEDULED_ACTIVITY_STATE; }

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
            return scheduledActivityState;
        }

        if (date != null) {
            scheduledActivityState.setDate(date);
        }
        if (reason != null && StringUtils.isNotEmpty(reason)) {
            scheduledActivityState.setReason(reason);
        }
        return scheduledActivityState;

    }
}