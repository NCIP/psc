/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets.representations;

import edu.northwestern.bioinformatics.studycalendar.web.WebTestCase;
import gov.nih.nci.cabig.ctms.lang.DateTools;
import org.codehaus.jackson.JsonGenerator;

import java.util.Calendar;

import static edu.northwestern.bioinformatics.studycalendar.restlets.representations.JacksonTools.*;

/**
 * @author Rhett Sutphin
 */
public class JacksonToolsTest extends WebTestCase {
    private JsonGenerator generator;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        generator = registerMockFor(JsonGenerator.class);
    }

    public void testWritePrimitiveWithInteger() throws Exception {
        generator.writeNumberField("foo", 14);
        replayMocks();
        nullSafeWritePrimitiveField(generator, "foo", 14);
        verifyMocks();
    }

    public void testWritePrimitiveWithLong() throws Exception {
        generator.writeNumberField("foo", 14L);
        replayMocks();
        nullSafeWritePrimitiveField(generator, "foo", 14L);
        verifyMocks();
    }

    public void testWritePrimitiveWithDouble() throws Exception {
        generator.writeNumberField("foo", 0.25);
        replayMocks();
        nullSafeWritePrimitiveField(generator, "foo", 0.25);
        verifyMocks();
    }

    public void testWritePrimitiveWithFloat() throws Exception {
        generator.writeNumberField("foo", 0.5f);
        replayMocks();
        nullSafeWritePrimitiveField(generator, "foo", 0.5f);
        verifyMocks();
    }

    public void testWritePrimitiveWithString() throws Exception {
        generator.writeStringField("foo", "n");
        replayMocks();
        nullSafeWritePrimitiveField(generator, "foo", "n");
        verifyMocks();
    }

    public void testWritePrimitiveWithBoolean() throws Exception {
        generator.writeBooleanField("foo", false);
        replayMocks();
        nullSafeWritePrimitiveField(generator, "foo", false);
        verifyMocks();
    }

    public void testWritePrimitiveWithNull() throws Exception {
        replayMocks();
        nullSafeWritePrimitiveField(generator, "foo", null);
        verifyMocks();
    }

    public void testWriteDateWithDate() throws Exception {
        generator.writeStringField("some_date", "2010-09-06");
        replayMocks();
        nullSafeWriteDateField(
            generator, "some_date", DateTools.createDate(2010, Calendar.SEPTEMBER, 6));
        verifyMocks();
    }

    public void testWriteDateWithNull() throws Exception {
        replayMocks();
        nullSafeWriteDateField(generator, "some_date", null);
        verifyMocks();
    }
}
