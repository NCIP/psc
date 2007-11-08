package edu.northwestern.bioinformatics.studycalendar.web.dashboard.sitecoordinator;

import static edu.northwestern.bioinformatics.studycalendar.domain.StudySite.findStudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.dao.ParticipantDao;

import java.util.List;
import java.util.ArrayList;

public class AssignParticipantToParticipantCoordinatorByUserCommand {
    private ParticipantDao participantDao;
    private List<Participant> participants = new ArrayList<Participant>();
    private User participantCoordinator;
    private Study study;
    private Site site;
    private User selected;

    public void assignParticipantsToParticipantCoordinator() {
        StudySite studySite = findStudySite(study, site);
        for (Participant participant : participants) {
            List<StudyParticipantAssignment> assignments = participant.getAssignments();
            for (StudyParticipantAssignment assignment : assignments) {
                if (studySite.equals(assignment.getStudySite())) {
                    assignment.setParticipantCoordinator(participantCoordinator);
                    participantDao.save(participant);
                }
            }
        }
    }

    public void setParticipantDao(ParticipantDao participantDao) {
        this.participantDao = participantDao;
    }

    public void setParticipants(List<Participant> participants) {
        this.participants = participants;
    }

    public void setParticipantCoordinator(User participantCoordinator) {
        this.participantCoordinator = participantCoordinator;
    }

    public void setStudy(Study study) {
        this.study = study;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    public void setSelected(User selected) {
        this.selected = selected;
    }

    public List<Participant> getParticipants() {
        return participants;
    }

    public User getParticipantCoordinator() {
        return participantCoordinator;
    }

    public Study getStudy() {
        return study;
    }

    public Site getSite() {
        return site;
    }

    public User getSelected() {
        return selected;
    }
}
