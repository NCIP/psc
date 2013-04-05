/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets.representations;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

/**
 * @author Nataliya Shurupova
 */
public class SiteListJsonRepresentationTest extends JsonRepresentationTestCase {
    private List<Site> sites;

    public void setUp() throws Exception {
        super.setUp();
        sites = new ArrayList<Site>();
        sites.add(Fixtures.createSite("ECOG-0100"));
        sites.add(Fixtures.createSite("ECOG-0107","ECG2"));
        Site s2 = Fixtures.createSite("ECOG-0003","ECG3");
        s2.setProvider("universe");
        sites.add(s2);
    }

    public void testSitesElementIsEmptyArrayForEmptyList() throws Exception {
        JSONArray actual = serializeAndReturnSitesArray(Arrays.<Site>asList());
        assertEquals(actual.length(), 0);
    }

    public void testSitesElementHasOneEntryPerSite() throws Exception {
        JSONArray actual = serializeAndReturnSitesArray(sites);
        assertEquals("Wrong number of results", sites.size(), actual.length());
    }

    public void testSitesAreInTheSameOrderAsTheInput() throws Exception {
        JSONArray actual = serializeAndReturnSitesArray(sites);
        assertEquals("Wrong number of results", 3, actual.length());
        assertEquals("Wrong element 0", "ECOG-0100", ((JSONObject) actual.get(0)).get("site_name"));
        assertEquals("Wrong element 1", "ECOG-0107", ((JSONObject) actual.get(1)).get("site_name"));
        assertEquals("Wrong element 2", "ECOG-0003", ((JSONObject) actual.get(2)).get("site_name"));
    }

    public void testSiteIncludesAssignedIdentifier() throws Exception {
        JSONObject actual = ((JSONObject) serializeAndReturnSitesArray(sites).get(2));
        assertEquals("Wrong assigned identifier", "ECG3", actual.get("assigned_identifier"));
    }

    public void testSiteIncludesProvider() throws Exception {
        JSONObject actual = ((JSONObject) serializeAndReturnSitesArray(sites).get(2));
        assertEquals("Wrong provider", "universe", actual.get("provider"));
    }

     public void testNoProviderKeyIfSiteDoesNotHaveProvider() throws Exception {
        JSONObject actual = ((JSONObject) serializeAndReturnSitesArray(sites).get(0));
        assertFalse("provider key should not be present", actual.has("provider"));
    }


    public void testNoSecondaryIdentKeyIfSiteDoesNotHaveAnySecondaryIdents() throws Exception {
        JSONObject actual = ((JSONObject) serializeAndReturnSitesArray(sites).get(0));
        assertFalse("secondary_identifiers key should not be present", actual.has("secondary_identifiers"));
    }

    private JSONObject serialize(List<Site> expected) throws IOException {
        return writeAndParseObject(new SiteListJsonRepresentation(expected));
    }

    private JSONArray serializeAndReturnSitesArray(List<Site> expected) throws IOException, JSONException {
        return (JSONArray) serialize(expected).get("sites");
    }
}
