package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.domain.*;

import java.util.List;
import java.util.ArrayList;

public class StudySiteService {
    public List<StudySite> getAllStudySitesForSubjectCoordinator(User user) {
        List<StudySite> studySites = new ArrayList<StudySite>();
        if (user != null) {
            UserRole userRole = user.getUserRole(Role.SUBJECT_COORDINATOR);
            if (userRole != null) {
                studySites.addAll(userRole.getStudySites());
            }
        }
        return studySites;
    }

    public List<StudySite> getStudySitesForSubjectCoordinator(User user, Site site) {
        List<StudySite> allStudySites = getAllStudySitesForSubjectCoordinator(user);
        List<StudySite> availableStudySites = new ArrayList<StudySite>();

        for (StudySite studySite : allStudySites) {
            if (studySite.getSite().equals(site)) {
                availableStudySites.add(studySite);
            }
        }
        return availableStudySites;
    }

    public List<StudySite> getStudySitesForSubjectCoordinator(User user, Study study) {
        List<StudySite> allStudySites = getAllStudySitesForSubjectCoordinator(user);
        List<StudySite> availableStudySites = new ArrayList<StudySite>();

        for (StudySite studySite : allStudySites) {
            if (studySite.getStudy().equals(study)) {
                availableStudySites.add(studySite);
            }
        }
        return availableStudySites;
    }
}
