package edu.northwestern.bioinformatics.studycalendar.web.dashboard.sitecoordinator;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.web.dashboard.sitecoordinator.AssignParticipantCoordinatorCommandByUser;
import static org.easymock.EasyMock.expect;

/**
 * @author John Dzak
 */
public class AssignParticipantCoordinatorCommandByUserTest extends StudyCalendarTestCase {
    private TemplateService templateService;
    private User user;
    private Study study0, study1;
    private Site site0, site1;
    private AssignParticipantCoordinatorCommandByUser command;

    protected void setUp() throws Exception {
        super.setUp();

        user = createNamedInstance("John", User.class);

        templateService = registerMockFor(TemplateService.class);

        command = new AssignParticipantCoordinatorCommandByUser(templateService, user, null, null, null);

        study0 = createNamedInstance("Study A", Study.class);
        study1 = createNamedInstance("Study B", Study.class);

        site0  = createNamedInstance("Mayo Clinic" , Site.class);
        site1  = createNamedInstance("Northwestern", Site.class);

        createStudySite(study0, site1);

        UserRole role0 = createUserRole(user, Role.PARTICIPANT_COORDINATOR, site0, site1);
        role0.addStudySite(createStudySite(study0, site0));
        user.addUserRole(role0);
    }

    public void testIsSiteSelectedPos() {
        boolean actualResult = command.isSiteSelected(study0, site0);
        assertTrue("StudySite should be selected", actualResult);
    }

    public void testIsSiteSelectedNeg() {
        boolean actualResult = command.isSiteSelected(study1, site0);
        assertFalse("StudySite should not be selected", actualResult);
    }

    public void testIsSiteAccessAllowedPos() {
        boolean actualResult = command.isSiteAccessAllowed(study0, site1);
        assertTrue("User site access shold be allowed", actualResult);
    }

    public void testIsSiteAccessAllowedNeg() {
        boolean actualResult = command.isSiteAccessAllowed(study1, site1);
        assertFalse("User site access shold not be allowed", actualResult);
    }

    public void testPerformCheckAction() throws Exception {
        expect(templateService.assignTemplateToParticipantCoordinator(study0, site0, user)).andReturn(user);
        replayMocks();
        command.performCheckAction(study0, site0);
        verifyMocks();
    }

    public void testPerformUncheckAction() throws Exception {
        expect(templateService.removeAssignedTemplateFromParticipantCoordinator(study0, site0, user)).andReturn(user);
        replayMocks();
        command.performUncheckAction(study0, site0);
        verifyMocks();
    }
}
