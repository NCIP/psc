package edu.northwestern.bioinformatics.studycalendar.web;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static org.easymock.EasyMock.expect;

import static java.util.Arrays.asList;
import java.util.List;
import java.util.Map;

/**
 * @author John Dzak
 */
public class SiteCoordinatorDashboardCommandByStudyTest extends StudyCalendarTestCase {
    private TemplateService templateService;
    private Study study0, study1;
    private User user0, user1;
    private Site site0, site1;
    private UserRole role0, role1;
    private List<Study> assignableStudies;
    private List<Site> assignableSites;
    private List<User> assignableUsers;


    protected void setUp() throws Exception {
        super.setUp();

        templateService = registerMockFor(TemplateService.class);

        study0 = createNamedInstance("Study A", Study.class);
        study1 = createNamedInstance("Study B", Study.class);
        assignableStudies = asList(study0, study1);

        site0  = createNamedInstance("Mayo Clinic" , Site.class);
        site1  = createNamedInstance("Northwestern", Site.class);
        assignableSites = asList(site0, site1);

        user0  = createNamedInstance("John", User.class);
        user1  = createNamedInstance("Jake", User.class);
        assignableUsers = asList(user0, user1);

        role0 = createUserRole(user0, Role.PARTICIPANT_COORDINATOR, site0, site1);
        role0.addStudySite(createStudySite(study0, site0));
        role0.addStudySite(createStudySite(study1, site0));


        role1 = createUserRole(user1, Role.PARTICIPANT_COORDINATOR, site0);
        user0.addUserRole(role0);
        user1.addUserRole(role1);
    }

    public void testBuildGrid() throws Exception {
        replayMocks();
        SiteCoordinatorDashboardCommandByStudy command = createCommand();
        verifyMocks();

        Map<User, Map<Site, SiteCoordinatorDashboardCommandByStudy.GridCell>> studyAssignmentGrid = command.getGrid();

        assertEquals("Wrong Size", 2, studyAssignmentGrid.keySet().size());
        assertEquals("Wrong Size", 2, studyAssignmentGrid.get(user0).keySet().size());
        assertEquals("Wrong Size", 2, studyAssignmentGrid.get(user1).keySet().size());

        assertTrue("No User", studyAssignmentGrid.containsKey(user0));
        assertTrue("No User", studyAssignmentGrid.containsKey(user1));

        assertTrue("No Site", studyAssignmentGrid.get(user0).containsKey(site0));
        assertTrue("No Site", studyAssignmentGrid.get(user1).containsKey(site0));

        assertTrue("Should be selected"     , studyAssignmentGrid.get(user0).get(site0).isSelected());
        assertFalse("Should not be selected", studyAssignmentGrid.get(user0).get(site1).isSelected());
        assertFalse("Should not be selected", studyAssignmentGrid.get(user1).get(site0).isSelected());
        assertFalse("Should not be selected", studyAssignmentGrid.get(user1).get(site1).isSelected());

        assertTrue("Site Access should be allowed"     , studyAssignmentGrid.get(user0).get(site0).isSiteAccessAllowed());
        assertTrue("Site Access should be allowed"     , studyAssignmentGrid.get(user0).get(site1).isSiteAccessAllowed());
        assertTrue("Site Access should be allowed"     , studyAssignmentGrid.get(user1).get(site0).isSiteAccessAllowed());
        assertFalse("Site Access should not be allowed", studyAssignmentGrid.get(user1).get(site1).isSiteAccessAllowed());
    }

    public void testApply() throws Exception {
        expectApply();

        replayMocks();

        SiteCoordinatorDashboardCommandByStudy command = createCommand();
        command.apply();
        verifyMocks();
    }

   /* public void testIsSelectedPos() throws Exception {
        replayMocks();
        
        boolean result = createCommand().isSiteSelected(role0, study0, site0);
        verifyMocks();

        assertTrue("Site should be selected", result);
    }

    public void testIsSelectedNeg() throws Exception {
        replayMocks();

        boolean result = createCommand().isSiteSelected(role1, study0, site0);
        verifyMocks();

        assertFalse("Site should be selected", result);
    }

    public void testIsSiteAccessAllowedPos() throws Exception {
        replayMocks();

        boolean result = createCommand().isSiteAccessAllowed(role0, site0);
        verifyMocks();

        assertTrue("Site access should be allowed", result);
    }

    public void testIsSiteAccessAllowedNeg() throws Exception {
        replayMocks();

        boolean result = createCommand().isSiteAccessAllowed(role0, site1);
        verifyMocks();

        assertTrue("Site access should not be allowed", result);
    }  */

    private void expectApply() throws Exception {
        expect(templateService.assignTemplateToParticipantCoordinator(study0, site0, user0)).andReturn(user0);
        expect(templateService.removeAssignedTemplateFromParticipantCoordinator(study0, site1, user0)).andReturn(user0);
        expect(templateService.removeAssignedTemplateFromParticipantCoordinator(study0, site0, user1)).andReturn(user0);
        expect(templateService.removeAssignedTemplateFromParticipantCoordinator(study0, site1, user1)).andReturn(user0);
    }

    private SiteCoordinatorDashboardCommandByStudy createCommand() {
        return new SiteCoordinatorDashboardCommandByStudy(templateService, study0, assignableStudies, assignableSites, assignableUsers);
    }
}
