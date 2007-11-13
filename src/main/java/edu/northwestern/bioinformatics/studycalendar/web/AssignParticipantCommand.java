package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.service.ParticipantService;

import java.util.Date;


/**
 * @author Padmaja Vedula
 */
public class AssignParticipantCommand {
    private Participant participant;
    private Arm arm;
    private Date startDate;
    private Site site;
    private Study study;
    private User participantCoordinator;

    private ParticipantService participantService;

    public StudyParticipantAssignment assignParticipant() {
        return participantService.assignParticipant(
            getParticipant(), getStudySite(), getEffectiveArm(), getStartDate(), getParticipantCoordinator());
    }

    private Arm getEffectiveArm() {
        Arm effectiveArm = getArm();
        if (effectiveArm == null) {
            effectiveArm = getStudySite().getStudy().getPlannedCalendar().getEpochs().get(0).getArms().get(0);
        }
        return effectiveArm;
    }

    private StudySite getStudySite() {
        return StudySite.findStudySite(getStudy(), getSite());
    }

    ////// CONFIGURATION

    public void setParticipantService(ParticipantService participantService) {
        this.participantService = participantService;
    }

    ////// BOUND PROPERTIES

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Participant getParticipant() {
        return participant;
    }

    public void setParticipant(Participant participant) {
        this.participant = participant;
    }

    public Arm getArm() {
        return arm;
    }

    public void setArm(Arm arm) {
        this.arm = arm;
    }

    public User getParticipantCoordinator() {
        return participantCoordinator;
    }

    public void setParticipantCoordinator(User participantCoordinator) {
        this.participantCoordinator = participantCoordinator;
    }

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        this.study = study;
    }
}
