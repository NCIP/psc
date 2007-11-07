package edu.northwestern.bioinformatics.studycalendar.service;

import static edu.northwestern.bioinformatics.studycalendar.domain.UserRole.findByRole;
import edu.northwestern.bioinformatics.studycalendar.domain.*;

import java.util.List;
import java.util.ArrayList;

public class StudySiteService {
    public List<StudySite> getAllStudySitesForParticipantCoordinator(User user) {
        List<StudySite> studySites = new ArrayList<StudySite>();
        if (user != null) {
            UserRole userRole = findByRole(user.getUserRoles(), Role.PARTICIPANT_COORDINATOR);
            if (userRole != null) {
                studySites = userRole.getStudySites();
            }
        }
        return studySites;
    }

    public List<StudySite> getStudySitesForParticipantCoordinator(User user, Site site) {
        List<StudySite> studySites = new ArrayList<StudySite>();
        if (user != null) {
            UserRole userRole = findByRole(user.getUserRoles(), Role.PARTICIPANT_COORDINATOR);
            if (userRole != null) {
                for (StudySite studySite : userRole.getStudySites()) {
                    if (studySite.getSite().equals(site)) {
                        studySites.add(studySite);
                    }
                }
            }
        }
        return studySites;
    }
}
