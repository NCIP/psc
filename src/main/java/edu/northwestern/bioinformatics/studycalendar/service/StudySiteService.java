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
                studySites.addAll(userRole.getStudySites());
            }
        }
        return studySites;
    }

    public List<StudySite> getStudySitesForParticipantCoordinator(User user, Site site) {
        List<StudySite> allStudySites = getAllStudySitesForParticipantCoordinator(user);
        List<StudySite> availableStudySites = new ArrayList<StudySite>();

        for (StudySite studySite : allStudySites) {
            if (studySite.getSite().equals(site)) {
                availableStudySites.add(studySite);
            }
        }
        return availableStudySites;
    }

    public List<StudySite> getStudySitesForParticipantCoordinator(User user, Study study) {
        List<StudySite> allStudySites = getAllStudySitesForParticipantCoordinator(user);
        List<StudySite> availableStudySites = new ArrayList<StudySite>();

        for (StudySite studySite : allStudySites) {
            if (studySite.getStudy().equals(study)) {
                availableStudySites.add(studySite);
            }
        }
        return availableStudySites;
    }
}
