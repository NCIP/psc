package edu.northwestern.bioinformatics.studycalendar.xml.domain;

import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.domain.User;

import java.util.Date;

/**
 * @author Rhett Sutphin
 */
public class Registration {
    private StudySegment firstStudySegment;
    private Date date;
    private Subject subject;
    private User subjectCoordinator;
    private String desiredStudySubjectAssignmentId;

    public static Registration create(StudySegment firstStudySegment, Date date, Subject subject) {
        Registration registration = new Registration();
        registration.firstStudySegment = firstStudySegment;
        registration.date = date;
        registration.subject = subject;
        return registration;
    }

    public static Registration create(StudySegment firstStudySegment, Date date, Subject subject, String desiredStudySubjectAssignmentId) {
        Registration registration = create(firstStudySegment, date, subject);
        registration.setDesiredStudySubjectAssignmentId(desiredStudySubjectAssignmentId);
        return registration;
    }

    public StudySegment getFirstStudySegment() {
        return firstStudySegment;
    }

    public void setFirstStudySegment(StudySegment firstStudySegment) {
        this.firstStudySegment = firstStudySegment;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public User getSubjectCoordinator() {
        return subjectCoordinator;
    }

    public void setSubjectCoordinator(User subjectCoordinator) {
        this.subjectCoordinator = subjectCoordinator;
    }

    public String getDesiredStudySubjectAssignmentId() {
        return desiredStudySubjectAssignmentId;
    }

    public void setDesiredStudySubjectAssignmentId(String desiredStudySubjectAssignmentId) {
        this.desiredStudySubjectAssignmentId = desiredStudySubjectAssignmentId;
    }
}
