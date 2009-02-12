package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.UserRole;
import edu.northwestern.bioinformatics.studycalendar.test.ServicedFixtures;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

import java.util.Arrays;
import java.util.List;
import java.util.Collections;

/**
 * @author Rhett Sutphin
 */
public class AuthorizationServiceTest extends StudyCalendarTestCase {
    private AuthorizationService service;
    private User user;
    private Study studyA, studyB, studyAB;
    private Site siteA, siteB;
    private List<Study> allStudies;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        service = new AuthorizationService();

        user = ServicedFixtures.createUser("jimbo");

        studyA = ServicedFixtures.createNamedInstance("A", Study.class);
        studyB = ServicedFixtures.createNamedInstance("B", Study.class);
        studyAB = ServicedFixtures.createNamedInstance("AB", Study.class);
        siteA = ServicedFixtures.createNamedInstance("a", Site.class);
        siteB = ServicedFixtures.createNamedInstance("b", Site.class);
        studyA.addSite(siteA);
        studyB.addSite(siteB);
        studyAB.addSite(siteA);
        studyAB.addSite(siteB);
        allStudies = Arrays.asList(studyA, studyB, studyAB);
    }

    public void testStudyVisibilityForSubjectCoordinator() throws Exception {
        UserRole coord = ServicedFixtures.createUserRole(user, Role.SUBJECT_COORDINATOR, siteB);
        coord.addStudySite(studyAB.getStudySite(siteB));
        assertFalse(service.isTemplateVisible(coord, studyA));
        assertFalse(service.isTemplateVisible(coord, studyB));
        assertTrue(service.isTemplateVisible(coord, studyAB));
    }

    public void testStudyVisibilityForSiteCoordinator() throws Exception {
        UserRole coord = ServicedFixtures.createUserRole(user, Role.SITE_COORDINATOR, siteA);
        assertTrue(service.isTemplateVisible(coord, studyA));
        assertFalse(service.isTemplateVisible(coord, studyB));
        assertTrue(service.isTemplateVisible(coord, studyAB));
    }

    public void testStudyVisibilityForSystemAdministrator() throws Exception {
        UserRole admin = ServicedFixtures.createUserRole(user, Role.SYSTEM_ADMINISTRATOR, siteA);
        assertFalse(service.isTemplateVisible(admin, studyA));
        assertFalse(service.isTemplateVisible(admin, studyB));
        assertFalse(service.isTemplateVisible(admin, studyAB));
    }

    public void testStudyVisibilityForStudyAdministrator() throws Exception {
        UserRole admin = ServicedFixtures.createUserRole(user, Role.STUDY_ADMIN);
        assertTrue(service.isTemplateVisible(admin, studyA));
        assertTrue(service.isTemplateVisible(admin, studyB));
        assertTrue(service.isTemplateVisible(admin, studyAB));
    }

    public void testStudyVisibilityForStudyCoordinator() throws Exception {
        UserRole coord = ServicedFixtures.createUserRole(user, Role.STUDY_COORDINATOR);
        assertTrue(service.isTemplateVisible(coord, studyA));
        assertTrue(service.isTemplateVisible(coord, studyB));
        assertTrue(service.isTemplateVisible(coord, studyAB));
    }
    
    public void testFilterForVisibilityOnAnEmptyListReturnsAnEmptyList() throws Exception {
        assertTrue(service.filterAssignmentsForVisibility(Collections.<StudySubjectAssignment>emptyList(), user).isEmpty());
    }

    public void testFilterAssignmentsForVisibility() throws Exception {
        StudySubjectAssignment ssa1 = ServicedFixtures.createAssignment(studyAB.getStudySite(siteA), null);
        StudySubjectAssignment ssa2 = ServicedFixtures.createAssignment(studyAB.getStudySite(siteB), null);
        addRole(Role.SUBJECT_COORDINATOR).addStudySite(studyAB.getStudySite(siteB));

        List<StudySubjectAssignment> filtered = service.filterAssignmentsForVisibility(Arrays.asList(ssa1, ssa2), user);
        assertEquals("Wrong number of assignments in result", 1, filtered.size());
        assertEquals("Wrong assignment in result", ssa2, filtered.get(0));
    }

    public void testStudyVisibilityWhenSubjectCoord() throws Exception {
        UserRole subjC = addRole(Role.SUBJECT_COORDINATOR);
        subjC.addStudySite(studyA.getStudySite(siteA));
        subjC.addStudySite(studyAB.getStudySite(siteB));
        List<Study> filtered = service.filterStudiesForVisibility(allStudies, user);

        assertEquals("Only A and AB should be present", Arrays.asList(studyA, studyAB), filtered);
    }

    public void testStudyVisibilityWhenSiteCoord() throws Exception {
        addRole(Role.SITE_COORDINATOR).addSite(siteB);
        List<Study> filtered = service.filterStudiesForVisibility(allStudies, user);

        assertEquals("Only B and AB should be present", Arrays.asList(studyB, studyAB), filtered);
    }

    public void testAllStudiesVisibleWhenStudyAdmin() throws Exception {
        addRole(Role.STUDY_ADMIN);
        List<Study> filtered = service.filterStudiesForVisibility(allStudies, user);

        assertEquals("All studies should be present", allStudies, filtered);
    }

    public void testAllStudiesVisibleWhenStudyCoord() throws Exception {
        addRole(Role.STUDY_COORDINATOR);
        List<Study> filtered = service.filterStudiesForVisibility(allStudies, user);

        assertEquals("All studies should be present", allStudies, filtered);
    }

    public void testStudyVisibilityWhenBothStudyCoordAndSubjectCoord() throws Exception {
        addRole(Role.STUDY_COORDINATOR);
        addRole(Role.SUBJECT_COORDINATOR).addStudySite(studyAB.getStudySite(siteB));
        List<Study> filtered = service.filterStudiesForVisibility(allStudies, user);

        assertEquals("All studies should be present exactly once", allStudies, filtered);
    }

    private UserRole addRole(Role role) {
        user.addUserRole(new UserRole(user, role));
        return user.getUserRole(role);
    }
}
