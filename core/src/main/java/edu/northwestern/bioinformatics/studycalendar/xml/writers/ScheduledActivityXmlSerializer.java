/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityState;
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
    public Element createElement(ScheduledActivity scheduledActivity, boolean inCollection) {
        if (scheduledActivity == null) {
            throw new StudyCalendarSystemException("scheduled activity must not be null");
        }

        Element rootElement = rootElement().create();
        SCHEDULED_ACTIVITY_ID.addTo(rootElement, scheduledActivity.getGridId());
        SCHEDULED_ACTIVITY_IDEAL_DATE.addTo(rootElement, scheduledActivity.getIdealDate());
        SCHEDULED_ACTIVITY_NOTES.addTo(rootElement, scheduledActivity.getNotes());
        SCHEDULED_ACTIVITY_DETAILS.addTo(rootElement, scheduledActivity.getDetails());
        SCHEDULED_ACTIVITY_REPETITION_NUMBER.addTo(rootElement, scheduledActivity.getRepetitionNumber());
        if (scheduledActivity.getPlannedActivity() != null) {
            SCHEDULED_ACTIVITY_PLANNED_ACITIVITY_ID.addTo(rootElement, scheduledActivity.getPlannedActivity().getGridId());
        }

        rootElement.add(currentScheduledActivityStateSerializer.createElement(scheduledActivity.getCurrentState()));

        for (ScheduledActivityState state : scheduledActivity.getPreviousStates()) {
            rootElement.add(previousScheduledActivityStateSerializer.createElement(state));
        }

        return rootElement;
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
