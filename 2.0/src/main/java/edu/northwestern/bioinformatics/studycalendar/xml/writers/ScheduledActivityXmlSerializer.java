package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.ScheduledActivityState;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.*;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdElement.SCHEDULED_ACTIVITY;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author John Dzak
 */
public class ScheduledActivityXmlSerializer extends AbstractStudyCalendarXmlSerializer<ScheduledActivity> {
    private CurrentScheduledActivityStateXmlSerializer currentScheduledActivityStateSerializer;
    private PreviousScheduledActivityStateXmlSerializer previousScheduledActivityStateSerializer;

    @Override
    public Element createElement(ScheduledActivity activity) {
        Element elt = SCHEDULED_ACTIVITY.create();
        SCHEDULED_ACTIVITY_ID.addTo(elt, activity.getGridId());
        SCHEDULED_ACTIVITY_IDEAL_DATE.addTo(elt, activity.getIdealDate());
        SCHEDULED_ACTIVITY_NOTES.addTo(elt, activity.getNotes());
        SCHEDULED_ACTIVITY_DETAILS.addTo(elt, activity.getDetails());
        SCHEDULED_ACTIVITY_REPITITION_NUMBER.addTo(elt, activity.getRepetitionNumber());
        if (activity.getPlannedActivity() != null) {
            SCHEDULED_ACTIVITY_PLANNED_ACITIVITY_ID.addTo(elt, activity.getPlannedActivity().getGridId());
        }

        elt.add(currentScheduledActivityStateSerializer.createElement(activity.getCurrentState()));

        for (ScheduledActivityState state : activity.getPreviousStates()) {
            elt.add(previousScheduledActivityStateSerializer.createElement(state));
        }

        return elt;
    }

    @Override
    public ScheduledActivity readElement(Element element) {
        throw new UnsupportedOperationException("Functionality to read a scheduled activity element does not exist");
    }

    ////// Bean Setters

    @Required
    public void setCurrentScheduledActivityStateXmlSerializer(CurrentScheduledActivityStateXmlSerializer currentScheduledActivityStateSerializer) {
        this.currentScheduledActivityStateSerializer = currentScheduledActivityStateSerializer;
    }

    @Required
    public void setPreviousScheduledActivityStateXmlSerializer(PreviousScheduledActivityStateXmlSerializer previousScheduledActivityStateSerializer) {
        this.previousScheduledActivityStateSerializer = previousScheduledActivityStateSerializer;
    }
}
