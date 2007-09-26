package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.domain.Participant;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.StudyParticipantAssignment;
import edu.northwestern.bioinformatics.studycalendar.service.ParticipantService;

import java.util.Date;


/**
 * @author Padmaja Vedula
 */
public class AssignParticipantCommand {
    private StudySite studySite;
    private Participant participant;
    private Arm arm;
    private Date startDate;

    private ParticipantService participantService;

    public StudyParticipantAssignment assignParticipant() {
        return participantService.assignParticipant(
            getParticipant(), getStudySite(), getEffectiveArm(), getStartDate());
    }

    private Arm getEffectiveArm() {
        Arm effectiveArm = getArm();
        if (effectiveArm == null) {
            effectiveArm = getStudySite().getStudy().getPlannedCalendar().getEpochs().get(0).getArms().get(0);
        }
        return effectiveArm;
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

    public StudySite getStudySite() {
        return studySite;
    }

    public void setStudySite(StudySite studySite) {
        this.studySite = studySite;
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
}
