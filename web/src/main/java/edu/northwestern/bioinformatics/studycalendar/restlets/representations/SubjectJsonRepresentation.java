/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets.representations;

import edu.northwestern.bioinformatics.studycalendar.domain.Gender;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.domain.SubjectProperty;
import edu.northwestern.bioinformatics.studycalendar.restlets.AbstractPscResource;
import org.codehaus.jackson.JsonGenerator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import java.io.IOException;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.restlets.representations.JacksonTools.*;

/**
 * @author Rhett Sutphin
 */
public class SubjectJsonRepresentation extends StreamingJsonRepresentation {
    private final Subject subject;
    private Reference rootRef;

    public SubjectJsonRepresentation(Subject subject, Reference rootReference) {
        this.subject = subject;
        this.rootRef = rootReference;
    }

    public SubjectJsonRepresentation(JSONObject jsonIn) {
        this.subject = new Subject();
        readJson(jsonIn);
    }

    ////// WRITE

    @Override
    public void generate(JsonGenerator g) throws IOException {
        g.writeStartObject();

            writeBasicSubjectAttributes(g);
            writeSubjectIdentifiers(g);
            writeProperties(g);
            writeLinks(g);

        g.writeEndObject();
    }

    private String getSubjectIdentifier() {
        return getSubject().getPersonId() == null ?
            getSubject().getGridId() : getSubject().getPersonId();
    }

    private void writeBasicSubjectAttributes(JsonGenerator g) throws IOException {
        nullSafeWriteStringField(g, "first_name", getSubject().getFirstName());
        nullSafeWriteStringField(g, "last_name", getSubject().getLastName());
        nullSafeWriteStringField(g, "full_name", getSubject().getFullName());
        nullSafeWriteStringField(g, "last_first", getSubject().getLastFirst());
        nullSafeWriteDateField(g, "birth_date", getSubject().getDateOfBirth());
        if (getSubject().getGender() != null) {
            g.writeStringField("gender", getSubject().getGender().getDisplayName());
        }
    }

    private void writeSubjectIdentifiers(JsonGenerator g) throws IOException {
        nullSafeWriteStringField(g, "person_id", getSubject().getPersonId());
        nullSafeWriteStringField(g, "subject_identifier", getSubjectIdentifier());
    }

    private void writeProperties(JsonGenerator g) throws IOException {
        g.writeArrayFieldStart("properties");
        for (SubjectProperty property : getSubject().getProperties()) {
            g.writeStartObject();
                nullSafeWriteStringField(g, "name", property.getName());
                nullSafeWriteStringField(g, "value", property.getValue());
            g.writeEndObject();
        }
        g.writeEndArray();
    }

    private void writeLinks(JsonGenerator g) throws IOException {
        Reference schedulesHref = getRootRef().clone().
            addSegment("subjects").
            addSegment(getSubjectIdentifier()).
            addSegment("schedules");

        g.writeObjectFieldStart("href");
            g.writeStringField("schedules", schedulesHref.toString());
        g.writeEndObject();
    }

    ////// READ

    // TODO: use something more declarative for this.  Maybe Jackson.

    private void readJson(JSONObject json) {
        if (json.has("first_name")) {
            getSubject().setFirstName(json.optString("first_name"));
        }
        if (json.has("last_name")) {
            getSubject().setLastName(json.optString("last_name"));
        }
        if (json.has("person_id")) {
            getSubject().setPersonId(json.optString("person_id"));
        }
        if (json.has("gender")) {
            getSubject().setGender(Gender.getByCode(json.optString("gender")));
        }
        if (json.has("properties")) {
            List<SubjectProperty> properties = new LinkedList<SubjectProperty>();
            JSONArray inProps = json.optJSONArray("properties");
            for (int i = 0 ; i < inProps.length() ; i++) {
                JSONObject entry = inProps.optJSONObject(i);
                properties.add(new SubjectProperty(
                    entry.optString("name"), entry.optString("value")));
            }
            getSubject().setProperties(properties);
        }
        if (json.has("birth_date")) {
            try {
                getSubject().setDateOfBirth(
                    AbstractPscResource.getApiDateFormat().parse(json.optString("birth_date")));
            } catch (ParseException e) {
                throw new ResourceException(Status.CLIENT_ERROR_NOT_ACCEPTABLE, "Misformatted birth_date", e);
            }
        }
    }

    ////// BEAN PROPERTIES

    public Subject getSubject() {
        return subject;
    }

    public Reference getRootRef() {
        return rootRef;
    }
}
