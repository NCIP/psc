/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets.representations;

import edu.northwestern.bioinformatics.studycalendar.configuration.Configuration;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySecondaryIdentifier;
import edu.northwestern.bioinformatics.studycalendar.restlets.StudyPrivilege;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.UserTemplateRelationship;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;

import java.io.IOException;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class StudyListJsonRepresentation extends StreamingJsonRepresentation {
    private List<Study> studies;
    private PscUser user;
    private Configuration configuration;

    public StudyListJsonRepresentation(List<Study> studies, Configuration configuration) {
        this.studies = studies;
        this.configuration = configuration;
    }

    public StudyListJsonRepresentation(List<Study> studies, PscUser user, Configuration configuration) {
        this.studies = studies;
        this.user = user;
        this.configuration = configuration;
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
        if (getUser() != null) {
            writeStudyPrivileges(generator, study);
        }
        generator.writeEndObject();
    }

    private void writeStudyPrivileges(JsonGenerator generator, Study study) throws IOException {
        generator.writeFieldName("privileges");
        UserTemplateRelationship utr = new UserTemplateRelationship(getUser(), study, configuration);
        generator.writeStartArray();
        List<StudyPrivilege> privileges = StudyPrivilege.valuesFor(utr);
        for (StudyPrivilege privilege : privileges) {
            generator.writeString(privilege.attributeName());
        }
        generator.writeEndArray();
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

    public PscUser getUser() {
        return user;
    }
}
