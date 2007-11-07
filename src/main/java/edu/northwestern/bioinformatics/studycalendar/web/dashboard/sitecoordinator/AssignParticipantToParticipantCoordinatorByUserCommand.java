package edu.northwestern.bioinformatics.studycalendar.web.dashboard.sitecoordinator;

import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.Participant;
import edu.northwestern.bioinformatics.studycalendar.dao.ParticipantDao;

public class AssignParticipantToParticipantCoordinatorByUserCommand {
    private User selected;
    private ParticipantDao participantDao;

    public User getSelected() {
        return selected;
    }

    public void setSelected(User selected) {
        this.selected = selected;
    }

    public void assignParticipantsToParticipantCoordinator() {
        return;
    }

    public void assignParticipantToParticipantCoordinator(Participant participant, User participantCoordinator) {
        return;
    }

    public void setParticipantDao(ParticipantDao participantDao) {
        this.participantDao = participantDao;
    }
}
