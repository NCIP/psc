package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySegmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUserDetailsService;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.UserStudySiteRelationship;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.Registration;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Jalpa Patel
 */
public class RegistrationService {
    private StudySegmentDao studySegmentDao;
    private SubjectService subjectService;
    private PscUserDetailsService userService;

    public Registration resolveRegistration(Registration registration, StudySite studySite) {
        if (registration.getStudySubjectCalendarManager() != null) {
            registration.setStudySubjectCalendarManager(
                resolvePscUser(registration.getStudySubjectCalendarManager()));
        }

        StudySegment segment = studySegmentDao.getByGridId(registration.getFirstStudySegment().getGridId());
        if (segment == null) {
            throw new StudyCalendarValidationException("Study Segment with grid id %s not found.",
                    registration.getFirstStudySegment().getGridId());
        }
        registration.setFirstStudySegment(segment);

        PscUser studySubjectCalendarManager = registration.getStudySubjectCalendarManager();
        UserStudySiteRelationship ussr = new UserStudySiteRelationship(studySubjectCalendarManager, studySite);
        Subject subject = subjectService.findSubject(registration.getSubject());
        if (subject != null) {
            registration.setSubject(subject);
        } else if (!ussr.getCanCreateSubjects()) {
            throw new StudyCalendarValidationException("%s has insufficient privilege to create new subject.", studySubjectCalendarManager);
        }
        return registration;
    }

    private PscUser resolvePscUser(PscUser spec) {
        return userService.loadUserByUsername(spec.getUsername());
    }

    @Required
    public void setStudySegmentDao(StudySegmentDao studySegmentDao) {
        this.studySegmentDao = studySegmentDao;
    }

    @Required
    public void setSubjectService(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @Required
    public void setPscUserDetailsService(PscUserDetailsService userDetailsService) {
        userService = userDetailsService;
    }
}
