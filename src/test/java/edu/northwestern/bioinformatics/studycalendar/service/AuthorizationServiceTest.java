package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.UserRole;
import edu.northwestern.bioinformatics.studycalendar.test.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

import java.util.Arrays;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class AuthorizationServiceTest extends StudyCalendarTestCase {
    private AuthorizationService service;
    private User user;
    private Study studyA, studyB, studyAB;
    private Site siteA, siteB;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        service = new AuthorizationService();

        user = Fixtures.createUser("jimbo");

        studyA = Fixtures.createNamedInstance("A", Study.class);
        studyB = Fixtures.createNamedInstance("B", Study.class);
        studyAB = Fixtures.createNamedInstance("AB", Study.class);
        siteA = Fixtures.createNamedInstance("a", Site.class);
        siteB = Fixtures.createNamedInstance("b", Site.class);
        studyA.addSite(siteA);
        studyB.addSite(siteB);
        studyAB.addSite(siteA);
        studyAB.addSite(siteB);
    }

    public void testStudyVisibilityForSubjectCoordinator() throws Exception {
        UserRole coord = Fixtures.createUserRole(user, Role.SUBJECT_COORDINATOR, siteB);
        coord.addStudySite(studyAB.getStudySite(siteB));
        assertFalse(service.isTemplateVisible(coord, studyA));
        assertFalse(service.isTemplateVisible(coord, studyB));
        assertTrue(service.isTemplateVisible(coord, studyAB));
    }

    public void testStudyVisibilityForSiteCoordinator() throws Exception {
        UserRole coord = Fixtures.createUserRole(user, Role.SITE_COORDINATOR, siteA);
        assertTrue(service.isTemplateVisible(coord, studyA));
        assertFalse(service.isTemplateVisible(coord, studyB));
        assertTrue(service.isTemplateVisible(coord, studyAB));
    }

    public void testStudyVisibilityForSystemAdministrator() throws Exception {
        UserRole admin = Fixtures.createUserRole(user, Role.SYSTEM_ADMINISTRATOR, siteA);
        assertFalse(service.isTemplateVisible(admin, studyA));
        assertFalse(service.isTemplateVisible(admin, studyB));
        assertFalse(service.isTemplateVisible(admin, studyAB));
    }

    public void testStudyVisibilityForStudyAdministrator() throws Exception {
        UserRole admin = Fixtures.createUserRole(user, Role.STUDY_ADMIN);
        assertTrue(service.isTemplateVisible(admin, studyA));
        assertTrue(service.isTemplateVisible(admin, studyB));
        assertTrue(service.isTemplateVisible(admin, studyAB));
    }

    public void testStudyVisibilityForStudyCoordinator() throws Exception {
        UserRole coord = Fixtures.createUserRole(user, Role.STUDY_COORDINATOR);
        assertTrue(service.isTemplateVisible(coord, studyA));
        assertTrue(service.isTemplateVisible(coord, studyB));
        assertTrue(service.isTemplateVisible(coord, studyAB));
    }
    
    public void testFilterAssignmentsForVisibility() throws Exception {
        StudySubjectAssignment ssa1 = Fixtures.createAssignment(studyAB.getStudySite(siteA), null);
        StudySubjectAssignment ssa2 = Fixtures.createAssignment(studyAB.getStudySite(siteB), null);
        user.addUserRole(new UserRole(user, Role.SUBJECT_COORDINATOR));
        user.getUserRole(Role.SUBJECT_COORDINATOR).addStudySite(studyAB.getStudySite(siteB));

        List<StudySubjectAssignment> filtered = service.filterForVisibility(Arrays.asList(ssa1, ssa2), user);
        assertEquals("Wrong number of assignments in result", 1, filtered.size());
        assertEquals("Wrong assignment in result", ssa2, filtered.get(0));
    }
}
