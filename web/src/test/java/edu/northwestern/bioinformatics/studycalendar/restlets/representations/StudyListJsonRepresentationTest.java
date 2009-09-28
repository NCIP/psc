package edu.northwestern.bioinformatics.studycalendar.restlets.representations;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class StudyListJsonRepresentationTest extends JsonRepresentationTestCase {
    private List<Study> studies;

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

    private JSONObject serialize(List<Study> expected) throws IOException {
        return writeAndParseObject(new StudyListJsonRepresentation(expected));
    }

    private JSONArray serializeAndReturnStudiesArray(List<Study> expected) throws IOException, JSONException {
        return (JSONArray) serialize(expected).get("studies");
    }
}
