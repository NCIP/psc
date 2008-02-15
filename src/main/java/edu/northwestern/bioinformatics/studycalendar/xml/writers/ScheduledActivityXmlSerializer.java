package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.*;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdElement.SCHEDULED_ACTIVITY;
import org.dom4j.Element;

/**
 * @author John Dzak
 */
public class ScheduledActivityXmlSerializer extends AbstractStudyCalendarXmlSerializer<ScheduledActivity> {

    public Element createElement(ScheduledActivity activity) {
        Element elt = SCHEDULED_ACTIVITY.create();
        SCHEDULED_ACTIVITY_IDEAL_DATE.addTo(elt, activity.getIdealDate());
        SCHEDULED_ACTIVITY_NOTES.addTo(elt, activity.getNotes());
        SCHEDULED_ACTIVITY_DETAILS.addTo(elt, activity.getDetails());
        SCHEDULED_ACTIVITY_REPITITION_NUMBER.addTo(elt, activity.getRepetitionNumber());
        if (activity.getPlannedActivity() != null) {
            SCHEDULED_ACTIVITY_PLANNED_ACITIVITY_ID.addTo(elt, activity.getPlannedActivity().getGridId());
        }

        return elt;
    }

    public ScheduledActivity readElement(Element element) {
        throw new UnsupportedOperationException("Functionality to read a scheduled activity element does not exist");
    }
}
