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
        nullSafeWriteStringField(generator, "assigned-identifier", study.getAssignedIdentifier());
        nullSafeWriteStringField(generator, "provider", study.getProvider());
        nullSafeWriteStringField(generator, "long-title", study.getLongTitle());
        if (!study.getSecondaryIdentifiers().isEmpty()) {
            generator.writeFieldName("secondary-identifiers");
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
        nullSafeWriteStringField(g, "type", identifier.getType());
        nullSafeWriteStringField(g, "value", identifier.getValue());
        g.writeEndObject();
    }

    private void nullSafeWriteStringField(JsonGenerator g, String fieldName, String value) throws IOException {
        if (value != null) {
            g.writeStringField(fieldName, value);
        }
    }

    public List<Study> getStudies() {
        return studies;
    }
}
