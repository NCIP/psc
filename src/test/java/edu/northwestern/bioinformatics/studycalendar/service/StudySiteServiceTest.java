package edu.northwestern.bioinformatics.studycalendar.service;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createStudySite;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createUserRole;

import static java.util.Arrays.asList;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setId;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.*;

import java.util.List;

public class StudySiteServiceTest extends StudyCalendarTestCase {
    private StudySiteService service;
    private List<StudySite> studySites;
    private User user;

    protected void setUp() throws Exception {
        super.setUp();

        service = new StudySiteService();

        Site site = createNamedInstance("Northwestern", Site.class);

        Study study0 = setId(0, createNamedInstance("Study A", Study.class));
        Study study1 = setId(1, createNamedInstance("Study B", Study.class));

        StudySite studySite0 = createStudySite(study0, site);
        StudySite studySite1 = createStudySite(study1, site);
        studySites           = asList(studySite0, studySite1);

        user          = createNamedInstance("John", User.class);
        UserRole role = createUserRole(user, Role.PARTICIPANT_COORDINATOR, site);
        role.setStudySites(studySites);
        user.addUserRole(role);

    }

    public void testGetStudySitesForParticipantCoordinator() {
        List<StudySite> actualStudySites = service.getAllStudySitesForParticipantCoordinator(user);
        assertEquals("Wrong number of Study Sites", studySites.size(), actualStudySites.size());
        assertEquals("Wrong Study Site", studySites.get(0), actualStudySites.get(0));
        assertEquals("Wrong Study Site", studySites.get(1), actualStudySites.get(1));
    }
}
