package edu.northwestern.bioinformatics.studycalendar.restlets.representations;

import edu.northwestern.bioinformatics.studycalendar.domain.UserAction;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import gov.nih.nci.security.authorization.domainobjects.User;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setGridId;

/**
 * @author Jalpa Patel
 */

public class UndoableActionListJsonRepresentationTest extends JsonRepresentationTestCase {
    private List<UserAction> userActions;
    private UserAction ua1, ua2, ua3;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private final String CONTEXT = "/psc/schedule";
    private final String ROOT_URI = "localhost/api";

    public void setUp() throws Exception {
        super.setUp();
        User csmUser = AuthorizationObjectFactory.createCsmUser(11, "perry");
        ua1 = setGridId("ua1", new UserAction("Delayed for 4 days", CONTEXT, "delay", false, csmUser));
        ua1.setTime(sdf.parse("2010-08-17 10:30:45.361"));

        ua2 = setGridId("ua2", new UserAction("Rescheduled from 2010-12-12 to 2010-12-19", CONTEXT, "scheduled", false, csmUser));
        ua2.setTime(sdf.parse("2010-08-17 10:27:58.361"));

        ua3 = setGridId("ua3", new UserAction("Notification dismissed", CONTEXT, "dismiss", false, csmUser));
        ua3.setTime(sdf.parse("2010-08-17 10:19:58.361"));

        userActions = new ArrayList<UserAction>();
        userActions.add(ua1);
        userActions.add(ua2);
        userActions.add(ua3);
    }

    public void testRepresentationIncludesContext() throws Exception {
        JSONObject actual = serialize(Arrays.<UserAction>asList(), CONTEXT, ROOT_URI);
        assertEquals("No cotext is present", CONTEXT, actual.getString("context"));
    }

    public void testUndoableActionsElementIsEmptyArrayForEmptyList() throws Exception {
        JSONArray actual = serializeAndReturnUndoableActionsArray(Arrays.<UserAction>asList(), CONTEXT, ROOT_URI);
        assertEquals(actual.length(), 0);
    }

    public void testUndoableActionsElementHasOneEntryPerUserAction() throws Exception {
        JSONArray actual = serializeAndReturnUndoableActionsArray(userActions, CONTEXT, ROOT_URI);
        assertEquals("Wrong number of results", userActions.size(), actual.length());
    }

    public void testUndoableActionsAreInTheSameOrderAsTheInput() throws Exception {
        JSONArray actual = serializeAndReturnUndoableActionsArray(userActions, CONTEXT, ROOT_URI);
        assertEquals("Wrong number of results", 3, actual.length());
        assertEquals("Wrong element 0", "delay", ((JSONObject) actual.get(0)).get("action_type"));
        assertEquals("Wrong element 1", "scheduled", ((JSONObject) actual.get(1)).get("action_type"));
        assertEquals("Wrong element 2", "dismiss", ((JSONObject) actual.get(2)).get("action_type"));
    }

    public void testUndoableActionIncludesDescription() throws Exception {
        JSONObject actual = ((JSONObject) serializeAndReturnUndoableActionsArray(userActions, CONTEXT, ROOT_URI).get(0));
        assertEquals("Wrong description", "Delayed for 4 days", actual.get("description"));
    }

    public void testUndoableActionIncludesContext() throws Exception {
        JSONObject actual = ((JSONObject) serializeAndReturnUndoableActionsArray(userActions, CONTEXT, ROOT_URI).get(0));
        assertEquals("Wrong context", "/psc/schedule", actual.get("context"));
    }

    public void testUndoableActionIncludesURI() throws Exception {
        JSONObject actual = ((JSONObject) serializeAndReturnUndoableActionsArray(userActions, CONTEXT, ROOT_URI).get(0));
        assertEquals("Wrong URI", "localhost/api/user-actions/ua1", actual.get("URI"));
    }

    public void testUndoableActionIncludesTime() throws Exception {
        JSONObject actual = ((JSONObject) serializeAndReturnUndoableActionsArray(userActions, CONTEXT, ROOT_URI).get(0));
        assertEquals("Wrong time", "2010-08-17 10:30:45.361", actual.get("time"));
    }

    public void testUndoableActionIncludesActionType() throws Exception {
        JSONObject actual = ((JSONObject) serializeAndReturnUndoableActionsArray(userActions, CONTEXT, ROOT_URI).get(0));
        assertEquals("Wrong action type", "delay", actual.get("action_type"));
    }

    private JSONObject serialize(List<UserAction> userActions, String context, String baseURI) throws IOException {
        return writeAndParseObject(new UndoableActionListJsonRepresentation(userActions, context, baseURI));
    }

    private JSONArray serializeAndReturnUndoableActionsArray(List<UserAction> userActions, String context, String baseURI)
            throws IOException, JSONException {
        return (JSONArray) serialize(userActions, context, baseURI).get("undoable_actions");
    }
}
