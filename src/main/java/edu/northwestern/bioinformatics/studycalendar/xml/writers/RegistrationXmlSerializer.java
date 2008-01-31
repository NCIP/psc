package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySegmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlSerializer;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.REGISTRATION_DATE;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.REGISTRATION_FIRST_STUDY_SEGMENT;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdElement.REGISTRATION;
import edu.northwestern.bioinformatics.studycalendar.xml.domain.Registration;
import org.dom4j.Element;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RegistrationXmlSerializer extends AbstractStudyCalendarXmlSerializer<Registration> {
    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    private StudySegmentDao studySegmentDao;

    public Element createElement(Registration registration) {
        Element element = REGISTRATION.create();

        REGISTRATION_FIRST_STUDY_SEGMENT.addTo(element, registration.getFirstStudySegment().getGridId());
        REGISTRATION_DATE.addTo(element, formatter.format(registration.getDate()));

        return element;
    }

    public Registration readElement(Element element) {
        Registration registration = new Registration();

        String segmentGridId = REGISTRATION_FIRST_STUDY_SEGMENT.from(element);
        StudySegment segment = studySegmentDao.getByGridId(segmentGridId);
        if (segment == null) throw new StudyCalendarValidationException("Study Segment with grid id %s not found.", segmentGridId);

        String dateString = REGISTRATION_DATE.from(element);
        Date date;
        try {
            date = formatter.parse(dateString);
        } catch(ParseException pe) {
            throw new StudyCalendarValidationException("Problem parsing date %s", dateString);
        }

        registration.setFirstStudySegment(segment);
        registration.setDate(date);

        return registration;
    }

    public void setStudySegmentDao(StudySegmentDao studySegmentDao) {
        this.studySegmentDao = studySegmentDao;
    }
}
