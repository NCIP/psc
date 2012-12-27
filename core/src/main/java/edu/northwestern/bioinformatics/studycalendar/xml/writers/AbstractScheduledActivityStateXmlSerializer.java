/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityState;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.*;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import org.dom4j.Element;

/**
 * @author John Dzak
 */
public abstract class AbstractScheduledActivityStateXmlSerializer extends AbstractStudyCalendarXmlSerializer<ScheduledActivityState> {
    protected static final String MISSED = "missed";
    protected static final String SCHEDULED = "scheduled";
    protected static final String OCCURRED = "occurred";
    public static final String CANCELED = "canceled";
    public static final String CONDITIONAL = "conditional";
    public static final String NOT_APPLICABLE = "not-applicable";

    @Override
    public Element createElement(ScheduledActivityState state) {
        Element elt = element().create();
        SCHEDULED_ACTIVITY_STATE_REASON.addTo(elt, state.getReason());
        SCHEDULED_ACTIVITY_STATE_DATE.addTo(elt, state.getDate());

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


    protected abstract XsdElement element();
}
