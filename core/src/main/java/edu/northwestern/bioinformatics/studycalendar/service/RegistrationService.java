package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.StudySegmentDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.xml.domain.Registration;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Jalpa Patel
 */
public class RegistrationService {
    private StudySegmentDao studySegmentDao;
    private UserDao userDao;
    private SubjectService subjectService;

    public Registration resolveRegistration(Registration registration) {
        if (registration.getSubjectCoordinator() != null) {
            User subjectCo = userDao.getByName(registration.getSubjectCoordinator().getName());
            registration.setSubjectCoordinator(subjectCo);
        }

        StudySegment segment = studySegmentDao.getByGridId(registration.getFirstStudySegment().getGridId());
        if (segment == null) {
            throw new StudyCalendarSystemException("Study Segment with grid id %s not found.",
                    registration.getFirstStudySegment().getGridId());
        }
        registration.setFirstStudySegment(segment);

        Subject subject = subjectService.findSubject(registration.getSubject());
        if (subject != null) {
            registration.setSubject(subject);
        }
        return registration;
    }

    @Required
    public void setStudySegmentDao(StudySegmentDao studySegmentDao) {
        this.studySegmentDao = studySegmentDao;
    }

    @Required
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    @Required
    public void setSubjectService(SubjectService subjectService) {
        this.subjectService = subjectService;
    }
}
