/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.domain.NextStudySegmentMode;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.*;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import edu.northwestern.bioinformatics.studycalendar.xml.domain.NextScheduledStudySegment;
import org.dom4j.Element;

import java.util.Date;

/**
 * @author John Dzak
 */
public class NextScheduledStudySegmentXmlSerializer extends AbstractStudyCalendarXmlSerializer<NextScheduledStudySegment> {
    private final String PER_PROTOCOL = "per-protocol";
    private final String IMMEDIATE = "immediate";

    public Element createElement(NextScheduledStudySegment object) {
        throw new UnsupportedOperationException();
    }

    public NextScheduledStudySegment readElement(Element elt) {
        if (!XsdElement.NEXT_SCHEDULED_STUDY_SEGMENT.xmlName().equals(elt.getName())) {
            throw new StudyCalendarValidationException("The element must be a <next-scheduled-study-segment> element");
        }

        Date startDate = NEXT_STUDY_SEGMENT_SCHEDULE_START_DATE.fromDate(elt);
        String segmentId = NEXT_STUDY_SEGMENT_SCHEDULE_STUDY_SEGMENT_ID.from(elt);
        StudySegment segment = new StudySegment();
        segment.setGridId(segmentId);

        NextStudySegmentMode mode;
        if (PER_PROTOCOL.equals(NEXT_STUDY_SEGMENT_SCHEDULE_MODE.from(elt))) {
            mode = NextStudySegmentMode.PER_PROTOCOL;
        } else if (IMMEDIATE.equals(NEXT_STUDY_SEGMENT_SCHEDULE_MODE.from(elt))) {
            mode = NextStudySegmentMode.IMMEDIATE;
        } else {
            throw new StudyCalendarValidationException("The next study segment mode must be either 'per-protocol' or 'immediate'");
        }

        NextScheduledStudySegment scheduled = new NextScheduledStudySegment();
        scheduled.setStartDate(startDate);
        scheduled.setStudySegment(segment);
        scheduled.setMode(mode);

        return scheduled;
    }
}
