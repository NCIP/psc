package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.domain.StudyParticipantAssignment;
import edu.northwestern.bioinformatics.studycalendar.service.ParticipantService;

import java.util.Date;

public class ParticipantOffStudyCommand {
    private StudyParticipantAssignment assignment;
    private Date expectedEndDate;
    private ParticipantService participantService;

    public StudyParticipantAssignment getAssignment() {
        return assignment;
    }

    public void setAssignment(StudyParticipantAssignment assignment) {
        this.assignment = assignment;
    }

    public Date getExpectedEndDate() {
        return expectedEndDate;
    }

    public void setExpectedEndDate(Date expectedEndDate) {
        this.expectedEndDate = expectedEndDate;
    }

    public StudyParticipantAssignment takeParticipantOffStudy() {
        return participantService.takeParticipantOffStudy(assignment, expectedEndDate);
    }

    public void setParticipantService(ParticipantService participantService) {
        this.participantService = participantService;
    }
}
