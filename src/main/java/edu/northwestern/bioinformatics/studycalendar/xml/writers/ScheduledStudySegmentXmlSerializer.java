package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.SCHEDULED_STUDY_SEGMENT_START_DATE;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.SCHEDULED_STUDY_SEGMENT_START_DAY;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdElement.SCHEDULED_STUDY_SEGMENT;
import org.dom4j.Element;

/**
 * @author John Dzak
 */
public class ScheduledStudySegmentXmlSerializer extends AbstractStudyCalendarXmlSerializer<ScheduledStudySegment> {
    public Element createElement(ScheduledStudySegment segment) {
        Element elt = SCHEDULED_STUDY_SEGMENT.create();
        SCHEDULED_STUDY_SEGMENT_START_DATE.addTo(elt, segment.getStartDate());
        SCHEDULED_STUDY_SEGMENT_START_DAY.addTo(elt, segment.getStartDay());

        // TODO: Call scheduled activity serializer
        return elt;
    }

    public ScheduledStudySegment readElement(Element element) {
        throw new UnsupportedOperationException("Functionality to read a scheduled study segment element does not exist");
    }
}
