package edu.northwestern.bioinformatics.studycalendar.service;

import static edu.northwestern.bioinformatics.studycalendar.domain.UserRole.findByRole;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.UserRole;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;

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
}
