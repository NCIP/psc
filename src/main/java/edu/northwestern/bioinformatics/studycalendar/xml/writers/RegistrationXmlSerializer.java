package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySegmentDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.xml.AbstractStudyCalendarXmlCollectionSerializer;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdAttribute.*;
import edu.northwestern.bioinformatics.studycalendar.xml.XsdElement;
import static edu.northwestern.bioinformatics.studycalendar.xml.XsdElement.REGISTRATION;
import edu.northwestern.bioinformatics.studycalendar.xml.domain.Registration;
import org.dom4j.Element;

import java.util.Date;

public class RegistrationXmlSerializer extends AbstractStudyCalendarXmlCollectionSerializer<Registration> {
    private StudySegmentDao studySegmentDao;
    private UserDao userDao;

    protected XsdElement rootElement() { return XsdElement.REGISTRATION; }
    protected XsdElement collectionRootElement() { return XsdElement.REGISTRATIONS; }

    public Element createElement(Registration reg, boolean inCollection) {
        Element elt = REGISTRATION.create();

        REGISTRATION_FIRST_STUDY_SEGMENT.addTo(elt, reg.getFirstStudySegment().getGridId());
        REGISTRATION_DATE.addTo(elt, reg.getDate());
        if (reg.getSubjectCoordinator() != null) {
            REGISTRATION_SUBJECT_COORDINATOR_NAME.addTo(elt, reg.getSubjectCoordinator().getName());
        }
        
        REGISTRATION_DESIRED_ASSIGNMENT_ID.addTo(elt, reg.getDesiredStudySubjectAssignmentId());

        return elt;
    }

    public Registration readElement(Element elt) {
        Registration reg = new Registration();

        String segmentId = REGISTRATION_FIRST_STUDY_SEGMENT.from(elt);
        StudySegment segment = studySegmentDao.getByGridId(segmentId);
        if (segment == null) throw new StudyCalendarValidationException("Study Segment with grid id %s not found.", segmentId);

        Date date = REGISTRATION_DATE.fromDate(elt);

        String subjCoordName = REGISTRATION_SUBJECT_COORDINATOR_NAME.from(elt);
        if (subjCoordName != null) {
            User subjCoord = userDao.getByName(subjCoordName);
            reg.setSubjectCoordinator(subjCoord);
        }

        String desiredAssignId = REGISTRATION_DESIRED_ASSIGNMENT_ID.from(elt);

        reg.setFirstStudySegment(segment);
        reg.setDate(date);
        reg.setDesiredStudySubjectAssignmentId(desiredAssignId);

        return reg;
    }

    public void setStudySegmentDao(StudySegmentDao studySegmentDao) {
        this.studySegmentDao = studySegmentDao;
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }
}
