package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySegmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.*;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdElement.SCHEDULED_STUDY_SEGMENT;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Required;

import java.util.Date;

/**
 * @author John Dzak
 */
public class ScheduledStudySegmentXmlSerializer extends AbstractStudyCalendarXmlSerializer<ScheduledStudySegment> {
    private ScheduledActivityXmlSerializer scheduledActivitySerializer;
    private StudySegmentDao studySegmentDao;

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
        Integer startDay;
        try {
            startDay = Integer.valueOf(SCHEDULED_STUDY_SEGMENT_START_DAY.from(elt));
        } catch(NumberFormatException nfe) {
            throw new StudyCalendarValidationException("The scheduled study segment start date must be an integer value");
        }

        Date startDate = SCHEDULED_STUDY_SEGMENT_START_DATE.fromDate(elt);

        StudySegment segment = studySegmentDao.getByGridId(SCHEDULED_STUDY_SEGMENT_STUDY_SEGMENT_ID.from(elt));
        if (segment == null) {
            throw new StudyCalendarValidationException("The study segment specified could not be found");
        }

        ScheduledStudySegment schdSegment = new ScheduledStudySegment();
        schdSegment.setStartDay(startDay);
        schdSegment.setStartDate(startDate);
        schdSegment.setStudySegment(segment);
        
        return schdSegment;
    }

    @Required
    public void setScheduledActivityXmlSerializer(ScheduledActivityXmlSerializer scheduledActivitySerializer) {
        this.scheduledActivitySerializer = scheduledActivitySerializer;
    }

    public void setStudySegmentDao(StudySegmentDao studySegmentDao) {
        this.studySegmentDao = studySegmentDao;
    }
}
