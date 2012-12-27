/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets.representations;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Gender;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.domain.SubjectProperty;
import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
import gov.nih.nci.cabig.ctms.lang.DateTools;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class SubjectJsonRepresentationTest extends JsonRepresentationTestCase {
    private Subject subject;
    private SubjectJsonRepresentation writeRep;

    private JSONObject subjectJson;
    private SubjectJsonRepresentation readRep;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        subject = Fixtures.createSubject("MR1100401003", "Harvey", "Lago",
            DateTools.createDate(2010, Calendar.APRIL, 1), Gender.UNKNOWN);
        subject.setGridId("GRID%HL");

        writeRep = new SubjectJsonRepresentation(subject, request.getRootRef());

        subjectJson = new JSONObject(
            new MapBuilder<String, Object>().
                put("first_name", "Elwood").
                put("last_name", "Dowd").
                put("person_id", "MR0430507001").
                put("birth_date", "1943-05-07").
                put("gender", "Male").
                toMap()
        );
        subjectJson.put("properties", new JSONArray("[ { 'name': 'Hat size', 'value': '6 3/4' } ]"));
        readRep = new SubjectJsonRepresentation(subjectJson);
    }

    ////// WRITE

    public void testFirstNamePresent() throws Exception {
        assertEquals("Harvey", writeAndGetStringField("first_name"));
    }

    public void testLastNamePresent() throws Exception {
        assertEquals("Lago", writeAndGetStringField("last_name"));
    }

    public void testFullNamePresent() throws Exception {
        assertEquals("Harvey Lago", writeAndGetStringField("full_name"));
    }

    public void testLastFirstPresent() throws Exception {
        assertEquals("Lago, Harvey", writeAndGetStringField("last_first"));
    }

    public void testBirthDatePresent() throws Exception {
        assertEquals("2010-04-01", writeAndGetStringField("birth_date"));
    }

    public void testGenderPresent() throws Exception {
        assertEquals("Unknown", writeAndGetStringField("gender"));
    }

    public void testJsonSafelyGeneratedWhenGenderNull() throws Exception {
        subject.setGender(null);
        assertFalse(writeAndParseObject(writeRep).has("gender"));
    }

    public void testPersonIdPresent() throws Exception {
        assertEquals("MR1100401003", writeAndGetStringField("person_id"));
    }

    public void testSubjectIdentifierIsPersonIdWhenSet() throws Exception {
        assertEquals("MR1100401003", writeAndGetStringField("subject_identifier"));
    }

    public void testSubjectIdentifierIsGridIdWhenNoPersonId() throws Exception {
        subject.setPersonId(null);
        assertEquals("GRID%HL", writeAndGetStringField("subject_identifier"));
    }

    public void testPropertiesEmptyWhenNoProperties() throws Exception {
        JSONArray actual = writeAndParseObject(writeRep).getJSONArray("properties");
        assertNotNull("Missing properties", actual);
        assertEquals("Properties not empty", 0, actual.length());
    }

    public void testPropertiesPresentWhenSubjectHasThem() throws Exception {
        subject.getProperties().add(new SubjectProperty("Visible", "no"));
        subject.getProperties().add(new SubjectProperty("Height", "6 ft"));
        JSONArray actual = writeAndParseObject(writeRep).getJSONArray("properties");
        assertEquals("Wrong number of properties serialized", 2, actual.length());
        assertEquals("Wrong 1st prop name",  "Visible", actual.getJSONObject(0).getString("name"));
        assertEquals("Wrong 1st prop value", "no", actual.getJSONObject(0).getString("value"));
        assertEquals("Wrong 2nd prop name", "Height", actual.getJSONObject(1).getString("name"));
        assertEquals("Wrong 2nd prop value", "6 ft", actual.getJSONObject(1).getString("value"));
    }

    public void testLinkToSchedulesProvided() throws Exception {
        JSONObject actualHref = writeAndParseObject(writeRep).optJSONObject("href");
        assertNotNull("No hyperlinks", actualHref);
        assertEquals("Wrong schedules ref", ROOT_URI + "/subjects/MR1100401003/schedules",
            actualHref.getString("schedules"));
    }

    public void testLinkToSchedulesUsesGridIdIfNoPersonId() throws Exception {
        subject.setPersonId(null);
        JSONObject actualHref = writeAndParseObject(writeRep).optJSONObject("href");
        assertNotNull("No hyperlinks", actualHref);
        assertEquals("Wrong schedules ref", ROOT_URI + "/subjects/GRID%25HL/schedules",
            actualHref.getString("schedules"));
    }

    private String writeAndGetStringField(String field) throws IOException {
        return writeAndParseObject(writeRep).optString(field);
    }

    ////// READ

    public void testReadsFirstName() throws Exception {
        assertEquals("Elwood", readRep.getSubject().getFirstName());
    }

    public void testReadsLastName() throws Exception {
        assertEquals("Dowd", readRep.getSubject().getLastName());
    }

    public void testReadsBirthDate() throws Exception {
        assertDayOfDate(1943, Calendar.MAY, 7, readRep.getSubject().getDateOfBirth());
    }

    public void testReadsPersonId() throws Exception {
        assertEquals("MR0430507001", readRep.getSubject().getPersonId());
    }

    public void testReadsGender() throws Exception {
        assertEquals(Gender.MALE, readRep.getSubject().getGender());
    }

    public void testReadsProperties() throws Exception {
        List<SubjectProperty> actual = readRep.getSubject().getProperties();
        assertEquals(1, actual.size());
        assertEquals("Wrong property name",  "Hat size", actual.get(0).getName());
        assertEquals("Wrong property value", "6 3/4", actual.get(0).getValue());
    }
}
