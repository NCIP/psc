package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.ParticipantDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Participant;
import edu.northwestern.bioinformatics.studycalendar.domain.StudyParticipantAssignment;

import java.util.Date;


/**
 * @author Padmaja Vedula
 */

public class AssignParticipantCommand {
    private Integer studyId;
    private Integer studySiteId;
    private Integer participantId;
    private Date startDateEpoch;
    private ParticipantDao participantDao;
    private StudySiteDao studySiteDao;

    public Participant assignParticipant() {
        Participant participant = participantDao.getById(getParticipantId());

        StudyParticipantAssignment studyParticipantAssignment = new StudyParticipantAssignment();
        studyParticipantAssignment.setStudySite(studySiteDao.getById(getStudySiteId()));
        studyParticipantAssignment.setParticipant(participant);
        studyParticipantAssignment.setStartDateEpoch(this.getStartDateEpoch());

        participant.addStudyParticipantAssignments(studyParticipantAssignment);

        return participant;
    }

    ////// CONFIGURATION

    public void setParticipantDao(ParticipantDao participantDao) {
        this.participantDao = participantDao;
    }

    public void setStudySiteDao(StudySiteDao studySiteDao) {
        this.studySiteDao = studySiteDao;
    }

    ////// BOUND PROPERTIES

    public Date getStartDateEpoch() {
        return startDateEpoch;
    }

    public void setStartDateEpoch(Date startDateEpoch) {
        this.startDateEpoch = startDateEpoch;
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

}
