package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.Bundle;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.springframework.osgi.mock.MockBundle;
import org.springframework.osgi.mock.MockBundleContext;

import java.io.IOException;

/**
 * @author Rhett Sutphin
 */
public class OsgiBundleListResourceTest extends AuthorizedResourceTestCase<OsgiBundleListResource> {
    private MockBundleContext bundleContext;
    private static final Bundle[] BUNDLES = {
        mockBundle(1, "org.slf4j.api", Bundle.RESOLVED, "1.5.0", null, null),
        mockBundle(6, "edu.northwestern.bioinformatics.studycalendar.psc-utility",
            Bundle.ACTIVE, "2.5.1", null, null),
        mockBundle(3, "org.slf4j.org.apache.commons.logging", Bundle.INSTALLED, "1.5.0", null, null),
        mockBundle(4, "org.slf4j.org.apache.log4j", Bundle.INSTALLED, "1.5.0",
            "Apache Log4j", "One of those loggers")
    };

    private static Bundle mockBundle(int id, final String symbolicName, final int mockState, String version, String name, String description) {
        MockBundle bundle = new MockBundle(
            new MapBuilder<String, Object>().
                put("Bundle-Version", version).
                put("Bundle-Name", name).
                put("Bundle-Description", description).
                toDictionary()
        ) {
            @Override public int getState() { return mockState; }
            @Override public String getSymbolicName() { return symbolicName; }
        };
        bundle.setBundleId(id);
        return bundle;
    }

    @Override
    protected OsgiBundleListResource createAuthorizedResource() {
        OsgiBundleListResource resource = new OsgiBundleListResource();
        resource.setBundleContext(bundleContext);
        return resource;
    }

    @Override
    protected void setUp() throws Exception {
        bundleContext = new MockBundleContext() {
            @Override
            public Bundle[] getBundles() {
                return BUNDLES;
            }
        };
        super.setUp();
    }

    public void testGetOnly() throws Exception {
        assertAllowedMethods("GET");
    }
    
    public void testAvailableToSystemAdminsOnly() throws Exception {
        assertRolesAllowedForMethod(Method.GET, Role.SYSTEM_ADMINISTRATOR);
    }

    public void testGetReturnsArrayWithOneEntryPerBundle() throws Exception {
        assertEquals(BUNDLES.length, getAndReturnEntityArray().length());
    }

    public void testReturnedArrayEntryObjectsDescribeBundles() throws Exception {
        JSONArray actual = getAndReturnEntityArray();
        assertTrue("Array does not contain JSON objects", actual.get(2) instanceof JSONObject);
        JSONObject bundle2 = (JSONObject) actual.get(2);
        assertEquals("Missing bundle ID", 4, bundle2.get("id"));
        assertEquals("Missing symbolic name", "org.slf4j.org.apache.log4j", bundle2.get("symbolic-name"));
        assertEquals("Missing version", "1.5.0", bundle2.get("version"));
        assertEquals("Missing state", "INSTALLED", bundle2.get("state"));
        assertEquals("Missing name", "Apache Log4j", bundle2.get("name"));
        assertEquals("Missing description", "One of those loggers", bundle2.get("description"));
    }
    
    public void testReturnedArrayIsOrderedByBundleId() throws Exception {
        JSONArray actual = getAndReturnEntityArray();
        assertEquals("Bundle 0 out of order", 1, ((JSONObject) actual.get(0)).get("id"));
        assertEquals("Bundle 1 out of order", 3, ((JSONObject) actual.get(1)).get("id"));
        assertEquals("Bundle 2 out of order", 4, ((JSONObject) actual.get(2)).get("id"));
        assertEquals("Bundle 3 out of order", 6, ((JSONObject) actual.get(3)).get("id"));
    }
    
    public void testNoObjectPropertiesForMissingManifestHeaders() throws Exception {
        JSONArray actual = getAndReturnEntityArray();
        assertTrue("Array does not contain JSON objects", actual.get(1) instanceof JSONObject);
        JSONObject bundle1 = (JSONObject) actual.get(1);
        assertFalse("JSON not missing name", bundle1.has("name"));
    }

    public void testReturnedBundleDescriptionsConvertStateNames() throws Exception {
        JSONArray actual = getAndReturnEntityArray();
        assertEquals("Wrong state for RESOLVED",  "RESOLVED",  ((JSONObject) actual.get(0)).get("state"));
        assertEquals("Wrong state for INSTALLED", "INSTALLED", ((JSONObject) actual.get(1)).get("state"));
        assertEquals("Wrong state for ACTIVE",    "ACTIVE",    ((JSONObject) actual.get(3)).get("state"));
    }

    private JSONArray getAndReturnEntityArray() throws IOException {
        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        String text = response.getEntity().getText();
        try {
            return new JSONArray(text);
        } catch (JSONException je) {
            fail("Response is not parseable as a JSON array: " + je.getMessage());
            return null; // not reached
        }
    }
}
