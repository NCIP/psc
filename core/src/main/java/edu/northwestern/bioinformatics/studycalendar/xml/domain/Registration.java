package edu.northwestern.bioinformatics.studycalendar.xml.domain;

import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;

import java.util.Date;

/**
 * @author Rhett Sutphin
 */
public class Registration {
    private StudySegment firstStudySegment;
    private Date date;
    private Subject subject;
    private PscUser studySubjectCalendarManager;
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

    public PscUser getStudySubjectCalendarManager() {
        return studySubjectCalendarManager;
    }

    public void setStudySubjectCalendarManager(PscUser studySubjectCalendarManager) {
        this.studySubjectCalendarManager = studySubjectCalendarManager;
    }

    public String getDesiredStudySubjectAssignmentId() {
        return desiredStudySubjectAssignmentId;
    }

    public void setDesiredStudySubjectAssignmentId(String desiredStudySubjectAssignmentId) {
        this.desiredStudySubjectAssignmentId = desiredStudySubjectAssignmentId;
    }
}
