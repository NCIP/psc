/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets.representations;

import edu.northwestern.bioinformatics.studycalendar.restlets.RestletTestCase;
import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.osgi.mock.MockServiceReference;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class OsgiRepresentationHelperTest extends RestletTestCase {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private JsonGenerator generator;
    private StringWriter out;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        out = new StringWriter();
        generator = new JsonFactory().createJsonGenerator(out);
    }

    public void testWriteServicePropertiesWithKey() throws Exception {
        generator.writeStartObject();
        OsgiRepresentationHelper.writeServiceProperties(generator, "props",
            serviceReferenceWithProperties(mapBuilder().put("matrix", "4x4")));
        generator.writeEndObject();

        JSONObject actual = outputAsObject();
        assertTrue("Missing key", actual.has("props"));
        assertTrue("Missing properties", actual.getJSONObject("props").has("matrix"));
    }

    public void testWriteServicePropertiesWithoutKey() throws Exception {
        OsgiRepresentationHelper.writeServiceProperties(generator,
            serviceReferenceWithProperties(mapBuilder().put("matrix", "4x4")));

        JSONObject actual = outputAsObject();
        assertTrue("Missing sole key", actual.has("matrix"));
        assertEquals("Missing value", "4x4", actual.get("matrix"));
    }

    public void testWriteSingleLevelNestedProperties() throws Exception {
        OsgiRepresentationHelper.writeServiceProperties(generator,
            serviceReferenceWithProperties(mapBuilder().
                put("service.pid", "com.sun.java.foo").
                put("service.description", "Metasyntax")
            ));

        JSONObject actual = outputAsObject();
        assertTrue("Missing nested property root", actual.has("service"));
        assertEquals("Missing nested PID", "com.sun.java.foo", actual.getJSONObject("service").get("pid"));
        assertEquals("Missing nested desc", "Metasyntax", actual.getJSONObject("service").get("description"));
    }

    public void testManyLevelNestedProperties() throws Exception {
        OsgiRepresentationHelper.writeServiceProperties(generator,
            serviceReferenceWithProperties(mapBuilder().
                put("a.b.c.d", 4).
                put("a.b.f", "3.0").
                put("a.g.h", 3.1).
                put("a.g.i.j.k", "5.0").
                put("a.g.i.j.l", 5.25f).
                put("m.n", "two").
                put("o", "one")
            ));

        JSONObject actual = outputAsObject();
        assertEquals("Missing four-level nest", 4,
            actual.getJSONObject("a").getJSONObject("b").getJSONObject("c").get("d"));
        assertEquals("Missing three-level nest", "3.0",
            actual.getJSONObject("a").getJSONObject("b").get("f"));
        assertEquals("Missing three-level nest", 3.1,
            actual.getJSONObject("a").getJSONObject("g").get("h"));
        assertEquals("Missing five-level nest", "5.0",
            actual.getJSONObject("a").getJSONObject("g").getJSONObject("i").getJSONObject("j").get("k"));
        assertEquals("Missing five-level nest", 5.25,
            actual.getJSONObject("a").getJSONObject("g").getJSONObject("i").getJSONObject("j").get("l"));
        assertEquals("Missing two-level nest", "two", actual.getJSONObject("m").get("n"));
        assertEquals("Missing flat property", "one", actual.get("o"));
    }

    public void testPropertyFlattener() throws Exception {
        String json = "{ a: { b: \"c\", d: { e: \"f\" } }, g: 5 }";
        Map<String, Object> properties = OsgiRepresentationHelper.flattenJsonConfiguration(json);
        assertEquals("Missing a.b", "c", properties.get("a.b"));
        assertEquals("Missing a.d.e", "f", properties.get("a.d.e"));
        assertEquals("Missing g", 5, properties.get("g"));
    }

    public void testJsonConfigurationReaderConvertsStringJsonArrayToJavaArray() throws Exception {
        String json = "{ a: [\"b\", \"c\"] }";
        Map<String, Object> properties = OsgiRepresentationHelper.flattenJsonConfiguration(json);
        assertTrue("Missing 'a' element", properties.containsKey("a"));
        assertTrue("'a' element is wrong type", properties.get("a") instanceof String[]);
        String[] actual = (String[]) properties.get("a");
        assertEquals("Wrong first element", "b", actual[0]);
        assertEquals("Wrong second element", "c", actual[1]);
        assertEquals("Wrong number of elements", 2, actual.length);
    }

    public void testJsonConfigurationReaderConvertsIntegralJsonArrayToLongArray() throws Exception {
        String json = "{ a: [11, 72, 53] }";
        Map<String, Object> properties = OsgiRepresentationHelper.flattenJsonConfiguration(json);
        assertTrue("Missing 'a' element", properties.containsKey("a"));
        assertTrue("'a' element is wrong type: " + properties.get("a").getClass().getName(),
            properties.get("a") instanceof Long[]);
        Long[] actual = (Long[]) properties.get("a");
        assertEquals("Wrong first element",  11L, actual[0].longValue());
        assertEquals("Wrong second element", 72L, actual[1].longValue());
        assertEquals("Wrong third element",  53L, actual[2].longValue());
        assertEquals("Wrong number of elements", 3, actual.length);
    }

    @SuppressWarnings({"UnnecessaryUnboxing"})
    public void testJsonConfigurationReaderConvertsFloatingPointJsonArrayToDoubleArray() throws Exception {
        String json = "{ a: [1, 4.125, 5.25] }";
        Map<String, Object> properties = OsgiRepresentationHelper.flattenJsonConfiguration(json);
        assertTrue("Missing 'a' element", properties.containsKey("a"));
        assertTrue("'a' element is wrong type: " + properties.get("a").getClass().getName(),
            properties.get("a") instanceof Double[]);
        Double[] actual = (Double[]) properties.get("a");
        assertEquals("Wrong first element",  1.0,   actual[0].doubleValue());
        assertEquals("Wrong second element", 4.125, actual[1].doubleValue());
        assertEquals("Wrong third element",  5.25,  actual[2].doubleValue());
        assertEquals("Wrong number of elements", 3, actual.length);
    }

    public void testJsonConfigurationReaderConvertsBooleanJsonArrayToBooleanArray() throws Exception {
        String json = "{ a: [true, false, false] }";
        Map<String, Object> properties = OsgiRepresentationHelper.flattenJsonConfiguration(json);
        assertTrue("Missing 'a' element", properties.containsKey("a"));
        assertTrue("'a' element is wrong type: " + properties.get("a").getClass().getName(),
            properties.get("a") instanceof Boolean[]);
        Boolean[] actual = (Boolean[]) properties.get("a");
        assertTrue( "Wrong first element",  actual[0]);
        assertFalse("Wrong second element", actual[1]);
        assertFalse("Wrong third element",  actual[2]);
        assertEquals("Wrong number of elements", 3, actual.length);
    }

    public void testJsonConfigurationReaderConvertsMixedJsonArrayToStringArray() throws Exception {
        String json = "{ a: [true, \"b\", 7.25, 6] }";
        Map<String, Object> properties = OsgiRepresentationHelper.flattenJsonConfiguration(json);
        assertTrue("Missing 'a' element", properties.containsKey("a"));
        assertTrue("'a' element is wrong type: " + properties.get("a").getClass().getName(),
            properties.get("a") instanceof String[]);
        String[] actual = (String[]) properties.get("a");
        assertEquals("Wrong first element",  "true", actual[0]);
        assertEquals("Wrong second element", "b",    actual[1]);
        assertEquals("Wrong third element",  "7.25", actual[2]);
        assertEquals("Wrong fourth element", "6",    actual[3]);
        assertEquals("Wrong number of elements", 4, actual.length);
    }

    private MapBuilder<String, Object> mapBuilder() {
        return new MapBuilder<String, Object>();
    }

    private ServiceReference serviceReferenceWithProperties(MapBuilder<String, Object> properties) {
        return new MockServiceReference(null, properties.toDictionary(), null,
            new String[] { ManagedService.class.getName() });
    }

    private JSONObject outputAsObject() throws IOException {
        generator.close();
        try {
            return new JSONObject(out.toString());
        } catch (JSONException e) {
            log.info("Generated JSON: {}", out.toString());
            fail("Generated JSON is not valid: " + e.getMessage());
            return null; // Unreachable
        }
    }
}
