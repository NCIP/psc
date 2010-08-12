package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.PscUserBuilder;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.SecurityContextHolderTestHelper;
import edu.northwestern.bioinformatics.studycalendar.dao.reporting.ScheduledActivitiesReportFilters;
import edu.northwestern.bioinformatics.studycalendar.dao.reporting.ScheduledActivitiesReportRowDao;
import edu.northwestern.bioinformatics.studycalendar.domain.reporting.ScheduledActivitiesReportRow;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRoleUse;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;

import java.util.Arrays;
import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import static org.easymock.classextension.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
public class ReportServiceTest extends StudyCalendarTestCase {
    private ReportService service;

    private ScheduledActivitiesReportRowDao scheduledActivitiesReportRowDao;
    private PscUserService pscUserService;

    private ScheduledActivitiesReportFilters filters;
    private List<ScheduledActivitiesReportRow> someRows;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        scheduledActivitiesReportRowDao = registerMockFor(ScheduledActivitiesReportRowDao.class);
        pscUserService = registerMockFor(PscUserService.class);

        filters = new ScheduledActivitiesReportFilters();
        someRows = Arrays.asList(
            new ScheduledActivitiesReportRow(), new ScheduledActivitiesReportRow());

        service = new ReportService();
        service.setApplicationSecurityManager(applicationSecurityManager);
        service.setScheduledActivitiesReportRowDao(scheduledActivitiesReportRowDao);
        service.setPscUserService(pscUserService);
    }

    public void testExecuteScheduledActivityReportForUnlimitedUser() throws Exception {
        PscUser all = new PscUserBuilder().
            add(PscRole.STUDY_TEAM_ADMINISTRATOR).forAllSites().toUser();
        SecurityContextHolderTestHelper.setSecurityContext(all);
        expect(pscUserService.getVisibleStudySiteIds(all, PscRoleUse.SUBJECT_MANAGEMENT.roles())).
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
}
