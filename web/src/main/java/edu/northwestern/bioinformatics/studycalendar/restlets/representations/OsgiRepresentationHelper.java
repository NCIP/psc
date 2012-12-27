/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets.representations;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import org.codehaus.jackson.JsonGenerator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.ServiceReference;
import org.restlet.resource.ResourceException;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class OsgiRepresentationHelper {
    public static void writeServiceProperties(JsonGenerator g, ServiceReference ref) throws IOException {
        writeServiceProperties(g, null, ref);
    }

    public static void writeServiceProperties(JsonGenerator g, String rootKey, ServiceReference ref) throws IOException {
        String[] keys = ref.getPropertyKeys();
        if (keys == null || keys.length == 0) return;
        Arrays.sort(keys);
        Map<String, Object> nestedKeys = new KeyNester("", Arrays.asList(keys)).nest();
        writeServicePropertiesObject(g, ref, rootKey, nestedKeys);
    }

    @SuppressWarnings({ "ChainOfInstanceofChecks", "RawUseOfParameterizedType", "unchecked" })
    private static void writeServicePropertiesObject(
        JsonGenerator g, ServiceReference ref, String key, Map<String, Object> keyStructure
    ) throws IOException {
        if (key != null) {
            g.writeObjectFieldStart(key);
        } else {
            g.writeStartObject();
        }
        for (Map.Entry<String, Object> entry : keyStructure.entrySet()) {
            if (entry.getValue() instanceof String) {
                JacksonTools.nullSafeWritePrimitiveField(g, entry.getKey(), ref.getProperty((String) entry.getValue()));
            } else if (entry.getValue() instanceof Map) {
                writeServicePropertiesObject(g, ref, entry.getKey(), (Map<String, Object>) entry.getValue());
            } else {
                throw new StudyCalendarError("Unexpected type in #nestKeys result: " +
                    entry.getValue().getClass().getName());
            }
        }
        g.writeEndObject();
    }

    public static Map<String, Object> flattenJsonConfiguration(String json) throws ResourceException {
        try {
            JSONObject obj = new JSONObject(json);
            return flatten("", obj, new Hashtable<String, Object>());
        } catch (JSONException je) {
            throw new UnsupportedOperationException("TODO");
        }
    }

    @SuppressWarnings({"unchecked"})
    private static Map<String, Object> flatten(
        String prefix, JSONObject in, Map<String, Object> out
    ) throws JSONException {
        Iterator<String> keys = in.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object value = in.get(key);
            if (value instanceof JSONObject) {
                flatten(prefix + key + '.', (JSONObject) value, out);
            } else if (value instanceof JSONArray) {
                out.put(prefix + key, convertArray((JSONArray) value));
            } else {
                out.put(prefix + key, value);
            }
        }
        return out;
    }

    private static Object convertArray(JSONArray in) throws JSONException {
        Object out = Array.newInstance(determineNativeArrayType(in), in.length());
        for (int i = 0; i < in.length(); i++) {
            Array.set(out, i, convertArrayValue(in.get(i), out.getClass().getComponentType()));
        }
        return out;
    }

    private static Object convertArrayValue(Object value, Class<?> type) {
        if (type.equals(String.class)) {
            return value.toString();
        } else if (type.equals(Long.class)) {
            return ((Number) value).longValue();
        } else if (type.equals(Double.class)) {
            return ((Number) value).doubleValue();
        } else {
            return value;
        }
    }

    private static Class<?> determineNativeArrayType(JSONArray in) throws JSONException {
        Class<?> firstType = in.get(0).getClass();
        boolean allNumeric = Number.class.isAssignableFrom(firstType);
        boolean allIntegral = Long.class.isAssignableFrom(firstType) ||
            Integer.class.isAssignableFrom(firstType);
        boolean allBoolean = Boolean.class.isAssignableFrom(firstType);
        for (int i = 1; i < in.length(); i++) {
            Class<?> subsequentType = in.get(i).getClass();
            allNumeric &= Number.class.isAssignableFrom(subsequentType);
            allIntegral &= Long.class.isAssignableFrom(subsequentType) ||
                Integer.class.isAssignableFrom(subsequentType);
            allBoolean &= Boolean.class.isAssignableFrom(subsequentType);
        }
        if (allIntegral) return Long.class;
        if (allNumeric) return Double.class;
        if (allBoolean) return Boolean.class;
        return String.class;
    }

    // damn Java's lack of closures
    private static class KeyNester {
        private String prefix;
        private List<String> keys;

        private String subprefix = null;
        private List<String> subcollection = new LinkedList<String>();
        private Map<String, Object> nest;

        private KeyNester(String prefix, List<String> keys) {
            this.prefix = prefix;
            this.keys = keys;
        }

        public Map<String, Object> nest() {
            nest = new LinkedHashMap<String, Object>();
            for (String key : keys) {
                if (key.indexOf('.') < 0) {
                    completeSubMap();
                    nest.put(key, prefix + key);
                } else {
                    String[] parts = key.split("\\.", 2);
                    if (parts[0].equals(subprefix)) {
                        subcollection.add(parts[1]);
                    } else {
                        completeSubMap();
                        subprefix = parts[0];
                        subcollection.add(parts[1]);
                    }
                }
            }
            completeSubMap();
            return nest;
        }

        private void completeSubMap() {
            if (subprefix != null) {
                // append the now-complete previous subcollection
                nest.put(subprefix, new KeyNester(prefix + subprefix + '.', subcollection).nest());
            }
            subcollection.clear();
            subprefix = null;
        }
    }

    // static class
    private OsgiRepresentationHelper() { }
}
