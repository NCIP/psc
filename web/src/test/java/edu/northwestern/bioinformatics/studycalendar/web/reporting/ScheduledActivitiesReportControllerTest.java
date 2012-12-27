/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.reporting;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.SecurityContextHolderTestHelper;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.PscUserService;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory.createPscUser;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;
import static org.easymock.EasyMock.expect;

/**
 * @author John Dzak
 */
public class ScheduledActivitiesReportControllerTest extends ControllerTestCase {
    private ScheduledActivitiesReportController controller;

    private List<ActivityType> activityTypes;
    private List<PscUser> sscms;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        activityTypes = Arrays.asList(Fixtures.createActivityType("Exercise"));
        ActivityTypeDao activityTypeDao = registerDaoMockFor(ActivityTypeDao.class);
        expect(activityTypeDao.getAll()).andStubReturn(activityTypes);

        sscms = Arrays.asList(createPscUser("alice"), createPscUser("betsy"));
        PscUser user = createPscUser("jo", PscRole.DATA_READER);
        PscUserService pscUserService = registerMockFor(PscUserService.class);
        expect(pscUserService.getColleaguesOf(
            user, PscRole.STUDY_SUBJECT_CALENDAR_MANAGER,
            ScheduledActivitiesReportController.REPORT_AUTHORIZED_ROLES)
        ).andStubReturn(sscms);
        SecurityContextHolderTestHelper.setSecurityContext(user);

        controller = new ScheduledActivitiesReportController();
        controller.setControllerTools(controllerTools);
        controller.setActivityTypeDao(activityTypeDao);
        controller.setApplicationSecurityManager(applicationSecurityManager);
        controller.setPscUserService(pscUserService);
    }

    public void testAuthorizedRoles() {
        Collection<ResourceAuthorization> actualAuthorizations = controller.authorizations(null, null);
        assertRolesAllowed(actualAuthorizations,
            DATA_READER,
            STUDY_SUBJECT_CALENDAR_MANAGER,
            STUDY_TEAM_ADMINISTRATOR);
    }

    public void testView() throws Exception {
        assertEquals("Wrong view",
            "reporting/scheduledActivitiesReport", handleRequest().getViewName());
    }

    @SuppressWarnings({"unchecked"})
    public void testModelIncludesModes() throws Exception {
        Map<String, Object> model = handleRequest().getModel();
        assertEquals("Model should contain modes",
            ScheduledActivityMode.values(), model.get("modes"));
    }

    @SuppressWarnings({"unchecked"})
    public void testModelIncludesActivityTypes() throws Exception {
        Map<String, Object> model = handleRequest().getModel();
        assertEquals("Model should contain activity types",
            activityTypes, model.get("types"));
    }

    @SuppressWarnings({"unchecked"})
    public void testModelIncludesPotentialResponsibleUsers() throws Exception {
        Map<String, Object> model = handleRequest().getModel();
        assertEquals("Model should contain potential responsible users",
            sscms, model.get("potentialResponsibleUsers"));
    }

    ////// HELPERS

    private ModelAndView handleRequest() throws Exception {
        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();
        return mv;
    }
}
