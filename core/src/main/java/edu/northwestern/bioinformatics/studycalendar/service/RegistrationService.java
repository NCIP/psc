package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarAuthorizationException;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySegmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.domain.SubjectProperty;
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
        Subject existingSubject = subjectService.findSubject(registration.getSubject());
        if (existingSubject != null) {
            Subject newSubject = registration.getSubject();
            mergeSubjectProperties(newSubject, existingSubject);
            registration.setSubject(existingSubject);
        } else if (!ussr.getCanCreateSubjects()) {
            throw new StudyCalendarAuthorizationException(
                "%s may not create a new subject.", studySubjectCalendarManager);
        }
        return registration;
    }

    private void mergeSubjectProperties(Subject newSubject, Subject existingSubject) {
        for (SubjectProperty newProperty : newSubject.getProperties()) {
            boolean propertyExists = false;
            for (SubjectProperty existingProperty : existingSubject.getProperties()) {
                if (existingProperty.getName().equals(newProperty.getName())) {
                    existingProperty.setValue(newProperty.getValue());
                    propertyExists = true;
                    break;
                }
            }
            if (!propertyExists) {
                existingSubject.getProperties().add(newProperty);
            }
        }
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
