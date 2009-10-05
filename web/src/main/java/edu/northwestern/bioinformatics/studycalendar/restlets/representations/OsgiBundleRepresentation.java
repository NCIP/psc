package edu.northwestern.bioinformatics.studycalendar.restlets.representations;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.restlets.OsgiBundleState;
import static edu.northwestern.bioinformatics.studycalendar.restlets.representations.JacksonTools.*;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.metatype.AttributeDefinition;
import org.osgi.service.metatype.MetaTypeInformation;
import org.osgi.service.metatype.MetaTypeService;
import org.osgi.service.metatype.ObjectClassDefinition;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Representation for one or many OSGi bundles.
 *
 * @author Rhett Sutphin
 */
public class OsgiBundleRepresentation extends StreamingJsonRepresentation {
    private Bundle[] bundles;
    private boolean isCollection;
    private MetaTypeService metaTypeService;

    public static OsgiBundleRepresentation create(Bundle bundle, MetaTypeService metaTypeService) {
        return new OsgiBundleRepresentation(new Bundle[] { bundle }, false, metaTypeService);
    }

    public static OsgiBundleRepresentation create(Bundle[] list, MetaTypeService metaTypeService) {
        return new OsgiBundleRepresentation(list, true, metaTypeService);
    }

    private OsgiBundleRepresentation(Bundle[] bundles, boolean isCollection, MetaTypeService metaTypeService) {
        super();
        this.bundles = bundles;
        Arrays.sort(this.bundles, new Comparator<Bundle>() {
            public int compare(Bundle b1, Bundle b2) {
                return (int) (b1.getBundleId() - b2.getBundleId());
            }
        });
        this.isCollection = isCollection;
        this.metaTypeService = metaTypeService;
    }

    @Override
    public void generate(JsonGenerator generator) throws IOException, JsonGenerationException {
        if (isCollection) generator.writeStartArray();
        for (Bundle bundle : bundles) {
            writeBundle(generator, bundle);
        }
        if (isCollection) generator.writeEndArray();
    }

    @SuppressWarnings({ "unchecked" })
    private void writeBundle(JsonGenerator g, Bundle bundle) throws IOException {
        g.writeStartObject();
        g.writeNumberField("id", bundle.getBundleId());
        g.writeStringField("symbolic_name", bundle.getSymbolicName());
        g.writeStringField("state", OsgiBundleState.valueOfConstant(bundle.getState()).name());

        Dictionary<String, Object> headers = bundle.getHeaders();
        Enumeration<String> headerNames = headers.keys();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            if (name.startsWith("Bundle-")) {
                String key = name.substring(7).toLowerCase();
                Object value = headers.get(name);
                if (value != null) g.writeStringField(key, value.toString());
            }
        }

        ServiceReference[] refs = bundle.getRegisteredServices();
        if (refs != null && refs.length > 0) {
            MetaTypeInformation metaTypes = metaTypeService.getMetaTypeInformation(bundle);
            g.writeArrayFieldStart("services");
            for (ServiceReference ref : refs) {
                writeService(g, ref, metaTypes);
            }
            g.writeEndArray();
        }

        g.writeEndObject();
    }

    private void writeService(JsonGenerator g, ServiceReference ref, MetaTypeInformation metaTypes) throws IOException {
        g.writeStartObject();

        String[] interfaces = (String[]) ref.getProperty(Constants.OBJECTCLASS);
        g.writeArrayFieldStart("interfaces");
        for (String anInterface : interfaces) {
            g.writeString(anInterface);
        }
        g.writeEndArray();

        writeServiceProperties(g, ref);

        if (metaTypes != null) {
            String pid = (String) ref.getProperty(Constants.SERVICE_PID);
            try {
                ObjectClassDefinition metainfo = metaTypes.getObjectClassDefinition(pid, null);
                writeMetatype(g, metainfo);
            } catch (IllegalArgumentException iae) {
                // there isn't metadata for this service
            }
        }

        g.writeEndObject();
    }

    private void writeServiceProperties(JsonGenerator g, ServiceReference ref) throws IOException {
        String[] keys = ref.getPropertyKeys();
        if (keys == null || keys.length == 0) return;
        Arrays.sort(keys);
        Map<String, Object> nestedKeys = nestKeys("", Arrays.asList(keys));
        writeServicePropertiesObject(g, ref, "properties", nestedKeys);
    }

    private Map<String, Object> nestKeys(String prefix, List<String> keys) {
        return new KeyNester(prefix, keys).nest();
    }

    @SuppressWarnings({ "ChainOfInstanceofChecks", "RawUseOfParameterizedType", "unchecked" })
    private void writeServicePropertiesObject(
        JsonGenerator g, ServiceReference ref, String key, Map<String, Object> keyStructure
    ) throws IOException {
        g.writeObjectFieldStart(key);
        for (Map.Entry<String, Object> entry : keyStructure.entrySet()) {
            if (entry.getValue() instanceof String) {
                nullSafeWritePrimitiveField(g, entry.getKey(), ref.getProperty((String) entry.getValue()));
            } else if (entry.getValue() instanceof Map) {
                writeServicePropertiesObject(g, ref, entry.getKey(), (Map<String, Object>) entry.getValue());
            } else {
                throw new StudyCalendarError("Unexpected type in #nestKeys result: " +
                    entry.getValue().getClass().getName());
            }
        }
        g.writeEndObject();
    }

    private void writeMetatype(JsonGenerator g, ObjectClassDefinition type) throws IOException {
        if (type == null) return;
        g.writeObjectFieldStart("metatype");
        nullSafeWriteStringField(g, "id", type.getID());
        nullSafeWriteStringField(g, "name", type.getName());
        nullSafeWriteStringField(g, "description", type.getDescription());
        writeMetatypeAttributes(g, type.getAttributeDefinitions(ObjectClassDefinition.ALL));
        g.writeEndObject();
    }

    private void writeMetatypeAttributes(JsonGenerator g, AttributeDefinition[] attributeDefinitions) throws IOException {
        if (attributeDefinitions == null || attributeDefinitions.length == 0) return;
        g.writeArrayFieldStart("attributes");
        for (AttributeDefinition definition : attributeDefinitions) {
            writeMetatypeAttribute(g, definition);
        }
        g.writeEndArray();
    }

    private void writeMetatypeAttribute(JsonGenerator g, AttributeDefinition definition) throws IOException {
        g.writeStartObject();

        nullSafeWriteStringField(g, "id", definition.getID());
        nullSafeWriteStringField(g, "name", definition.getName());
        nullSafeWriteStringField(g, "description", definition.getDescription());
        g.writeStringField("type", attributeTypeName(definition.getType()));

        String[] optionLabels = definition.getOptionLabels();
        String[] optionValues = definition.getOptionValues();
        int optionCount = Math.min(optionLabels.length, optionValues.length);
        if (optionCount > 0) {
            g.writeArrayFieldStart("options");
            for (int i = 0 ; i < optionCount ; i++) {
                g.writeStartObject();
                nullSafeWriteStringField(g, "label", optionLabels[i]);
                nullSafeWriteStringField(g, "value", optionValues[i]);
                g.writeEndObject();
            }
            g.writeEndArray();
        }

        g.writeEndObject();
    }

    private String attributeTypeName(int code) {
        switch(code) {
            case AttributeDefinition.BOOLEAN:   return "boolean";
            case AttributeDefinition.BYTE:      return "byte";
            case AttributeDefinition.CHARACTER: return "character";
            case AttributeDefinition.DOUBLE:    return "double";
            case AttributeDefinition.FLOAT:     return "float";
            case AttributeDefinition.INTEGER:   return "integer";
            case AttributeDefinition.LONG:      return "long";
            case AttributeDefinition.SHORT:     return "short";
            case AttributeDefinition.STRING:    return "string";
        }
        return "unknown";
    }

    // damn Java's lack of closures
    private class KeyNester {
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
}
