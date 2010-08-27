package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.core.osgi.OsgiLayerTools;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.test.osgi.PscMockBundle;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.Bundle;
import org.osgi.service.metatype.MetaTypeService;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.springframework.osgi.mock.MockBundleContext;

import java.io.IOException;

import static org.easymock.classextension.EasyMock.*;

/**
 * @author Rhett Sutphin
 */
public class OsgiBundleResourceTest extends AuthorizedResourceTestCase<OsgiBundleResource> {
    private MockBundleContext bundleContext;
    private PscMockBundle[] bundles;
    private OsgiLayerTools osgiLayerTools;

    @Override
    protected OsgiBundleResource createAuthorizedResource() {
        OsgiBundleResource resource = new OsgiBundleResource();
        resource.setBundleContext(bundleContext);
        resource.setOsgiLayerTools(osgiLayerTools);
        return resource;
    }

    @Override
    protected void setUp() throws Exception {
        bundles = new PscMockBundle[] {
            PscMockBundle.create(1, "org.slf4j.api", Bundle.RESOLVED, "1.5.0", null, null),
            PscMockBundle.create(6, "edu.northwestern.bioinformatics.studycalendar.psc-utility",
                Bundle.ACTIVE, "2.5.1", null, null),
            PscMockBundle.create(3, "org.slf4j.org.apache.commons.logging", Bundle.INSTALLED, "1.5.0", null, null),
            PscMockBundle.create(4, "org.slf4j.org.apache.log4j", Bundle.INSTALLED, "1.5.0",
                "Apache Log4j", "One of those loggers")
        };
        bundleContext = new MockBundleContext() {
            @Override
            public Bundle[] getBundles() {
                return bundles;
            }
        };

        MetaTypeService metaTypeService = registerMockFor(MetaTypeService.class);
        expect(metaTypeService.getMetaTypeInformation((Bundle) notNull())).andStubReturn(null);
        osgiLayerTools = registerMockFor(OsgiLayerTools.class);
        expect(osgiLayerTools.getRequiredService(MetaTypeService.class)).andStubReturn(metaTypeService);

        super.setUp();
    }

    public void testGetOnly() throws Exception {
        assertAllowedMethods("GET");
    }
    
    public void testAvailableToSystemAdminsOnly() throws Exception {
        assertRolesAllowedForMethod(Method.GET, PscRole.SYSTEM_ADMINISTRATOR);
    }

    public void testGetReturnsArrayWithOneEntryPerBundle() throws Exception {
        assertEquals(bundles.length, getAndReturnEntityArray().length());
    }

    public void testReturnedArrayEntryObjectsDescribeBundles() throws Exception {
        JSONArray actual = getAndReturnEntityArray();
        assertTrue("Array does not contain JSON objects", actual.get(2) instanceof JSONObject);
        JSONObject bundle2 = (JSONObject) actual.get(2);
        assertEquals("Missing bundle ID", 4, bundle2.get("id"));
        assertEquals("Missing symbolic name", "org.slf4j.org.apache.log4j", bundle2.get("symbolic_name"));
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
    
    public void testReturnsASingleBundleByIdIfBundleIdSpecified() throws Exception {
        UriTemplateParameters.BUNDLE_ID.putIn(request, "6");
        JSONObject actual = getAndReturnSingleEntity();
        assertEquals("Metadata is for wrong bundle", actual.get("id"), 6);
        assertEquals("Metadata is for wrong bundle", actual.get("state"), "ACTIVE");
    }

    public void test404sForUnmatchedBundleId() throws Exception {
        UriTemplateParameters.BUNDLE_ID.putIn(request, "11");
        doGet();
        assertEquals("Should be not found", 404, response.getStatus().getCode());
    }

    public void test404sForNonNumericBundleId() throws Exception {
        UriTemplateParameters.BUNDLE_ID.putIn(request, "alef");
        doGet();
        assertEquals("Should be not found", 404, response.getStatus().getCode());
    }

    private JSONObject getAndReturnSingleEntity() throws IOException {
        try {
            return new JSONObject(getAndReturnText());
        } catch (JSONException je) {
            fail("Response is not parseable as a JSON object: " + je.getMessage());
            return null; // not reached
        }
    }

    private JSONArray getAndReturnEntityArray() throws IOException {
        try {
            return new JSONArray(getAndReturnText());
        } catch (JSONException je) {
            fail("Response is not parseable as a JSON array: " + je.getMessage());
            return null; // not reached
        }
    }

    private String getAndReturnText() throws IOException {
        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
        return response.getEntity().getText();
    }
}
