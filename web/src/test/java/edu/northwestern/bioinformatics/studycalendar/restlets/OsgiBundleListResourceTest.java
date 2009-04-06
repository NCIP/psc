package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
import org.dynamicjava.osgi.da_launcher.web.DaLauncherWebConstants;
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
        mockBundle(1, "org.slf4j.api", Bundle.RESOLVED, "1.5.0"),
        mockBundle(3, "org.slf4j.org.apache.commons.logging", Bundle.INSTALLED, "1.5.0"),
        mockBundle(4, "org.slf4j.org.apache.log4j", Bundle.INSTALLED, "1.5.0"),
        mockBundle(6, "edu.northwestern.bioinformatics.studycalendar.psc-utility", Bundle.ACTIVE, "2.5.1")
    };

    private static Bundle mockBundle(int id, final String symbolicName, final int mockState, String version) {
        MockBundle bundle = new MockBundle(
            new MapBuilder<String, Object>().put("Bundle-Version", version).toDictionary()
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
