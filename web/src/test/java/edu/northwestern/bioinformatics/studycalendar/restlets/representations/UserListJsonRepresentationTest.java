/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets.representations;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.PscUserBuilder;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import gov.nih.nci.cabig.ctms.lang.DateTools;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;

/**
 * @author Rhett Sutphin
 */
public class UserListJsonRepresentationTest extends JsonRepresentationTestCase {
    private Study a, b, c;
    private Site nu;
    private List<PscUser> someUsers;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        nu = Fixtures.createSite("NU", "IL036");
        Site vu = Fixtures.createSite("VU", "TN801");
        a = Fixtures.createReleasedTemplate("A");
        b = Fixtures.createReleasedTemplate("B");
        c = Fixtures.createReleasedTemplate("C");
        someUsers = Arrays.asList(
            new PscUserBuilder("alice").
                add(STUDY_CALENDAR_TEMPLATE_BUILDER).forSites(nu).forStudies(b, c).
                add(STUDY_CREATOR).forAllSites().
                add(STUDY_SUBJECT_CALENDAR_MANAGER).forSites(nu, vu).forStudies(a, b).
                add(DATA_READER).forAllSites().forAllStudies().
                setCsmUserProperty("firstName", "Alice").
                setCsmUserProperty("lastName", "Zed").
                toUser(),
            new PscUserBuilder("bob").add(SYSTEM_ADMINISTRATOR).toUser()
        );
    }

    public void testBriefFormIncludesTotal() throws Exception {
        assertEquals(7, writeAndReturnBrief(7, 4, 2).opt("total"));
    }

    public void testBriefFormIncludesOffset() throws Exception {
        assertEquals(4, writeAndReturnBrief(7, 4, 2).opt("offset"));
    }

    public void testDefaultOffsetIsZero() throws Exception {
        assertEquals(0, writeAndReturnBrief(7, null, null).opt("offset"));
    }

    public void testBriefFormIncludesLimit() throws Exception {
        assertEquals(2, writeAndReturnBrief(7, 4, 2).opt("limit"));
    }

    public void testDefaultLimitIsNull() throws Exception {
        assertNull(writeAndReturnBrief(7, null, null).opt("limit"));
    }

    public void testBriefFormIncludesUsers() throws Exception {
        JSONArray actual = writeAndReturnBrief(2, null, null).optJSONArray("users");
        assertNotNull("No users", actual);
        assertEquals("Wrong number of users: " + actual, someUsers.size(), actual.length());
        assertEquals("Wrong 1st user", "alice", actual.optJSONObject(0).opt("username"));
        assertEquals("Wrong 2nd user", "bob", actual.optJSONObject(1).opt("username"));
    }

    ////// common user properties

    public void testUserIncludesUsername() throws Exception {
        assertEquals("alice", writeAndReturnBriefUser(0).opt("username"));
    }

    public void testUserIncludesFirstName() throws Exception {
        assertEquals("Alice", writeAndReturnBriefUser(0).opt("first_name"));
    }

    public void testUserIncludesLastName() throws Exception {
        assertEquals("Zed", writeAndReturnBriefUser(0).opt("last_name"));
    }

    public void testUserIncludesDisplayName() throws Exception {
        assertEquals("Alice Zed", writeAndReturnBriefUser(0).opt("display_name"));
    }

    public void testUserIncludesEndDateIfSpecified() throws Exception {
        someUsers.get(0).getCsmUser().setEndDate(DateTools.createDate(2007, Calendar.MARCH, 6));
        assertEquals("2007-03-06", writeAndReturnBriefUser(0).opt("end_date"));
    }

    public void testUserDoesNotIncludeEndDateIfNotSpecified() throws Exception {
        someUsers.get(0).getCsmUser().setEndDate(null);
        assertNull("Should be no date", writeAndReturnBriefUser(0).opt("end_date"));
    }

    ////// roles

    public void testBriefUserDoesNotIncludeRoles() throws Exception {
        assertNull("Should be no roles", writeAndReturnBriefUser(0).opt("roles"));
    }

    public void testFullUserIncludesRoles() throws Exception {
        JSONArray actual = writeAndReturnFullUser(1).optJSONArray("roles");
        assertNotNull("No roles", actual);
        assertEquals("Wrong number of roles", 1, actual.length());
    }

    public void testGlobalRoleValueIsRoleInfoOnly() throws Exception {
        JSONObject actual = writeAndReturnFullUser(1).optJSONArray("roles").optJSONObject(0);
        assertNotNull("Missing role", actual);
        assertEquals("Missing key", "system_administrator", actual.opt("key"));
        assertEquals("Missing name", "System Administrator", actual.opt("display_name"));
    }

    public void testVisibleSiteScopeIsIncluded() throws Exception {
        UserListJsonRepresentation rep = fullRep();
        rep.setVisibleSites(Arrays.asList(nu));
        JSONObject actual = writeAndReturnUserRole(rep, 0, STUDY_SUBJECT_CALENDAR_MANAGER);
        assertEquals("Wrong number of sites visible", 1, actual.optJSONArray("sites").length());
        assertEquals("Wrong site ident", "IL036",
            actual.optJSONArray("sites").getJSONObject(0).get("identifier"));
        assertEquals("Wrong site name", "NU",
            actual.optJSONArray("sites").getJSONObject(0).get("name"));
    }

    public void testVisibleStudyScopeIsIncludedForParticipatingRoles() throws Exception {
        UserListJsonRepresentation rep = fullRep();
        rep.setVisibleParticipatingStudyIdentifiers(Arrays.asList("A", "C"));
        JSONObject actual = writeAndReturnUserRole(rep, 0, STUDY_SUBJECT_CALENDAR_MANAGER);
        assertEquals("Wrong number of studies visible", 1, actual.optJSONArray("studies").length());
        assertEquals("Wrong study", "A",
            actual.optJSONArray("studies").getJSONObject(0).get("identifier"));
    }

    public void testVisibleStudyScopeIsIncludedForManagingRoles() throws Exception {
        UserListJsonRepresentation rep = fullRep();
        rep.setVisibleManagedStudyIdentifiers(Arrays.asList("A", "B"));
        JSONObject actual = writeAndReturnUserRole(rep, 0, STUDY_CALENDAR_TEMPLATE_BUILDER);
        assertEquals("Wrong number of studies visible", 1, actual.optJSONArray("studies").length());
        assertEquals("Wrong study", "B",
            actual.optJSONArray("studies").getJSONObject(0).get("identifier"));
    }

    public void testAllSiteScopeIncludedWhenApplicable() throws Exception {
        JSONObject actual = writeAndReturnUserRole(fullRep(), 0, STUDY_CREATOR);
        assertTrue("Should be all-sites", actual.optBoolean("all_sites"));
    }

    public void testAllStudyScopeIncludedWhenApplicable() throws Exception {
        JSONObject actual = writeAndReturnUserRole(fullRep(), 0, DATA_READER);
        assertTrue("Should be all-studies", actual.optBoolean("all_studies"));
    }

    ////// helpers

    private UserListJsonRepresentation fullRep() {
        return new UserListJsonRepresentation(someUsers, false, 2, null, null);
    }

    private JSONObject writeAndReturnFullUser(int index) throws IOException, JSONException {
        return writeAndParseObject(fullRep()).getJSONArray("users").getJSONObject(index);
    }

    private JSONObject writeAndReturnBriefUser(int index) throws IOException, JSONException {
        return writeAndParseObject(new UserListJsonRepresentation(someUsers, true, 2, null, null)).
            getJSONArray("users").getJSONObject(index);
    }

    private JSONObject writeAndReturnBrief(int total, Integer offset, Integer limit) throws IOException {
        return writeAndParseObject(
            new UserListJsonRepresentation(someUsers, true, total, offset, limit));
    }

    private JSONObject writeAndReturnUserRole(
        UserListJsonRepresentation rep, int userIndex, PscRole expectedRole
    ) throws IOException, JSONException {
        JSONObject user = writeAndParseObject(rep).getJSONArray("users").getJSONObject(userIndex);
        JSONArray roles = user.getJSONArray("roles");
        for (int i = 0; i < roles.length(); i++) {
            JSONObject role = roles.getJSONObject(i);
            if (role.get("key").equals(expectedRole.getCsmName())) {
                return role;
            }
        }
        fail("No role " + expectedRole + " in " + user);
        return null; // unreachable
    }
}
