package edu.northwestern.bioinformatics.studycalendar.xml.domain;

import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;

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
        registration.setFirstStudySegment(firstStudySegment);
        registration.setDate(date);
        registration.setSubject(subject);
        return registration;
    }

    public static Registration create(StudySegment firstStudySegment, Date date, Subject subject, String desiredStudySubjectAssignmentId) {
        Registration registration = create(firstStudySegment, date, subject);
        registration.setDesiredStudySubjectAssignmentId(desiredStudySubjectAssignmentId);
        return registration;
    }

    public static Registration create(StudySubjectAssignment assignment) {
        Registration reg = new Registration();
        reg.setDesiredStudySubjectAssignmentId(assignment.getGridId());
        reg.setDate(assignment.getStartDate());
        reg.setFirstStudySegment(assignment.getScheduledCalendar().getScheduledStudySegments().get(0).getStudySegment());
        reg.setSubject(assignment.getSubject());
        reg.setSubjectCoordinator(assignment.getSubjectCoordinator());
        return reg;
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
