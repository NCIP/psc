/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.dashboard;

import edu.northwestern.bioinformatics.studycalendar.configuration.Configuration;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.PscUserBuilder;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.PscUserService;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.UserStudySiteRelationship;
import edu.northwestern.bioinformatics.studycalendar.web.WebTestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory.createPscUser;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;
import static org.easymock.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
public class DashboardCommandTest extends WebTestCase {
    private DashboardCommand command;

    private StudyDao studyDao;
    private PscUserService pscUserService;

    private Study eg, ie, etc;
    private Site nu;
    private PscUser jo, dash, zelda;
    private Configuration configuration;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        nu = createSite("NU", "IL036");
        Site vanderbilt = createSite("VU", "TN081");
        Site mayo = createSite("Mayo", "MN108");

        eg  = setId(1701, createBasicTemplate("EG 1701"));
        ie  = setId(3000, createBasicTemplate("IE 3000"));
        etc = setId(4401, createBasicTemplate("ETC 4401"));

        // all studies are available for assignment at NU
        createStudySite(eg, nu).approveAmendment(eg.getAmendment(), new Date());
        createStudySite(ie, nu).approveAmendment(ie.getAmendment(), new Date());
        createStudySite(etc, nu).approveAmendment(etc.getAmendment(), new Date());

        // all sites are particpating in etc
        createStudySite(etc, vanderbilt).approveAmendment(etc.getAmendment(), new Date());
        createStudySite(etc, mayo).approveAmendment(etc.getAmendment(), new Date());

        jo = new PscUserBuilder("jo").
            add(STUDY_SUBJECT_CALENDAR_MANAGER).forSites(nu, vanderbilt).forStudies(eg, etc).
            toUser();
        dash = new PscUserBuilder("dash").
            add(STUDY_SUBJECT_CALENDAR_MANAGER).forSites(nu, mayo).forStudies(ie, etc).
            toUser();
        zelda = new PscUserBuilder("zelda").add(STUDY_TEAM_ADMINISTRATOR).forAllSites().toUser();

        studyDao = registerDaoMockFor(StudyDao.class);
        pscUserService = registerMockFor(PscUserService.class);
        configuration = registerMockFor(Configuration.class);

        command = new DashboardCommand(jo, pscUserService, studyDao, configuration);
        command.setUser(dash);
    }

    ////// hidden info

    public void testIsNoHiddenInfoWhenOwnDashboard() {
        command.setUser(jo);
        assertFalse(actualHasHidden());
    }

    public void testIsHiddenInfoWhenDashboardUserHasOtherStudies() throws Exception {
        expectVisibleStudyIds(dash, 1701, 4401);
        expectVisibleStudyIds(jo, 4401, 3000);
        assertTrue(actualHasHidden());
    }

    public void testIsNoHiddenInformationWhenCurrentUserHasOtherStudies() throws Exception {
        expectVisibleStudyIds(dash, 4401);
        expectVisibleStudyIds(jo, 4401, 3000);

        expectVisibleAssignments(dash, 3);
        expectVisibleAssignments(jo, 3);

        assertFalse(actualHasHidden());
    }

    public void testIsNoHiddenInformationWhenCurrentUserHasTheSameStudies() throws Exception {
        expectVisibleStudyIds(dash, 3000, 4401);
        expectVisibleStudyIds(jo, 4401, 3000);

        expectVisibleAssignments(dash, 3);
        expectVisibleAssignments(jo, 3);

        assertFalse(actualHasHidden());
    }

    public void testIsNoHiddenInformationWhenDashboardUserIsNotAnSSCM() throws Exception {
        command.setUser(zelda);
        assertFalse(actualHasHidden());
    }

    public void testIsHiddenInformationWhenCurrentUserIsNotAnSSCM() throws Exception {
        DashboardCommand thisCmd = new DashboardCommand(zelda, pscUserService, studyDao, configuration);
        thisCmd.setUser(jo);
        assertTrue(thisCmd.getHasHiddenInformation());
    }

    public void testHiddenInformationWhenDashboardUserHasOtherAssignments() throws Exception {
        expectVisibleStudyIds(dash, 3000, 4401);
        expectVisibleStudyIds(jo, 4401, 3000);

        expectVisibleAssignments(dash, 3, 7);
        expectVisibleAssignments(jo, 3, 1);

        assertTrue(actualHasHidden());
    }

    public void testNotHiddenInformationWhenLoggedInUserHasOtherAssignments() throws Exception {
        expectVisibleStudyIds(dash, 3000, 4401);
        expectVisibleStudyIds(jo, 4401, 3000);

        expectVisibleAssignments(dash, 3, 7);
        expectVisibleAssignments(jo, 7, 3, 1);

        assertFalse(actualHasHidden());
    }

    private void expectVisibleAssignments(PscUser user, Integer... expected) {
        expect(pscUserService.getVisibleAssignmentIds(user, STUDY_SUBJECT_CALENDAR_MANAGER)).
            andReturn(Arrays.asList(expected));
    }

    private void expectVisibleStudyIds(PscUser user, Integer... ids) {
        expect(studyDao.getVisibleStudyIds(
            user.getVisibleStudyParameters(STUDY_SUBJECT_CALENDAR_MANAGER))).
            andReturn(Arrays.asList(ids));
    }

    private boolean actualHasHidden() {
        replayMocks();
        boolean actual = command.getHasHiddenInformation();
        verifyMocks();
        return actual;
    }

    ////// assignable studies

    public void testAssignableStudiesUsesDashboardUserToFindAndLoggedInUserToFilter() throws Exception {
        expect(studyDao.getVisibleStudies(
            dash.getVisibleStudyParameters(STUDY_SUBJECT_CALENDAR_MANAGER))).
            andReturn(Arrays.asList(ie, etc));
        expect(configuration.get(Configuration.ENABLE_ASSIGNING_SUBJECT)).andReturn(Boolean.TRUE).anyTimes();

        replayMocks();
        Map<Study, List<UserStudySiteRelationship>> actual = command.getAssignableStudies();
        verifyMocks();

        assertEquals("Wrong number of studies", 1, actual.size());

        assertTrue("Missing ETC: " + actual.keySet(), actual.containsKey(etc));
        assertEquals("Wrong number of common study sites for ETC: " + actual.get(etc), 
            1, actual.get(etc).size());
        assertEquals("Wrong site in common for ETC",
            nu, actual.get(etc).get(0).getStudySite().getSite());
        assertSame("Wrong user for nu-ETC", jo, actual.get(etc).get(0).getUser());
    }

    public void testAssignableStudiesCachesResults() throws Exception {
        expect(studyDao.getVisibleStudies(
            dash.getVisibleStudyParameters(STUDY_SUBJECT_CALENDAR_MANAGER))).
            andReturn(Arrays.asList(eg, ie, etc)).once();
        expect(configuration.get(Configuration.ENABLE_ASSIGNING_SUBJECT)).andReturn(Boolean.TRUE).anyTimes();
        replayMocks();
        assertSame(command.getAssignableStudies(), command.getAssignableStudies());
        verifyMocks();
    }

    ////// colleagues

    public void testGetColleaguesAlwaysReturnsColleaguesOfLoggedInUser() throws Exception {
        expect(pscUserService.getColleaguesOf(jo, STUDY_SUBJECT_CALENDAR_MANAGER)).andReturn(
            Arrays.asList(createPscUser("alice"), createPscUser("barbara")));

        replayMocks();
        List<PscUser> actual = command.getColleagues();
        verifyMocks();

        assertEquals("Wrong number of colleagues", 2, actual.size());
        assertEquals("Wrong 1st user", "alice", actual.get(0).getUsername());
        assertEquals("Wrong 2nd user", "barbara", actual.get(1).getUsername());
    }

    public void testGetColleaguesCachesItsResults() throws Exception {
        expect(pscUserService.getColleaguesOf(jo, STUDY_SUBJECT_CALENDAR_MANAGER)).andReturn(
            Arrays.asList(createPscUser("alice"), createPscUser("barbara"))).once();

        replayMocks();
        assertSame(command.getColleagues(), command.getColleagues());
        verifyMocks();
    }

    public void testGetColleaguesRemovesTheLoggedInUserIfPresent() throws Exception {
        expect(pscUserService.getColleaguesOf(jo, STUDY_SUBJECT_CALENDAR_MANAGER)).andReturn(
            new ArrayList<PscUser>(Arrays.asList(
                createPscUser("alice"), createPscUser("jo"), createPscUser("barbara"))));

        replayMocks();
        List<PscUser> actual = command.getColleagues();
        verifyMocks();

        assertEquals("Wrong number of colleagues: " + actual, 2, actual.size());
        assertEquals("Wrong 1st user", "alice", actual.get(0).getUsername());
        assertEquals("Wrong 2nd user", "barbara", actual.get(1).getUsername());
    }
}
