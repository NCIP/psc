package edu.northwestern.bioinformatics.studycalendar.restlets.representations;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySecondaryIdentifier;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;

import java.io.IOException;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class StudyListJsonRepresentation extends StreamingJsonRepresentation {
    private List<Study> studies;

    public StudyListJsonRepresentation(List<Study> studies) {
        this.studies = studies;
    }

    @Override
    public void generate(JsonGenerator generator) throws IOException, JsonGenerationException {
        generator.writeStartObject();
        generator.writeFieldName("studies");
        generator.writeStartArray();

        for (Study study : getStudies()) {
            writeStudyObject(generator, study);
        }

        generator.writeEndArray();
        generator.writeEndObject();
    }

    private void writeStudyObject(JsonGenerator generator, Study study) throws IOException {
        generator.writeStartObject();
        JacksonTools.nullSafeWriteStringField(generator, "assigned_identifier", study.getAssignedIdentifier());
        JacksonTools.nullSafeWriteStringField(generator, "provider", study.getProvider());
        JacksonTools.nullSafeWriteStringField(generator, "long_title", study.getLongTitle());
        if (!study.getSecondaryIdentifiers().isEmpty()) {
            generator.writeFieldName("secondary_identifiers");
            generator.writeStartArray();
            for (StudySecondaryIdentifier identifier : study.getSecondaryIdentifiers()) {
                writeSecondaryIdentifier(generator, identifier);
            }
            generator.writeEndArray();
        }
        generator.writeEndObject();
    }

    private void writeSecondaryIdentifier(JsonGenerator g, StudySecondaryIdentifier identifier) throws IOException {
        g.writeStartObject();
        JacksonTools.nullSafeWriteStringField(g, "type", identifier.getType());
        JacksonTools.nullSafeWriteStringField(g, "value", identifier.getValue());
        g.writeEndObject();
    }

    public List<Study> getStudies() {
        return studies;
    }
}
