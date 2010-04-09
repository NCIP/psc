package edu.northwestern.bioinformatics.studycalendar.restlets.representations;

import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createSource;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

/**
 * @author Jalpa Patel
 */
public class SourceListJsonRepresentationTest  extends JsonRepresentationTestCase {
    private List<Source> sources;

    public void setUp() throws Exception {
        super.setUp();
        sources = new ArrayList<Source>();
        Source s1 = createSource("TestSource1");
        Source s2 = createSource("TestSource2");
        s2.setManualFlag(true);
        sources.add(s1);
        sources.add(s2);
    }

    public void testSourcesElementIsEmptyArrayForEmptyList() throws Exception {
        JSONArray actual = serializeAndReturnSourcessArray(Arrays.<Source>asList());
        assertEquals(actual.length(), 0);
    }

    public void testSourcesElementHasOneEntryPerStudy() throws Exception {
        JSONArray actual = serializeAndReturnSourcessArray(sources);
        assertEquals("Wrong number of results", sources.size(), actual.length());
    }

    public void testSourcesAreInTheSameOrderAsTheInput() throws Exception {
        JSONArray actual = serializeAndReturnSourcessArray(sources);
        assertEquals("Wrong number of results", 2, actual.length());
        assertEquals("Wrong element 0", "TestSource1", ((JSONObject) actual.get(0)).get("name"));
        assertEquals("Wrong element 1", "TestSource2", ((JSONObject) actual.get(1)).get("name"));

    }

    public void testSourceIncludesManualFlag() throws Exception {
        JSONObject actual = ((JSONObject) serializeAndReturnSourcessArray(sources).get(1));
        assertEquals("Wrong assigned identifier", true, actual.get("manual_flag"));
    }

    private JSONObject serialize(List<Source> expected) throws IOException {
        return writeAndParseObject(new SourceListJsonRepresentation(expected));
    }

    private JSONArray serializeAndReturnSourcessArray(List<Source> expected) throws IOException, JSONException {
        return (JSONArray) serialize(expected).get("sources");
    }
}

