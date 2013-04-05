/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets.representations;

import edu.northwestern.bioinformatics.studycalendar.test.osgi.PscMockAttributeDefinition;
import edu.northwestern.bioinformatics.studycalendar.test.osgi.PscMockBundle;
import edu.northwestern.bioinformatics.studycalendar.test.osgi.PscMockMetaTypeInformation;
import edu.northwestern.bioinformatics.studycalendar.test.osgi.PscMockObjectClassDefinition;
import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
import static org.easymock.EasyMock.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.service.metatype.AttributeDefinition;
import org.osgi.service.metatype.MetaTypeService;
import org.springframework.osgi.mock.MockServiceReference;

import java.io.IOException;

/**
 * @author Rhett Sutphin
 */
public class OsgiBundleRepresentationTest extends JsonRepresentationTestCase {
    private PscMockBundle bundleWithServices, bundleWithoutServices;
    private MetaTypeService metaTypeService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        bundleWithoutServices = PscMockBundle.create(1, "org.slf4j.api", Bundle.RESOLVED, "1.5.0", null, null);
        bundleWithServices = PscMockBundle.create(6, "edu.northwestern.bioinformatics.studycalendar.psc-utility",
            Bundle.ACTIVE, "2.5.1", "PSC Utility Code", "Some stuff");
        bundleWithServices.addRegisteredService(new MockServiceReference(
            bundleWithServices,
            new MapBuilder<String, Object>().
                put(Constants.SERVICE_DESCRIPTION, "Plenty").
                put(Constants.SERVICE_PID, "util-o-mat").
                put("matrix", "3x3").
                put("a.very.deeply.nested", "property").
                put("a.very.large", Integer.MAX_VALUE).
                toDictionary(),
            null,
            new String[] { "org.osgi.service.cm.ManagedService" }
        ));
        bundleWithServices.addRegisteredService(new MockServiceReference(
            bundleWithServices,
            new String[] { "java.util.Map" }
        ));

        metaTypeService = registerMockFor(MetaTypeService.class);
        expect(metaTypeService.getMetaTypeInformation((Bundle) notNull())).andStubReturn(null);
    }

    public void testRepresentationForMultipleBundlesIsArray() throws Exception {
        JSONArray actual = representMultiple(bundleWithServices, bundleWithoutServices);
        assertEquals(2, actual.length());
    }

    public void testRepresentationForMultipleBundlesIsSortedByBundleId() throws Exception {
        JSONArray actual = representMultiple(bundleWithServices, bundleWithoutServices);
        assertEquals(1, actual.getJSONObject(0).get("id"));
        assertEquals(6, actual.getJSONObject(1).get("id"));
    }

    public void testRepresentationForOneBundleInListModeIsStillArray() throws Exception {
        JSONArray actual = representMultiple(bundleWithoutServices);
        assertEquals(1, actual.length());
    }

    public void testBundleInfoIncludesSymbolicName() throws Exception {
        assertEquals("edu.northwestern.bioinformatics.studycalendar.psc-utility",
            representSingle(bundleWithServices).get("symbolic_name"));
    }

    public void testBundleInfoIncludesId() throws Exception {
        assertEquals(6, representSingle(bundleWithServices).get("id"));
    }

    public void testBundleInfoIncludesVersion() throws Exception {
        assertEquals("2.5.1", representSingle(bundleWithServices).get("version"));
    }

    public void testBundleInfoIncludesName() throws Exception {
        assertEquals("PSC Utility Code", representSingle(bundleWithServices).get("name"));
    }

    public void testBundleInfoIncludesDescription() throws Exception {
        assertEquals("Some stuff", representSingle(bundleWithServices).get("description"));
    }

    public void testBundleInfoIncludesState() throws Exception {
        JSONArray actual = representMultiple(bundleWithServices, bundleWithoutServices);
        assertEquals("RESOLVED", actual.getJSONObject(0).get("state"));
        assertEquals("ACTIVE", actual.getJSONObject(1).get("state"));
    }

    public void testBundleInfoDoesNotIncludeNameIfNotPresent() throws Exception {
        assertFalse(representSingle(bundleWithoutServices).has("name"));
    }
    
    public void testBundleInfoDoesNotIncludeDescriptionIfNotPresent() throws Exception {
        assertFalse(representSingle(bundleWithoutServices).has("description"));
    }

    public void testBundleInfoDoesNotIncludeServicesIfNotPresent() throws Exception {
        JSONObject actual = representSingle(bundleWithoutServices);
        assertFalse(actual.has("services"));
    }

    public void testBundleInfoIncludesExportedServicesIfPresent() throws Exception {
        JSONObject actual = representSingle(bundleWithServices);
        JSONArray actualServices = (JSONArray) actual.get("services");
        assertEquals("Wrong number of services", 2, actualServices.length());
        assertServiceObject("Wrong first service", "org.osgi.service.cm.ManagedService",
            (JSONObject) actualServices.get(0));
        assertServiceObject("Wrong second service", "java.util.Map",
            (JSONObject) actualServices.get(1));
    }

    private void assertServiceObject(String msg, String expectedInterface, JSONObject actual) throws JSONException {
        assertTrue(msg + ": no interfaces list", actual.has("interfaces"));
        assertEquals(msg + ": wrong number of interfaces", 1,
            actual.getJSONArray("interfaces").length());
        assertEquals(msg + ": wrong interface", expectedInterface,
            actual.getJSONArray("interfaces").get(0));
    }

    public void testBundleInfoIncludesServiceProperties() throws Exception {
        JSONObject actual = representSingle(bundleWithServices);
        JSONObject firstService = actual.getJSONArray("services").getJSONObject(0);
        assertTrue("Missing properties", firstService.has("properties"));
        JSONObject actualProperties = firstService.getJSONObject("properties");
        assertEquals("Missing other property", "3x3", actualProperties.get("matrix"));
        System.out.println(actualProperties);
        assertTrue("Missing nested service properties", actualProperties.has("service"));
        assertEquals("Missing nested desc", "Plenty",
            actualProperties.getJSONObject("service").get("description"));
        assertEquals("Missing nested PID", "util-o-mat",
            actualProperties.getJSONObject("service").get("pid"));
        assertEquals("Missing three-level property", Integer.MAX_VALUE,
            actualProperties.getJSONObject("a").getJSONObject("very").get("large"));
        assertEquals("Missing four-level property", "property",
            actualProperties.getJSONObject("a").getJSONObject("very").
                getJSONObject("deeply").get("nested"));
    }

    public void testBundleInfoIncludesMetaTypesIfAvailable() throws Exception {
        JSONObject actualMetatype = getMetatypeResult(
            new PscMockAttributeDefinition(AttributeDefinition.STRING, "foo", "Foo", "Metasyntax").
                addOption("Bar", "bar"));
        
        assertEquals("Missing ID", "obj", actualMetatype.get("id"));
        assertEquals("Missing name", "Object", actualMetatype.get("name"));
        assertEquals("Missing desc", "Generic",
            actualMetatype.get("description"));
        assertTrue("Missing attributes", actualMetatype.has("attributes"));
        assertEquals(1, actualMetatype.getJSONArray("attributes").length());
        JSONObject firstAttribute = actualMetatype.getJSONArray("attributes").getJSONObject(0);
        assertEquals("Missing attribute ID", "foo", firstAttribute.get("id"));
        assertEquals("Missing attribute name", "Foo", firstAttribute.get("name"));
        assertEquals("Missing attribute desc", "Metasyntax", firstAttribute.get("description"));
        assertEquals("Missing attribute type", "string", firstAttribute.get("type"));
        assertTrue("Missing attribute options", firstAttribute.has("options"));
        JSONArray actualOptions = firstAttribute.getJSONArray("options");
        assertEquals("Missing option label", "Bar", actualOptions.getJSONObject(0).get("label"));
        assertEquals("Missing option value", "bar", actualOptions.getJSONObject(0).get("value"));
    }

    public void testBundleInfoDoesNotIncludeMetatypeIfNotPresent() throws Exception {
        expect(metaTypeService.getMetaTypeInformation(bundleWithServices)).andReturn(null);

        JSONObject bundle6 = representSingle(bundleWithServices);
        JSONObject firstService = bundle6.getJSONArray("services").getJSONObject(0);
        assertFalse(firstService.has("metatype"));                                                      
    }
    
    public void testBundleInfoIncludesCorrectTypeForStringMetaTypeAttributes() throws Exception {
        JSONObject actualMetatype = getMetatypeResult(
            new PscMockAttributeDefinition(AttributeDefinition.STRING, "foo"));
        assertEquals("string", actualMetatype.getJSONArray("attributes").getJSONObject(0).get("type"));
    }

    public void testBundleInfoIncludesCorrectTypeForLongMetaTypeAttributes() throws Exception {
        JSONObject actualMetatype = getMetatypeResult(
            new PscMockAttributeDefinition(AttributeDefinition.LONG, "foo"));
        assertEquals("long", actualMetatype.getJSONArray("attributes").getJSONObject(0).get("type"));
    }

    public void testBundleInfoIncludesCorrectTypeForIntegerMetaTypeAttributes() throws Exception {
        JSONObject actualMetatype = getMetatypeResult(
            new PscMockAttributeDefinition(AttributeDefinition.INTEGER, "foo"));
        assertEquals("integer", actualMetatype.getJSONArray("attributes").getJSONObject(0).get("type"));
    }

    public void testBundleInfoIncludesCorrectTypeForShortMetaTypeAttributes() throws Exception {
        JSONObject actualMetatype = getMetatypeResult(
            new PscMockAttributeDefinition(AttributeDefinition.SHORT, "foo"));
        assertEquals("short", actualMetatype.getJSONArray("attributes").getJSONObject(0).get("type"));
    }

    public void testBundleInfoIncludesCorrectTypeForCharacterMetaTypeAttributes() throws Exception {
        JSONObject actualMetatype = getMetatypeResult(
            new PscMockAttributeDefinition(AttributeDefinition.CHARACTER, "foo"));
        assertEquals("character", actualMetatype.getJSONArray("attributes").getJSONObject(0).get("type"));
    }

    public void testBundleInfoIncludesCorrectTypeForByteMetaTypeAttributes() throws Exception {
        JSONObject actualMetatype = getMetatypeResult(
            new PscMockAttributeDefinition(AttributeDefinition.BYTE, "foo"));
        assertEquals("byte", actualMetatype.getJSONArray("attributes").getJSONObject(0).get("type"));
    }

    public void testBundleInfoIncludesCorrectTypeForDoubleMetaTypeAttributes() throws Exception {
        JSONObject actualMetatype = getMetatypeResult(
            new PscMockAttributeDefinition(AttributeDefinition.DOUBLE, "foo"));
        assertEquals("double", actualMetatype.getJSONArray("attributes").getJSONObject(0).get("type"));
    }

    public void testBundleInfoIncludesCorrectTypeForFloatMetaTypeAttributes() throws Exception {
        JSONObject actualMetatype = getMetatypeResult(
            new PscMockAttributeDefinition(AttributeDefinition.FLOAT, "foo"));
        assertEquals("float", actualMetatype.getJSONArray("attributes").getJSONObject(0).get("type"));
    }

    public void testBundleInfoIncludesCorrectTypeForBooleanMetaTypeAttributes() throws Exception {
        JSONObject actualMetatype = getMetatypeResult(
            new PscMockAttributeDefinition(AttributeDefinition.BOOLEAN, "foo"));
        assertEquals("boolean", actualMetatype.getJSONArray("attributes").getJSONObject(0).get("type"));
    }

    private JSONObject getMetatypeResult(PscMockAttributeDefinition attr) throws IOException, JSONException {
        PscMockObjectClassDefinition ocd = new PscMockObjectClassDefinition("obj", "Object", "Generic").
            addAttributeDefinition(attr);
        PscMockMetaTypeInformation mti = new PscMockMetaTypeInformation(bundleWithServices).
            addObjectClassDefinition("util-o-mat", ocd);

        expect(metaTypeService.getMetaTypeInformation(bundleWithServices)).andReturn(mti);

        JSONObject bundle6 = representSingle(bundleWithServices);
        JSONObject firstService = bundle6.getJSONArray("services").getJSONObject(0);
        assertTrue("Missing metatype", firstService.has("metatype"));
        return firstService.getJSONObject("metatype");
    }

    private JSONObject representSingle(Bundle bundle) throws IOException {
        replayMocks();
        JSONObject result = writeAndParseObject(OsgiBundleRepresentation.create(bundle, metaTypeService));
        verifyMocks();
        return result;
    }

    private JSONArray representMultiple(Bundle... bundles) throws IOException {
        replayMocks();
        JSONArray result = writeAndParseArray(OsgiBundleRepresentation.create(bundles, metaTypeService));
        verifyMocks();
        return result;
    }
}
