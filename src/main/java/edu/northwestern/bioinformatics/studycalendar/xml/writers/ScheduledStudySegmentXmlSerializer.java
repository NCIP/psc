package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.SCHEDULED_STUDY_SEGMENT_START_DATE;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.SCHEDULED_STUDY_SEGMENT_START_DAY;
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
        SCHEDULED_STUDY_SEGMENT_START_DATE.addTo(elt, segment.getStartDate());
        SCHEDULED_STUDY_SEGMENT_START_DAY.addTo(elt, segment.getStartDay());

        for (ScheduledActivity activity : segment.getActivities()) {
            elt.add(scheduledActivitySerializer.createElement(activity));
        }
        return elt;
    }

    @Override
    public ScheduledStudySegment readElement(Element element) {
        throw new UnsupportedOperationException("Functionality to read a scheduled study segment element does not exist");
    }

    @Required
    public void setScheduledActivityXmlSerializer(ScheduledActivityXmlSerializer scheduledActivitySerializer) {
        this.scheduledActivitySerializer = scheduledActivitySerializer;
    }
}
