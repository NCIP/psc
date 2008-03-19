package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.ScheduledActivityState;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlCollectionSerializer;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.*;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author John Dzak
 */
public class ScheduledActivityXmlSerializer extends AbstractStudyCalendarXmlCollectionSerializer<ScheduledActivity> {
    private CurrentScheduledActivityStateXmlSerializer currentScheduledActivityStateSerializer;
    private PreviousScheduledActivityStateXmlSerializer previousScheduledActivityStateSerializer;

    protected XsdElement collectionRootElement() {
        return XsdElement.SCHEDULED_ACTIVITIES;
    }

    protected XsdElement rootElement() {
        return XsdElement.SCHEDULED_ACTIVITY;
    }

    @Override
    public Element createElement(final ScheduledActivity scheduledActivity, final boolean inCollection) {

        if (scheduledActivity == null) {
            throw new StudyCalendarValidationException("activity can not be null");

        }
        Element rootElement = rootElement().create();
        SCHEDULED_ACTIVITY_ID.addTo(rootElement, scheduledActivity.getGridId());
        SCHEDULED_ACTIVITY_IDEAL_DATE.addTo(rootElement, scheduledActivity.getIdealDate());
        SCHEDULED_ACTIVITY_NOTES.addTo(rootElement, scheduledActivity.getNotes());
        SCHEDULED_ACTIVITY_DETAILS.addTo(rootElement, scheduledActivity.getDetails());
        SCHEDULED_ACTIVITY_REPITITION_NUMBER.addTo(rootElement, scheduledActivity.getRepetitionNumber());
        if (scheduledActivity.getPlannedActivity() != null) {
            SCHEDULED_ACTIVITY_PLANNED_ACITIVITY_ID.addTo(rootElement, scheduledActivity.getPlannedActivity().getGridId());
        }

        rootElement.add(currentScheduledActivityStateSerializer.createElement(scheduledActivity.getCurrentState()));

        for (ScheduledActivityState state : scheduledActivity.getPreviousStates()) {
            rootElement.add(previousScheduledActivityStateSerializer.createElement(state));
        }


        if (inCollection) {
            return rootElement;

        } else {
            Element root = collectionRootElement().create();
            root.add(rootElement);
            return root;
        }


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
