/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.*;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdElement.SCHEDULED_STUDY_SEGMENT;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author John Dzak
 */
public class ScheduledStudySegmentXmlSerializer extends AbstractStudyCalendarXmlSerializer<ScheduledStudySegment> {
    private ScheduledActivityXmlSerializer scheduledActivitySerializer;

    @Override
    public Element createElement(ScheduledStudySegment segment) {
        Element elt = SCHEDULED_STUDY_SEGMENT.create();
        SCHEDULED_STUDY_SEGMENT_ID.addTo(elt, segment.getGridId());
        SCHEDULED_STUDY_SEGMENT_START_DATE.addTo(elt, segment.getStartDate());
        SCHEDULED_STUDY_SEGMENT_START_DAY.addTo(elt, segment.getStartDay());
        XsdAttribute.SCHEDULED_STUDY_SEGMENT_STUDY_SEGMENT_ID.addTo(elt, segment.getStudySegment().getGridId());

        for (ScheduledActivity activity : segment.getActivities()) {
            elt.add(scheduledActivitySerializer.createElement(activity));
        }
        return elt;
    }

    @Override
    public ScheduledStudySegment readElement(Element elt) {
        throw new UnsupportedOperationException();
    }

    @Required
    public void setScheduledActivityXmlSerializer(ScheduledActivityXmlSerializer scheduledActivitySerializer) {
        this.scheduledActivitySerializer = scheduledActivitySerializer;
    }

}
