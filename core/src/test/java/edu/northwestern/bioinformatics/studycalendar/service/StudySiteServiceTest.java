package edu.northwestern.bioinformatics.studycalendar.service;

import static edu.northwestern.bioinformatics.studycalendar.core.ServicedFixtures.createStudySite;
import static edu.northwestern.bioinformatics.studycalendar.core.ServicedFixtures.createUserRole;

import static java.util.Arrays.asList;

import static edu.northwestern.bioinformatics.studycalendar.core.ServicedFixtures.createNamedInstance;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.*;

import java.util.List;

public class StudySiteServiceTest extends StudyCalendarTestCase {
    private StudySiteService service;
    private User user;
    private Site site0, site1;
    private List<StudySite> studySites;
    private Study study0, study1, study2;
    private StudySite studySite0, studySite1, studySite2, studySite3;

    protected void setUp() throws Exception {
        super.setUp();

        service = new StudySiteService();

        site0 = createNamedInstance("Northwestern", Site.class);
        site1 = createNamedInstance("Mayo Clinic" , Site.class);

        study0 = createNamedInstance("Study A", Study.class);
        study1 = createNamedInstance("Study B", Study.class);
        study2 = createNamedInstance("Study C", Study.class);

        studySite0 = createStudySite(study0, site0);
        studySite1 = createStudySite(study1, site0);
        studySite2 = createStudySite(study2, site0);
        studySite3 = createStudySite(study2, site1);

        studySites = asList(studySite0, studySite1, studySite2, studySite3);

        user  = createNamedInstance("John", User.class);
        UserRole role = createUserRole(user, Role.SUBJECT_COORDINATOR, site0, site1);
        role.setStudySites(studySites);
        user.addUserRole(role);

    }

    public void testGetAllStudySitesForSubjectCoordinator() {
        List<StudySite> actualStudySites = service.getAllStudySitesForSubjectCoordinator(user);
        assertEquals("Wrong number of Study Sites", studySites.size(), actualStudySites.size());
        assertEquals("Wrong Study Site", studySite0, actualStudySites.get(0));
        assertEquals("Wrong Study Site", studySite1, actualStudySites.get(1));
        assertEquals("Wrong Study Site", studySite2, actualStudySites.get(2));
        assertEquals("Wrong Study Site", studySite3, actualStudySites.get(3));
    }

    public void testGetStudySitesForSubjectCoordinatorFromSite() {
        List<StudySite> actualStudySites = service.getStudySitesForSubjectCoordinator(user, site1);
        assertEquals("Wrong number of Study Sites", 1, actualStudySites.size());
        assertEquals("Wrong Study Site", studySite3, actualStudySites.get(0));
    }

    public void testGetStudySitesForSubjectCoordinatorFromStudy() {
        List<StudySite> actualStudySites = service.getStudySitesForSubjectCoordinator(user, study2);
        assertEquals("Wrong number of Study Sites", 2, actualStudySites.size());
        assertEquals("Wrong Study Site", studySite2, actualStudySites.get(0));
        assertEquals("Wrong Study Site", studySite3, actualStudySites.get(1));
    }
}
