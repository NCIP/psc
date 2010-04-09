package edu.northwestern.bioinformatics.studycalendar.restlets.representations;

import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import java.util.List;
import java.io.IOException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonGenerationException;

/**
 * @author Jalpa Patel
 */
public class SourceListJsonRepresentation extends StreamingJsonRepresentation {
    private List<Source> sources;

    public SourceListJsonRepresentation(List<Source> sources) {
        this.sources = sources;
    }

    @Override
    public void generate(JsonGenerator generator) throws IOException, JsonGenerationException {
        generator.writeStartObject();
        generator.writeFieldName("sources");
        generator.writeStartArray();

        for (Source source : getSources()) {
            writeSourceObject(generator, source);
        }

        generator.writeEndArray();
        generator.writeEndObject();
    }

    private void writeSourceObject(JsonGenerator generator, Source source) throws IOException {
        generator.writeStartObject();
        JacksonTools.nullSafeWriteStringField(generator, "id", source.getGridId());
        JacksonTools.nullSafeWriteStringField(generator, "name", source.getName());
        if (source.getManualFlag() != null) {
            JacksonTools.nullSafeWritePrimitiveField(generator, "manual_flag", source.getManualFlag());
        }
        generator.writeEndObject();
    }

    public List<Source> getSources() {
        return sources;
    }
}
