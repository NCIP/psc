package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;

import java.util.Date;

public class SubjectOffStudyCommand {
    private StudySubjectAssignment assignment;
    private Date expectedEndDate;
    private SubjectService subjectService;

    public StudySubjectAssignment getAssignment() {
        return assignment;
    }

    public void setAssignment(StudySubjectAssignment assignment) {
        this.assignment = assignment;
    }

    public Date getExpectedEndDate() {
        return expectedEndDate;
    }

    public void setExpectedEndDate(Date expectedEndDate) {
        this.expectedEndDate = expectedEndDate;
    }

    public StudySubjectAssignment takeSubjectOffStudy() {
        return subjectService.takeSubjectOffStudy(assignment, expectedEndDate);
    }

    public void setSubjectService(SubjectService subjectService) {
        this.subjectService = subjectService;
    }
}
