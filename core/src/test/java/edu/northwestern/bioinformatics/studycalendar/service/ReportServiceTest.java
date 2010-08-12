package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.PscUserBuilder;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.SecurityContextHolderTestHelper;
import edu.northwestern.bioinformatics.studycalendar.dao.reporting.ScheduledActivitiesReportFilters;
import edu.northwestern.bioinformatics.studycalendar.dao.reporting.ScheduledActivitiesReportRowDao;
import edu.northwestern.bioinformatics.studycalendar.domain.reporting.ScheduledActivitiesReportRow;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRoleUse;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import gov.nih.nci.security.AuthorizationManager;
import gov.nih.nci.security.authorization.domainobjects.User;
import gov.nih.nci.security.exceptions.CSObjectNotFoundException;

import java.util.Arrays;
import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import static org.easymock.classextension.EasyMock.*;

/**
 * @author Rhett Sutphin
 */
public class ReportServiceTest extends StudyCalendarTestCase {
    private ReportService service;

    private ScheduledActivitiesReportRowDao scheduledActivitiesReportRowDao;
    private PscUserService pscUserService;
    private AuthorizationManager csmAuthorizationManager;

    private ScheduledActivitiesReportFilters filters;
    private List<ScheduledActivitiesReportRow> someRows;
    private PscUser user;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        scheduledActivitiesReportRowDao = registerMockFor(ScheduledActivitiesReportRowDao.class);
        pscUserService = registerMockFor(PscUserService.class);
        csmAuthorizationManager = registerMockFor(AuthorizationManager.class);

        filters = new ScheduledActivitiesReportFilters();
        someRows = Arrays.asList(
            new ScheduledActivitiesReportRow(), new ScheduledActivitiesReportRow());
        user = new PscUserBuilder().
            add(PscRole.STUDY_TEAM_ADMINISTRATOR).forAllSites().toUser();
        SecurityContextHolderTestHelper.setSecurityContext(user);

        expect(csmAuthorizationManager.getUserById((String) notNull())).
            andStubReturn(user.getCsmUser());
        expect(pscUserService.getVisibleStudySiteIds(user, PscRoleUse.SUBJECT_MANAGEMENT.roles())).
            andStubReturn(null);

        service = new ReportService();
        service.setApplicationSecurityManager(applicationSecurityManager);
        service.setScheduledActivitiesReportRowDao(scheduledActivitiesReportRowDao);
        service.setPscUserService(pscUserService);
        service.setCsmAuthorizationManager(csmAuthorizationManager);
    }

    public void testExecuteScheduledActivityReportForUnlimitedUser() throws Exception {
        expect(pscUserService.getVisibleStudySiteIds(user, PscRoleUse.SUBJECT_MANAGEMENT.roles())).
            andReturn(null);
        expect(scheduledActivitiesReportRowDao.search(filters)).andReturn(someRows);

        replayMocks();
        List<ScheduledActivitiesReportRow> actual = service.searchScheduledActivities(filters);
        verifyMocks();

        assertSame("Wrong results", actual, someRows);
        assertNull("User should be unlimited", filters.getAuthorizedStudySiteIds());
    }

    public void testExecuteScheduledActivityReportForLimitedUser() throws Exception {
        List<Integer> expectedStudySiteIds = Arrays.asList(45, 72);
        PscUser some = new PscUserBuilder().
            add(PscRole.DATA_READER).forSites(createSite("A")).forStudies(createBasicTemplate("F")).
            toUser();
        SecurityContextHolderTestHelper.setSecurityContext(some);
        expect(pscUserService.getVisibleStudySiteIds(some, PscRoleUse.SUBJECT_MANAGEMENT.roles())).
            andReturn(expectedStudySiteIds);
        expect(scheduledActivitiesReportRowDao.search(filters)).andReturn(someRows);

        replayMocks();
        List<ScheduledActivitiesReportRow> actual = service.searchScheduledActivities(filters);
        verifyMocks();

        assertSame("Wrong results", actual, someRows);
        assertEquals("User should only be able to see certain study sites",
            expectedStudySiteIds,
            filters.getAuthorizedStudySiteIds());
    }

    public void testScheduledActivityReportLooksUpCsmUsers() throws Exception {
        List<ScheduledActivitiesReportRow> results = Arrays.asList(
            createRowWithUserId(10L),
            createRowWithUserId(null),
            createRowWithUserId(10L),
            createRowWithUserId(27L),
            createRowWithUserId(12L)
        );
        User user10 = AuthorizationObjectFactory.createCsmUser(10L, "jo");
        User user12 = AuthorizationObjectFactory.createCsmUser(12L, "larry");

        expect(csmAuthorizationManager.getUserById("10")).andReturn(user10).once();
        expect(csmAuthorizationManager.getUserById("12")).andReturn(user12).once();
        expect(csmAuthorizationManager.getUserById("27")).
            andThrow(new CSObjectNotFoundException("Uh oh")).once();
        expect(scheduledActivitiesReportRowDao.search(filters)).andReturn(results);

        replayMocks();
        List<ScheduledActivitiesReportRow> actual = service.searchScheduledActivities(filters);
        verifyMocks();

        assertEquals("Wrong number of results: " + actual, 5, actual.size());
        assertSame("Wrong user for row 0", user10, actual.get(0).getResponsibleUser());
        assertNull("Wrong user for row 1", actual.get(1).getResponsibleUser());
        assertSame("Wrong user for row 2", user10, actual.get(2).getResponsibleUser());
        assertNull("Wrong user for row 3", actual.get(3).getResponsibleUser());
        assertSame("Wrong user for row 4", user12, actual.get(4).getResponsibleUser());
    }

    private ScheduledActivitiesReportRow createRowWithUserId(Long id) {
        ScheduledActivitiesReportRow row = new ScheduledActivitiesReportRow();
        row.setResponsibleUserCsmUserId(id);
        return row;
    }
}
