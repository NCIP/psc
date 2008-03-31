package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySegmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.NextStudySegmentMode;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.*;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import edu.northwestern.bioinformatics.studycalendar.xml.domain.NextStudySegmentSchedule;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Required;

import java.util.Date;

/**
 * @author John Dzak
 */
public class NextStudySegmentScheduleXmlSerializer extends AbstractStudyCalendarXmlSerializer<NextStudySegmentSchedule> {
    private StudySegmentDao studySegmentDao;
    private final String PER_PROTOCOL = "per-protocol";
    private final String IMMEDIATE = "immediate";

    public Element createElement(NextStudySegmentSchedule object) {
        throw new UnsupportedOperationException();
    }

    public NextStudySegmentSchedule readElement(Element elt) {
        if (!XsdElement.NEXT_STUDY_SEGMENT_SCHEDULE.xmlName().equals(elt.getName())) {
            throw new StudyCalendarValidationException("The element must be a <next-study-segment-schedule> element");
        }
        
        Integer startDay;
        try {
            startDay = Integer.valueOf(NEXT_STUDY_SEGMENT_SCHEDULE_START_DAY.from(elt));
        } catch(NumberFormatException nfe) {
            throw new StudyCalendarValidationException("The scheduled study segment start date must be an integer value");
        }

        Date startDate = NEXT_STUDY_SEGMENT_SCHEDULE_START_DATE.fromDate(elt);

        StudySegment segment = studySegmentDao.getByGridId(NEXT_STUDY_SEGMENT_SCHEDULE_STUDY_SEGMENT_ID.from(elt));
        if (segment == null) {
            throw new StudyCalendarValidationException("The study segment specified could not be found");
        }

        NextStudySegmentMode mode;
        if (PER_PROTOCOL.equals(NEXT_STUDY_SEGMENT_SCHEDULE_MODE.from(elt))) {
            mode = NextStudySegmentMode.PER_PROTOCOL;
        } else if (IMMEDIATE.equals(NEXT_STUDY_SEGMENT_SCHEDULE_MODE.from(elt))) {
            mode = NextStudySegmentMode.IMMEDIATE;
        } else {
            throw new StudyCalendarValidationException("The next study segment mode must be either 'per-protocol' or 'immediate'");
        }

        NextStudySegmentSchedule schedule = new NextStudySegmentSchedule();
        schedule.setStartDay(startDay);
        schedule.setStartDate(startDate);
        schedule.setStudySegment(segment);
        schedule.setMode(mode);

        return schedule;
    }

    @Required
    public void setStudySegmentDao(StudySegmentDao studySegmentDao) {
        this.studySegmentDao = studySegmentDao;
    }
}
