package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.ParticipantDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Participant;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudyParticipantAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;

import java.util.Date;

/**
 * @author Rhett Sutphin
 */
public class ParticipantService {
    private ParticipantDao participantDao;

    public void assignParticipant(Participant participant, StudySite study, Date startDate) {
        StudyParticipantAssignment spa = new StudyParticipantAssignment();
        spa.setParticipant(participant);
        spa.setStudySite(study);
        spa.setStartDateEpoch(startDate);
        participant.addStudyAssignment(spa);
        participantDao.save(participant);
    }

    ////// CONFIGURATION

    public void setParticipantDao(ParticipantDao participantDao) {
        this.participantDao = participantDao;
    }
}
