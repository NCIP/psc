package edu.northwestern.bioinformatics.studycalendar.restlets.representations;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationScopeMappings.createSuiteRoleMembership;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.restlets.StudyPrivilege;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory.createPscUser;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;

/**
 * @author Rhett Sutphin
 */
public class StudyListJsonRepresentationTest extends JsonRepresentationTestCase {
    private List<Study> studies;
    private Study study;
    private Site nu, mayo, vanderbilt;

    public void setUp() throws Exception {
        super.setUp();
        studies = new ArrayList<Study>();
        studies.add(createReleasedTemplate("ECOG-0100"));
        studies.add(createReleasedTemplate("ECOG-0107"));
        Study s2 = createReleasedTemplate("ECOG-0003");
        addSecondaryIdentifier(s2, "aleph", "zero");
        s2.setProvider("universe");
        s2.setLongTitle("This is the longest title I can think of");
        studies.add(s2);
    }

    public void testStudiesElementIsEmptyArrayForEmptyList() throws Exception {
        JSONArray actual = serializeAndReturnStudiesArray(Arrays.<Study>asList());
        assertEquals(actual.length(), 0);
    }

    public void testStudiesElementHasOneEntryPerStudy() throws Exception {
        JSONArray actual = serializeAndReturnStudiesArray(studies);
        assertEquals("Wrong number of results", studies.size(), actual.length());
    }

    public void testStudiesAreInTheSameOrderAsTheInput() throws Exception {
        JSONArray actual = serializeAndReturnStudiesArray(studies);
        assertEquals("Wrong number of results", 3, actual.length());
        assertEquals("Wrong element 0", "ECOG-0100", ((JSONObject) actual.get(0)).get("assigned_identifier"));
        assertEquals("Wrong element 1", "ECOG-0107", ((JSONObject) actual.get(1)).get("assigned_identifier"));
        assertEquals("Wrong element 2", "ECOG-0003", ((JSONObject) actual.get(2)).get("assigned_identifier"));
    }

    public void testStudyIncludesAssignedIdentifier() throws Exception {
        JSONObject actual = ((JSONObject) serializeAndReturnStudiesArray(studies).get(2));
        assertEquals("Wrong assigned identifier", "ECOG-0003", actual.get("assigned_identifier"));
    }

    public void testStudyIncludesProvider() throws Exception {
        JSONObject actual = ((JSONObject) serializeAndReturnStudiesArray(studies).get(2));
        assertEquals("Wrong provider", "universe", actual.get("provider"));
    }

    public void testStudyIncludesLongTitle() throws Exception {
        JSONObject actual = ((JSONObject) serializeAndReturnStudiesArray(studies).get(2));
        assertEquals("Wrong long title", "This is the longest title I can think of", actual.get("long_title"));
    }

    public void testStudyIncludesSecondaryIdentifiers() throws Exception {
        JSONObject actual = ((JSONObject) serializeAndReturnStudiesArray(studies).get(2));
        assertTrue("Idents not an array",
            actual.get("secondary_identifiers") instanceof JSONArray);
        JSONArray actualIdents = (JSONArray) actual.get("secondary_identifiers");
        assertEquals("Wrong number of secondary idents", 1, actualIdents.length());
        assertTrue("Secondary ident isn't an object", actualIdents.get(0) instanceof JSONObject);
        JSONObject actualIdent = (JSONObject) actualIdents.get(0);
        assertEquals("Wrong name for ident", "aleph", actualIdent.get("type"));
        assertEquals("Wrong value for ident", "zero", actualIdent.get("value"));
    }

    public void testNoProviderKeyIfStudyDoesNotHaveProvider() throws Exception {
        JSONObject actual = ((JSONObject) serializeAndReturnStudiesArray(studies).get(0));
        assertFalse("provider key should not be present", actual.has("provider"));
    }

    public void testNoLongTitleKeyIfStudyDoesNotHaveLongTitle() throws Exception {
        JSONObject actual = ((JSONObject) serializeAndReturnStudiesArray(studies).get(0));
        assertFalse("long-title key should not be present", actual.has("long_title"));
    }

    public void testNoSecondaryIdentKeyIfStudyDoesNotHaveAnySecondaryIdents() throws Exception {
        JSONObject actual = ((JSONObject) serializeAndReturnStudiesArray(studies).get(0));
        assertFalse("secondary_identifiers key should not be present", actual.has("secondary_identifiers"));
    }

    public void testAmendStudyPrivilege() throws Exception {
        createStudyForPrivilege();
        study.addManagingSite(nu);
        JSONObject actual = ((JSONObject) serializeAndReturnStudiesArrayWithUser(studies,
                createUserWithMembership(createSuiteRoleMembership(STUDY_CALENDAR_TEMPLATE_BUILDER).forSites(nu).forStudies(study))).get(3));
        assertTrue("Privileges not an array",
            actual.get("privileges") instanceof JSONArray);
        String actualPrivilege = ((JSONArray) actual.get("privileges")).get(0).toString();
        assertEquals("Wrong Privilege", StudyPrivilege.AMEND.attributeName(), actualPrivilege);
    }

    public void testDevelopStudyPrivilege() throws Exception {
        createStudyForPrivilege();
        study.addManagingSite(nu);
        study.setDevelopmentAmendment(new Amendment());
        JSONObject actual = ((JSONObject) serializeAndReturnStudiesArrayWithUser(studies,
                createUserWithMembership(createSuiteRoleMembership(STUDY_CALENDAR_TEMPLATE_BUILDER).forSites(nu).forStudies(study))).get(3));
        assertTrue("Privileges not an array",
            actual.get("privileges") instanceof JSONArray);
        String actualPrivilege = ((JSONArray) actual.get("privileges")).get(0).toString();
        assertEquals("Wrong Privilege", StudyPrivilege.DEVELOP.attributeName(), actualPrivilege);
    }

    public void testSeeDevelopementStudyPrivilege() throws Exception {
        createStudyForPrivilege();
        study.addManagingSite(nu);
        study.setDevelopmentAmendment(new Amendment());
        JSONObject actual = ((JSONObject) serializeAndReturnStudiesArrayWithUser(studies,
                createUserWithMembership(createSuiteRoleMembership(STUDY_CALENDAR_TEMPLATE_BUILDER).forSites(nu).forStudies(study))).get(3));
        assertTrue("Privileges not an array",
            actual.get("privileges") instanceof JSONArray);
        String actualPrivilege = ((JSONArray) actual.get("privileges")).get(1).toString();
        assertEquals("Wrong Privilege", StudyPrivilege.SEE_DEVELOPMENT.attributeName(), actualPrivilege);
    }

    public void testSetManagingSitesStudyPrivilege() throws Exception {
        createStudyForPrivilege();
        study.addManagingSite(nu);
        JSONObject actual = ((JSONObject) serializeAndReturnStudiesArrayWithUser(studies,
                createUserWithMembership(createSuiteRoleMembership(STUDY_QA_MANAGER).forSites(nu))).get(3));
        assertTrue("Privileges not an array",
            actual.get("privileges") instanceof JSONArray);
        String actualPrivilege = ((JSONArray) actual.get("privileges")).get(0).toString();
        assertEquals("Wrong Privilege", StudyPrivilege.SET_MANAGING_SITES.attributeName(), actualPrivilege);
    }

    public void testReleaseStudyPrivilege() throws Exception {
        createStudyForPrivilege();
        study.addManagingSite(nu);
        study.setDevelopmentAmendment(new Amendment());
        JSONObject actual = ((JSONObject) serializeAndReturnStudiesArrayWithUser(studies,
                createUserWithMembership(createSuiteRoleMembership(STUDY_QA_MANAGER).forSites(nu))).get(3));
        assertTrue("Privileges not an array",
            actual.get("privileges") instanceof JSONArray);
        String actualPrivilege = ((JSONArray) actual.get("privileges")).get(2).toString();
        assertEquals("Wrong Privilege", StudyPrivilege.RELEASE.attributeName(), actualPrivilege);
    }

    public void testSetParticipationStudyPrivilege() throws Exception {
        createStudyForPrivilege();
        study.addManagingSite(nu);
        JSONObject actual = ((JSONObject) serializeAndReturnStudiesArrayWithUser(studies,
                createUserWithMembership(createSuiteRoleMembership(STUDY_SITE_PARTICIPATION_ADMINISTRATOR).forSites(nu))).get(3));
        assertTrue("Privileges not an array",
            actual.get("privileges") instanceof JSONArray);
        String actualPrivilege = ((JSONArray) actual.get("privileges")).get(0).toString();
        assertEquals("Wrong Privilege", StudyPrivilege.SET_PARTICIPATION.attributeName(), actualPrivilege);
    }

    public void testApproveStudyPrivilege() throws Exception {
        createStudyForPrivilege();
        JSONObject actual = ((JSONObject) serializeAndReturnStudiesArrayWithUser(studies,
                createUserWithMembership(createSuiteRoleMembership(STUDY_QA_MANAGER).forSites(vanderbilt))).get(3));
        assertTrue("Privileges not an array",
            actual.get("privileges") instanceof JSONArray);
        String actualPrivilege = ((JSONArray) actual.get("privileges")).get(1).toString();
        assertEquals("Wrong Privilege", StudyPrivilege.APPROVE.attributeName(), actualPrivilege);
    }

    public void testScheduleReconsentStudyPrivilege() throws Exception {
        createStudyForPrivilege();
        study.addManagingSite(mayo);
        JSONObject actual = ((JSONObject) serializeAndReturnStudiesArrayWithUser(studies,
                createUserWithMembership(createSuiteRoleMembership(STUDY_QA_MANAGER).forSites(mayo))).get(3));
        assertTrue("Privileges not an array",
            actual.get("privileges") instanceof JSONArray);
        String actualPrivilege = ((JSONArray) actual.get("privileges")).get(1).toString();
        assertEquals("Wrong Privilege", StudyPrivilege.SCHEDULE_RECONSENT.attributeName(), actualPrivilege);
    }

    public void testRegisterStudyPrivilege() throws Exception {
        createStudyForPrivilege();
        JSONObject actual = ((JSONObject) serializeAndReturnStudiesArrayWithUser(studies,
                createUserWithMembership(createSuiteRoleMembership(STUDY_SUBJECT_CALENDAR_MANAGER).forSites(nu).forStudies(study))).get(3));
        assertTrue("Privileges not an array",
            actual.get("privileges") instanceof JSONArray);
        String actualPrivilege = ((JSONArray) actual.get("privileges")).get(0).toString();
        assertEquals("Wrong Privilege", StudyPrivilege.REGISTER.attributeName(), actualPrivilege);
    }

    public void testSeeReleasedStudyPrivilege() throws Exception {
        createStudyForPrivilege();
        JSONObject actual = ((JSONObject) serializeAndReturnStudiesArrayWithUser(studies,
                createUserWithMembership(createSuiteRoleMembership(STUDY_SUBJECT_CALENDAR_MANAGER).forSites(nu).forStudies(study))).get(3));
        assertTrue("Privileges not an array",
            actual.get("privileges") instanceof JSONArray);
        String actualPrivilege = ((JSONArray) actual.get("privileges")).get(1).toString();
        assertEquals("Wrong Privilege", StudyPrivilege.SEE_RELEASED.attributeName(), actualPrivilege);
    }

    public void testAssignIdentifiersStudyPrivilege() throws Exception {
        createStudyForPrivilege();
        study.addManagingSite(nu);
        study.setDevelopmentAmendment(new Amendment());
        JSONObject actual = ((JSONObject) serializeAndReturnStudiesArrayWithUser(studies,
                createUserWithMembership(createSuiteRoleMembership(STUDY_CREATOR).forSites(nu))).get(3));
        assertTrue("Privileges not an array",
            actual.get("privileges") instanceof JSONArray);
        String actualPrivilege = ((JSONArray) actual.get("privileges")).get(2).toString();
        assertEquals("Wrong Privilege", StudyPrivilege.ASSIGN_IDENTIFIERS.attributeName(), actualPrivilege);
    }

    public void testPurgeStudyPrivilege() throws Exception {
        createStudyForPrivilege();
        study.addManagingSite(nu);
        JSONObject actual = ((JSONObject) serializeAndReturnStudiesArrayWithUser(studies,
                createUserWithMembership(createSuiteRoleMembership(STUDY_QA_MANAGER).forSites(nu))).get(3));
        assertTrue("Privileges not an array",
            actual.get("privileges") instanceof JSONArray);
        String actualPrivilege = ((JSONArray) actual.get("privileges")).get(3).toString();
        assertEquals("Wrong Privilege", StudyPrivilege.PURGE.attributeName(), actualPrivilege);
    }

    //Helper Methods

    private JSONObject serialize(List<Study> expected) throws IOException {
        return writeAndParseObject(new StudyListJsonRepresentation(expected));
    }

    private JSONArray serializeAndReturnStudiesArray(List<Study> expected) throws IOException, JSONException {
        return (JSONArray) serialize(expected).get("studies");
    }

    private void createStudyForPrivilege() {
        /*
         * Study is released to NU and VU, approved at NU.
         */
        study = createBasicTemplate("ECT 3402");
        nu = createSite("RHLCCC", "IL036");
        mayo = createSite("Mayo", "MN003");
        vanderbilt = createSite("Vanderbilt", "TN008");

        StudySite nuSS = study.addSite(nu);
        nuSS.approveAmendment(study.getAmendment(), new Date());
        createAssignment(nuSS, createSubject("T", "F"));
        study.addSite(vanderbilt);
        studies.add(study);
    }

    private PscUser createUserWithMembership(SuiteRoleMembership membership) {
        return createPscUser("jo", membership);
    }

    private JSONObject serializeWithUser(List<Study> expected, PscUser user) throws IOException {
        return writeAndParseObject(new StudyListJsonRepresentation(expected, user));
    }

    private JSONArray serializeAndReturnStudiesArrayWithUser(List<Study> expected, PscUser user) throws IOException, JSONException {
        return (JSONArray) serializeWithUser(expected, user).get("studies");
    }

}
