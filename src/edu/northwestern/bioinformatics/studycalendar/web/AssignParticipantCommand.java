package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.ParticipantDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySiteDao;
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
    private Integer studyId;
    private Integer studySiteId;
    private Integer participantId;
    private Arm arm;
    private Date startDate;

    private ParticipantDao participantDao;
    private StudySiteDao studySiteDao;
    private ParticipantService participantService;

    public StudySite getStudySite() {
        return studySiteDao.getById(getStudySiteId());
    }

    public Participant getParticipant() {
        return participantDao.getById(getParticipantId());
    }

    public StudyParticipantAssignment assignParticipant() {
        return participantService.assignParticipant(
            getParticipant(), getStudySite(), getArm(), getStartDate());
    }

    ////// CONFIGURATION

    public void setParticipantDao(ParticipantDao participantDao) {
        this.participantDao = participantDao;
    }

    public void setStudySiteDao(StudySiteDao studySiteDao) {
        this.studySiteDao = studySiteDao;
    }

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

    public Integer getParticipantId() {
        return participantId;
    }

    public void setParticipantId(Integer participantId) {
        this.participantId = participantId;
    }

    public Integer getStudyId() {
        return studyId;
    }

    public void setStudyId(Integer studyId) {
        this.studyId = studyId;
    }

    public Integer getStudySiteId() {
        return studySiteId;
    }

    public void setStudySiteId(Integer studySiteId) {
        this.studySiteId = studySiteId;
    }

    public Arm getArm() {
        return arm;
    }

    public void setArm(Arm arm) {
        this.arm = arm;
    }
}
