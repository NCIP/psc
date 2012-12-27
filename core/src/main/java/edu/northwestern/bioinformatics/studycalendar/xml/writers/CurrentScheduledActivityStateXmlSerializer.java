/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityState;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

import java.util.Date;

import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.*;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdElement.CURRENT_SCHEDULED_ACTIVITY_STATE;

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

         // TODO: fix this abstraction failure
        if (state == null || StringUtils.isEmpty(state)) {
            return null;
        } else if (state.equals(CONDITIONAL)) {
            scheduledActivityState = ScheduledActivityMode.CONDITIONAL.createStateInstance();
        } else if (state.equals(OCCURRED)) {
            scheduledActivityState = ScheduledActivityMode.OCCURRED.createStateInstance();
        } else if (state.equals(SCHEDULED)) {
            scheduledActivityState = ScheduledActivityMode.SCHEDULED.createStateInstance();
        } else if (state.equals(MISSED)) {
            scheduledActivityState = ScheduledActivityMode.MISSED.createStateInstance();
        } else if (state.equals(NOT_APPLICABLE)) {
            scheduledActivityState = ScheduledActivityMode.NOT_APPLICABLE.createStateInstance();
        } else if (state.equals(CANCELED)) {
            scheduledActivityState = ScheduledActivityMode.CANCELED.createStateInstance();
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