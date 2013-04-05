/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets.representations;

import junit.framework.TestCase;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.MediaType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.channels.Channels;

/**
 * @author Rhett Sutphin
 */
public class StreamingJsonRepresentationTest extends TestCase {
    private TestRepresentation representation;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        representation = new TestRepresentation();
    }

    public void testDefaultMediaTypeIsApplicationJson() throws Exception {
        assertEquals(MediaType.APPLICATION_JSON, new StreamingJsonRepresentation() {
            @Override public void generate(JsonGenerator generator) throws IOException { }
        }.getMediaType());
    }

    public void testWriteNotInvokedUntilWritten() throws Exception {
        assertFalse(representation.isGenerateStarted());
    }

    public void testWriteToWriter() throws Exception {
        StringWriter w = new StringWriter();
        representation.write(w);
        assertTestRepresentationContent(w.toString());
    }

    public void testWriteToStream() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        representation.write(out);
        assertTestRepresentationContent(new String(out.toByteArray(), "UTF-8"));
    }

    public void testWriteToChannel() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        representation.write(Channels.newChannel(out));
        assertTestRepresentationContent(new String(out.toByteArray(), "UTF-8"));
    }

    private void assertTestRepresentationContent(String s) throws JSONException {
        JSONObject actual = new JSONObject(s);
        assertEquals("Missing expected content", 1, actual.get("aleph"));
    }

    private static class TestRepresentation extends StreamingJsonRepresentation {
        private boolean generateStarted = false;

        @Override
        public void generate(JsonGenerator generator) throws JsonGenerationException, IOException {
            generateStarted = true;
            generator.writeStartObject();
            generator.writeNumberField("aleph", 1);
            generator.writeEndObject();
        }

        public boolean isGenerateStarted() {
            return generateStarted;
        }
    }
}
