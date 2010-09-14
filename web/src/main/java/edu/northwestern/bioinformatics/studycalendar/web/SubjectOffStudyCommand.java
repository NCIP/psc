package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.tools.FormatTools;
import edu.nwu.bioinformatics.commons.spring.Validatable;
import org.springframework.validation.Errors;

import java.text.ParseException;
import java.util.Date;

public class SubjectOffStudyCommand implements Validatable {
    private StudySubjectAssignment assignment;
    private String expectedEndDate;
    private SubjectService subjectService;

    public void validate(Errors errors) {
        if (getExpectedEndDate() == null || getExpectedEndDate().length()==0) {
            errors.rejectValue("expectedEndDate", "error.expected.end.date.is.not.selected");
        } else if (convertStringToDate(getExpectedEndDate()) == null) {
            errors.rejectValue("expectedEndDate", "error.expected.end.date.is.not.correct");
        }
    }

    public Date convertStringToDate(String dateString) {
        Date convertedDate;
        try {
            convertedDate = FormatTools.getLocal().getDateFormat().parse(dateString);
        } catch (ParseException e) {
            convertedDate = null;

        }
        return convertedDate;
    }

    public StudySubjectAssignment getAssignment() {
        return assignment;
    }

    public void setAssignment(StudySubjectAssignment assignment) {
        this.assignment = assignment;
    }

    public String getExpectedEndDate() {
        return expectedEndDate;
    }

    public void setExpectedEndDate(String expectedEndDate) {
        this.expectedEndDate = expectedEndDate;
    }

    public StudySubjectAssignment takeSubjectOffStudy() {
        Date date =   convertStringToDate(getExpectedEndDate());
        return subjectService.takeSubjectOffStudy(assignment, date);
    }

    public void setSubjectService(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

}
