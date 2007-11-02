package edu.northwestern.bioinformatics.studycalendar.web.dashboard.sitecoordinator;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.web.dashboard.sitecoordinator.AssignParticipantCoordinatorByStudyCommand;
import static org.easymock.EasyMock.expect;

/**
 * @author John Dzak
 */
public class AssignParticipantCoordinatorByStudyCommandTest extends StudyCalendarTestCase {
    private TemplateService templateService;
    private Study study;
    private User user0, user1;
    private Site site0, site1;
    private AssignParticipantCoordinatorByStudyCommand command;

    protected void setUp() throws Exception {
        super.setUp();

        study = createNamedInstance("Study A", Study.class);

        templateService = registerMockFor(TemplateService.class);

        command = new AssignParticipantCoordinatorByStudyCommand(templateService, study, null, null, null);

        site0  = createNamedInstance("Mayo Clinic" , Site.class);
        site1  = createNamedInstance("Northwestern", Site.class);

        user0  = createNamedInstance("John", User.class);
        user1  = createNamedInstance("Jake", User.class);

        UserRole role0 = createUserRole(user0, Role.PARTICIPANT_COORDINATOR, site0, site1);
        role0.addStudySite(createStudySite(study, site0));
        user0.addUserRole(role0);

        UserRole role1 = createUserRole(user1, Role.PARTICIPANT_COORDINATOR, site0);
        user1.addUserRole(role1);
    }

    public void testIsSiteSelectedPos() {
        boolean actualResult = command.isSiteSelected(user0, site0);
        assertTrue("User should be selected", actualResult);
    }

    public void testIsSiteSelectedNeg() {
        boolean actualResult = command.isSiteSelected(user1, site0);
        assertFalse("User should not be selected", actualResult);
    }

    public void testIsSiteAccessAllowedPos() {
        boolean actualResult = command.isSiteAccessAllowed(user0, site1);
        assertTrue("User site access shold be allowed", actualResult);
    }

    public void testIsSiteAccessAllowedNeg() {
        boolean actualResult = command.isSiteAccessAllowed(user1, site1);
        assertFalse("User site access shold not be allowed", actualResult);
    }

    public void testPerformCheckAction() throws Exception {
        expect(templateService.assignTemplateToParticipantCoordinator(study, site0, user0)).andReturn(user0);
        replayMocks();
        command.performCheckAction(user0, site0);
        verifyMocks();
    }

    public void testPerformUncheckAction() throws Exception {
        expect(templateService.removeAssignedTemplateFromParticipantCoordinator(study, site0, user0)).andReturn(user0);
        replayMocks();
        command.performUncheckAction(user0, site0);
        verifyMocks();
    }
}
