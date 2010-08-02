package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.UserRole;
import edu.northwestern.bioinformatics.studycalendar.service.dataproviders.StudyConsumer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import static java.util.Arrays.*;
import static org.easymock.EasyMock.*;

/**
 * @author Rhett Sutphin
 */
public class AuthorizationServiceTest extends StudyCalendarTestCase {
    private AuthorizationService service;

    private StudyConsumer studyConsumer;

    private User user;
    private Study studyA, studyB, studyAB;
    private Site siteA, siteB;
    private List<Study> allStudies;

    @Override
    @SuppressWarnings({ "unchecked" })
    protected void setUp() throws Exception {
        super.setUp();
        studyConsumer = registerMockFor(StudyConsumer.class);
        expect(studyConsumer.refresh((List<Study>) notNull())).andStubReturn(null);

        service = new AuthorizationService();
        service.setStudyConsumer(studyConsumer);

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
        allStudies = Arrays.asList(studyA, studyB, studyAB);
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
        addRole(Role.SUBJECT_COORDINATOR).addStudySite(studyAB.getStudySite(siteB));

        List<StudySubjectAssignment> filtered = service.filterAssignmentsForVisibility(Arrays.asList(ssa1, ssa2), user);
        assertEquals("Wrong number of assignments in result", 1, filtered.size());
        assertEquals("Wrong assignment in result", ssa2, filtered.get(0));
    }

    public void testVisibilityToRoleWithRole() throws Exception {
        // Other roles, etc., are tested under isTemplateVisible
        UserRole siteCoordinatorRole = Fixtures.createUserRole(user, Role.SITE_COORDINATOR, siteB);

        replayMocks();
        List<Study> actualStudyTemplates = service.filterStudiesForVisibility(asList(studyA, studyB), siteCoordinatorRole);
        verifyMocks();
        assertEquals("Wrong number of studies returned", 1, actualStudyTemplates.size());
        assertSame("Wrong study returned", studyB, actualStudyTemplates.get(0));
    }

    public void testFilterForVizReturnsNothingWithNullRole() throws Exception {
        replayMocks();
        List<Study> actual = service.filterStudiesForVisibility(asList(studyA, studyB), (UserRole) null);
        verifyMocks();

        assertEquals(0, actual.size());
    }

    public void testFilterForVisibilityOnAnEmptyListReturnsAnEmptyList() throws Exception {
        assertTrue(service.filterAssignmentsForVisibility(Collections.<StudySubjectAssignment>emptyList(), user).isEmpty());
    }

    public void testStudyVisibilityForUserRefreshesFirst() throws Exception {
        expect(studyConsumer.refresh(allStudies)).andReturn(allStudies);
        replayMocks();

        service.filterStudiesForVisibility(allStudies, user);
        verifyMocks();
    }

    public void testStudyVisibilityForUserRoleRefreshesFirst() throws Exception {
        expect(studyConsumer.refresh(allStudies)).andReturn(allStudies);
        replayMocks();

        service.filterStudiesForVisibility(allStudies, createUserRole(user, Role.SYSTEM_ADMINISTRATOR));
        verifyMocks();
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
